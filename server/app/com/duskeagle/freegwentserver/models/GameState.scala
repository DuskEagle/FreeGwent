package com.duskeagle.freegwentserver.models

import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.util.{Random, Try}

sealed trait GameState {
  val player1: PlayerState
  val player2: PlayerState

  protected val utf8 = "UTF-8"

  def getPlayer(id: PlayerId): PlayerState = {
    id match {
      case player1.id => player1
      case player2.id => player2
      case _ => throw new IllegalArgumentException(s"PlayerId '$id' not in game")
    }
  }

}

case class MulliganGameState(
  player1: MulliganPlayerState,
  player2: MulliganPlayerState
) extends GameState {

  def mulligan(id: PlayerId, s: String): GameState = {

    val newMulliganState = if (getPlayer(id).id == player1.id) {
      this.copy(player1 = player1.mulligan(s))
    } else {
      this.copy(player2 = player2.mulligan(s))
    }

    newMulliganState.checkMulliganComplete()
  }

  private def checkMulliganComplete(): GameState = {
    if (player1.cardsMulliganed == 2 && player2.cardsMulliganed == 2) {
      val inGameState = InGameState(
        player1 = player1.endMulliganPhase(),
        player2 = player2.endMulliganPhase(),
        board = BoardState.empty,
        player1sTurn = false //Random.nextBoolean
      )
      inGameState.sendGameStart()
      inGameState
    } else {
      this
    }
  }

}

case class InGameState(
  player1: InGamePlayerState,
  player2: InGamePlayerState,
  board: BoardState,
  player1sTurn: Boolean,
  mockGame: Boolean = false
) extends GameState {

  def sendGameStart(): Unit = {
    val gameStart = "game start".getBytes(utf8)
    player1.actor.tell(gameStart, player1.actor)
    player2.actor.tell(gameStart, player2.actor)
  }

  def gameStateMessageString(id: PlayerId): String = {
    val message = if (getPlayer(id).id == player1.id) {
      gameStateMessage(player1, player2)
    } else {
      gameStateMessage(player2, player1)
    }
    Json.toJson[GameStateMessage](message).toString
  }

  private def gameStateMessage(
    toPlayer: InGamePlayerState,
    otherPlayer: InGamePlayerState
  ): GameStateMessage = {
    val boardRotation = if (toPlayer.id == player1.id) {
      board
    } else {
      board.flip
    }
    val ourTurn = player1sTurn ^ (toPlayer.id == player2.id)
    GameStateMessage(
      board = boardRotation,
      hand = toPlayer.hand.cards,
      ourDiscardPile = toPlayer.discardPile.cards,
      theirDiscardPile = otherPlayer.discardPile.cards,
      ourLife = toPlayer.life,
      theirLife = otherPlayer.life,
      theirHandCount = otherPlayer.hand.cards.size,
      ourDeckCount = toPlayer.deck.cards.size,
      theirDeckCount = otherPlayer.deck.cards.size,
      ourTurn = ourTurn
    )
  }

  def updateGameState(id: PlayerId, message: String): InGameState = {
    Json.fromJson[TurnEvents](Json.parse(message)) match {
      case JsSuccess(e: TurnEvents, _) =>  updateGameState(id, e)
      case e: JsError => sys.error(e.toString)
    }
  }

  def updateGameState(playerId: PlayerId, events: TurnEvents): InGameState = {
    val player = if (getPlayer(playerId).id == player1.id) {
      player1
    } else {
      player2
    }
    val newGame = events.events.headOption match {
      case None => this
      case Some(event) =>
        if (event.pass) {
          passMove(player, event)
        } else {
          nonPassMove(player, event)
        }
    }

    if (newGame.player1.passed && newGame.player2.passed) {
      println("Round end")
      roundEnd
    } else {
      val p1sTurn = if (mockGame) {
        player1sTurn
      } else {
        if (newGame.player1.passed && newGame.player2.passed) {
          println("Round end")
          true
        } else if (newGame.player1.passed) {
          false
        } else if (newGame.player2.passed) {
          true
        } else {
          !player1sTurn
        }
      }
      newGame.copy(player1sTurn = p1sTurn)
    }
  }

  private def roundEnd: InGameState = {
    val p1Score = board.melee1.score + board.ranged1.score + board.siege1.score
    val p2Score = board.melee2.score + board.ranged2.score + board.siege2.score

    var p1 = player1
    var p2 = player2
    var p1sTurn = player1sTurn

    // If a player wins, they go first next round.
    // In case of a tie, both players lose a life
    // and the player who passed first goes first
    // next round.
    if (p1Score < p2Score) {
      p1 = p1.loseALife
      p1sTurn = false
    } else if (p2Score < p1Score) {
      p2 = p2.loseALife
      p1sTurn = true
    } else {
      p1 = p1.loseALife
      p2 = p2.loseALife
    }

    if (p1.life <= 0 || p2.life <= 0) {
      println("Game over") // Handle game end here
    }
    p1 = p1.copy(
      discardPile = p1.discardPile.add(
        board.melee1.cards ++ board.ranged1.cards ++ board.siege1.cards
      ),
      passed = false
    )
    p2 = p2.copy(
      discardPile = p2.discardPile.add(
        board.melee2.cards ++ board.ranged2.cards ++ board.siege2.cards
      ),
      passed = false
    )
    copy(player1 = p1, player2 = p2, BoardState.empty, player1sTurn = p1sTurn)

  }

  private def passMove(player: InGamePlayerState, event: TurnEvent): InGameState = {
    val newPlayer = player.copy(passed = true)
    newPlayerAndBoardState(newPlayer, board)
  }

  private def newPlayerAndBoardState(newPlayer: InGamePlayerState, newBoard: BoardState): InGameState = {
    if (newPlayer.id == player1.id) {
      copy(
        player1 = newPlayer,
        board = newBoard
      )
    } else {
      copy(
        player2 = newPlayer,
        board = newBoard
      )
    }
  }

  private def nonPassMove(player: InGamePlayerState, event: TurnEvent): InGameState = {
    val card = CardCollection.getCardById(event.cardId)
    val row = RowId.rowStringToId(event.row, player.id == player1.id)
    var newPlayer = player.copy(
      hand = player.hand.remove(card) // throws if card not in hand
    )
    var newBoard = board
    if (card.hasSpy) {
      val (drawnCards, newDeck) = newPlayer.deck.draw(2)
      newPlayer = newPlayer.copy(
        hand = newPlayer.hand.add(drawnCards),
        deck = newDeck
      )
    }
    if (card.hasMuster) {
      val musterResult = muster(card, row, newPlayer, newBoard)
      newPlayer = musterResult._1
      newBoard = musterResult._2
    }
    newBoard = newBoard.addCard(card, row)
    newPlayerAndBoardState(newPlayer, newBoard)
  }

  private def muster(
    card: Card, row: RowId,
    _player: InGamePlayerState,
    _board: BoardState):
    (InGamePlayerState, BoardState) = {

    var newPlayer = _player
    var newBoard = _board
    val (drawnDeckCards, newDeck) = newPlayer.deck.muster(card.musterId)
    val (drawnHandCards, newHand) = newPlayer.hand.muster(card.musterId)
    newPlayer = newPlayer.copy(
      hand = newHand,
      deck = newDeck
    )
    (drawnDeckCards ++ drawnHandCards).foreach { card =>
      val musterRow = card.combatTypes.headOption.flatMap { ct =>
        Try {
          RowId.combatTypeToId(ct, newPlayer.id == player1.id)
        }.toOption
      }.getOrElse (
        // This is a semi-failure case. We were hoping to place the card
        // onto it's appropriate row based off of the combat type of the
        // card, but that failed, so we'll place it on the same row as
        // the card that the player played.
        row
      )
      newBoard = newBoard.addCard(card, musterRow)
    }
    (newPlayer, newBoard)
  }

}
