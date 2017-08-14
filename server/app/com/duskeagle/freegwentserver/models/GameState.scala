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

  def getOtherPlayer(id: PlayerId): PlayerState = {
    id match {
      case player1.id => player2
      case player2.id => player1
      case _ => throw new IllegalArgumentException(s"PlayerId '$id' not in game")
    }
  }

}

case class MulliganGameState(
  player1: MulliganPlayerState,
  player2: MulliganPlayerState,
  player1sTurn: Option[Boolean]
) extends GameState {

  def updateGameState(id: PlayerId, s: String): GameState = {
    (Json.fromJson[MulliganJson](Json.parse(s)) match {
      case JsSuccess(m: MulliganJson, _) => mulligan(id, m.card)
      case _: JsError =>
        Json.fromJson[TurnEvents](Json.parse(s)) match {
          case JsSuccess(te: TurnEvents, _) => updateWhoGoesFirst(id, te.events)
          case e: JsError => sys.error(e.toString)
        }
    }).checkMulliganComplete()
  }

  def mulligan(id: PlayerId, card: CardJson): MulliganGameState = {
    val newMulliganState = if (getPlayer(id).id == player1.id) {
      this.copy(player1 = player1.mulligan(card))
    } else {
      this.copy(player2 = player2.mulligan(card))
    }
    newMulliganState
  }

  private def checkMulliganComplete(): GameState = {
    if (player1.cardsMulliganed == 2 && player2.cardsMulliganed == 2) {
      if (player1sTurn.isEmpty &&
        (player1.faction == Scoiatael && player2.faction != Scoiatael ||
        player1.faction != Scoiatael && player2.faction == Scoiatael)) {
        this
      } else {
        val inGameState = InGameState(
          player1 = player1.endMulliganPhase(),
          player2 = player2.endMulliganPhase(),
          board = BoardState.empty,
          player1sTurn = player1sTurn.getOrElse(Random.nextBoolean)
        )
        inGameState.sendGameStart()
        inGameState
      }
    } else {
      this
    }
  }

  def updateWhoGoesFirst(playerId: PlayerId, events: List[TurnEvent]): MulliganGameState = {
    val player = if (playerId == player1.id) player1 else player2
    require(player.faction == Scoiatael)
    val p1sTurn = events.headOption match {
      case None => sys.error("Missing event in SelectWhoToPlayFirstState.")
      case Some(event) if event.cardId == "first" =>
        if (playerId == player1.id) true else false
      case Some(event) if event.cardId == "second" =>
        if (playerId == player1.id) false else true
      case Some(event) => sys.error(s"Invalid event id ${event.cardId} in SelectWhoToPlayFirstState")
    }
    copy(player1sTurn = Some(p1sTurn))
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
      ourLeader = toPlayer.leader,
      ourLeaderEnabled = toPlayer.leaderEnabled,
      theirLeader = otherPlayer.leader,
      theirLeaderEnabled = otherPlayer.leaderEnabled,
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
      case JsSuccess(e: TurnEvents, _) =>  updateGameState(id, e.events)
      case e: JsError => sys.error(e.toString)
    }
  }

  def updateGameState(playerId: PlayerId, events: List[TurnEvent]): InGameState = {
    val (player, otherPlayer) = if (getPlayer(playerId).id == player1.id) {
      (player1, player2)
    } else {
      (player2, player1)
    }
    val newGame = events.headOption match {
      case None => passMove(player)
      case Some(event) => nonPassMove(player, event, events.tail)
    }

    if (newGame.player1.passed && newGame.player2.passed) {
      println("Round end")
      roundEnd
    } else {
      val p1sTurn = if (mockGame) {
        player1sTurn
      } else {
        if (newGame.player1.passed && newGame.player2.passed) {
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
    handleWinner.clearBoard
  }

  private def handleWinner: InGameState = {
    // If a player wins, they go first next round.
    // In case of a tie, both players lose a life
    // and the player who passed first goes first
    // next round.
    roundWinner match {
      case Some(id) if id == player1.id =>
        copy(player2 = player2.loseALife, player1sTurn = true)
          .checkForGameWinner
          .copy(player1 = player1.northernRealmsFactionAbility)
      case Some(id) if id == player2.id =>
        copy(player1 = player1.loseALife, player1sTurn = false)
          .checkForGameWinner
          .copy(player2 = player2.northernRealmsFactionAbility)
      case Some(id) => sys.error(s"PlayerId $id not in game!")
      case None =>
        copy(player1 = player1.loseALife, player2 = player2.loseALife)
          .checkForGameWinner
    }
  }

  private def roundWinner: Option[PlayerId] = {
    val p1Score = board.melee1.score + board.ranged1.score + board.siege1.score
    val p2Score = board.melee2.score + board.ranged2.score + board.siege2.score
    if (p1Score > p2Score ||
      (p1Score == p2Score &&
      player1.faction == Nilfgaard &&
      player2.faction != Nilfgaard)) {
      Some(player1.id)
    } else if (p2Score > p1Score ||
      (p2Score == p1Score &&
      player2.faction == Nilfgaard &&
      player1.faction != Nilfgaard)) {
      Some(player2.id)
    } else {
      None
    }
  }

  private def checkForGameWinner: InGameState = {
    if (player1.life <= 0 || player2.life <= 0) {
      throw GameEndException(this)
    }
    this
  }

  private def clearBoard: InGameState = {
    val (p1CardRetained, p1) = player1.roundEndClearBoard(
      board.melee1.cards ++ board.ranged1.cards ++ board.siege1.cards
    )
    val (p2CardRetained, p2) = player2.roundEndClearBoard(
      board.melee2.cards ++ board.ranged2.cards ++ board.siege2.cards
    )
    val retainedCards = List(p1CardRetained, p2CardRetained).flatten
    copy(player1 = p1, player2 = p2, board = board.clearBoard(retainedCards))
  }

  private def passMove(player: InGamePlayerState): InGameState = {
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

  private def nonPassMove(
    player: InGamePlayerState,
    turn: TurnEvent,
    restOfEvents: List[TurnEvent]): InGameState = {
    val card = CardCollection.getCardById(turn.cardId)
    val newPlayer = player.copy(
      hand = player.hand.remove(card) // throws if card not in hand
    )
    resolveCardEffects(newPlayer, board, turn, restOfEvents)
  }

  private def resolveCardEffects(
    player: InGamePlayerState,
    _board: BoardState,
    turn: TurnEvent,
    restOfEvents: List[TurnEvent]): InGameState = {
    val card = CardCollection.getCardById(turn.cardId)
    val row = RowId.rowStringToId(turn.row, player.id == player1.id)
    var newBoard = _board
    var newPlayer = player
    if (card.hasMedic) {
      restOfEvents.headOption match {
        case None =>
        case Some(ev) =>
          val revivedCard = Card
          newPlayer = newPlayer.copy(discardPile = newPlayer.discardPile.remove(ev.cardId))
          val newGameState = resolveCardEffects(newPlayer, newBoard, ev, restOfEvents.tail)
          newBoard = newGameState.board
          newPlayer = if (player.id == newGameState.player1.id) newGameState.player1 else newGameState.player2
      }
    }
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

    if (card.isScorch) {
      // Don't add the Scorch card to the board like we would any other card,
      // instead, it goes directly to the discard pile.
      newPlayer = newPlayer.copy(
        discardPile = newPlayer.discardPile.add(card)
      )
      newPlayerAndBoardState(newPlayer, newBoard).scorch
    }
    // This section is inelegant - if we were adding custom cards, a card
    // could have more than one of ScorchMelee, ScorchRanged, or ScorchSiege.
    // Also, there's a fair bit of code repetition here. This area is a prime
    // target for refactoring in the future.
    else if (card.hasScorchMelee) {
      newBoard = newBoard.addCard(card, row)
      newPlayerAndBoardState(newPlayer, newBoard).scorchMelee(newPlayer.id)
    } else if (card.hasScorchRanged) {
      newBoard = newBoard.addCard(card, row)
      newPlayerAndBoardState(newPlayer, newBoard).scorchRanged(newPlayer.id)
    } else if (card.hasScorchSiege) {
      newBoard = newBoard.addCard(card, row)
      newPlayerAndBoardState(newPlayer, newBoard).scorchSiege(newPlayer.id)
    } else {
      newBoard = newBoard.addCard(card, row)
      newPlayerAndBoardState(newPlayer, newBoard)
    }
  }

  private def scorch: InGameState = {
    val p1Cards = (board.melee1.cards ++ board.ranged1.cards ++ board.siege1.cards).filter { card =>
      !card.hasHero
    }
    val p2Cards = (board.melee2.cards ++ board.ranged2.cards ++ board.siege2.cards).filter { card =>
      !card.hasHero
    }
    val allCards = p1Cards ++ p2Cards
    val highestPower = allCards.map { card =>
      card.currentPower.getOrElse(-1)
    }.reduceOption(_ max _)
    highestPower match {
      case None => this
      case Some(power) =>
        val highestPowerFunc = (c: Card) => c.currentPower == highestPower
        val newBoard = board.applyToAllRows((cr: CardRow) => cr.filter(!highestPowerFunc(_)))
        val newPlayer1 = player1.copy(
          discardPile = player1.discardPile.add(p1Cards.filter(highestPowerFunc))
        )
        val newPlayer2 = player2.copy(
          discardPile = player2.discardPile.add(p2Cards.filter(highestPowerFunc))
        )
        copy(
          player1 = newPlayer1,
          player2 = newPlayer2,
          board = newBoard
        )
    }
  }

  private def scorchMelee(playerId: PlayerId): InGameState = {
    if (playerId == player1.id) {
      val (newCardRow, newPlayer2) = scorchRow(board.melee2, player2)
      copy(
        board = board.copy(
          melee2 = newCardRow
        ),
        player2 = newPlayer2)
    } else {
      val (newCardRow, newPlayer1) = scorchRow(board.melee1, player1)
      copy(
        board = board.copy(
          melee1 = newCardRow
        ),
        player1 = newPlayer1)
    }
  }

  private def scorchRanged(playerId: PlayerId): InGameState = {
    if (playerId == player1.id) {
      val (newCardRow, newPlayer2) = scorchRow(board.ranged2, player2)
      copy(
        board = board.copy(
          ranged2 = newCardRow
        ),
        player2 = newPlayer2)
    } else {
      val (newCardRow, newPlayer1) = scorchRow(board.ranged1, player1)
      copy(
        board = board.copy(
          ranged1 = newCardRow
        ),
        player1 = newPlayer1)
    }
  }

  private def scorchSiege(playerId: PlayerId): InGameState = {
    if (playerId == player1.id) {
      val (newCardRow, newPlayer2) = scorchRow(board.siege2, player2)
      copy(
        board = board.copy(
          siege2 = newCardRow
        ),
        player2 = newPlayer2)
    } else {
      val (newCardRow, newPlayer1) = scorchRow(board.siege1, player1)
      copy(
        board = board.copy(
          siege1 = newCardRow
        ),
        player1 = newPlayer1)
    }
  }

  private def scorchRow(row: CardRow, ownerOfRow: InGamePlayerState): (CardRow, InGamePlayerState) = {
    if (row.cards.map { _.currentPower.getOrElse(0) }.sum >= 10) {
      val allCards = row.cards.filter { !_.hasHero }
      val highestPower = allCards.map { card =>
        card.currentPower.getOrElse(-1)
      }.reduceOption(_ max _)
      highestPower match {
        case None => (row, ownerOfRow)
        case Some(power) =>
          val highestPowerFunc = (c: Card) => c.currentPower == highestPower
          val newRow = row.filter(!highestPowerFunc(_))
          val newPlayer = ownerOfRow.copy(
            discardPile = ownerOfRow.discardPile.add(allCards.filter(highestPowerFunc))
          )
          (newRow, newPlayer)
      }
    } else {
      (row, ownerOfRow)
    }
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
