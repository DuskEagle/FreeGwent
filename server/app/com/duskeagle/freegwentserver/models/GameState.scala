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
    val ourTurn = if (mockGame) true else player1sTurn ^ toPlayer.id == player2.id
    GameStateMessage(
      board = boardRotation,
      hand = toPlayer.hand.cards,
      ourDiscardPile = Nil,
      theirDiscardPile = Nil,
      ourLife = 2,
      theirLife = 2,
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
        if (event.pass.getOrElse(false)) {
          passMove(player, event)
        } else {
          nonPassMove(player, event)
        }
    }
    newGame.copy(player1sTurn = if (mockGame) player1sTurn else !player1sTurn)
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
