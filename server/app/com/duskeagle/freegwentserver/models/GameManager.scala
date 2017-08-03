package com.duskeagle.freegwentserver.models

import akka.actor.ActorRef

object GameManager {

  var waitingActor: Option[ActorRef] = None
  var waitingPlayerState: Option[WaitingForOtherPlayer] = None
  var waitingGame: Option[GameStateWrapper] = None

  private[this] val utf8 = "UTF-8"
  private[this] val playerFound = "player found".getBytes(utf8)

  def createGame(actor: ActorRef, playerState: WaitingForOtherPlayer): GameStateWrapper = {
    this.synchronized {
      (waitingActor, waitingPlayerState, waitingGame) match {
        case (Some(wActor), Some(wPlayerState), Some(game)) =>
          println("Player was waiting")
          game.startGame(
            wPlayerState.playerFound(),
            playerState.playerFound()
          )
          wActor.tell(playerFound, actor)
          actor.tell(playerFound, actor)
          // Comment out for testing
          waitingActor = None
          waitingPlayerState = None
          waitingGame = None

          game
        case _ =>
          waitingActor = Some(actor)
          waitingPlayerState = Some(playerState)
          println("waiting for another player")
          val game = new GameStateWrapper()
          waitingGame = Some(game)
          game
      }
    }

  }

  def mockGame(actor: ActorRef): GameStateWrapper = {
    val mockCards = CardCollection.cards.filter { card =>
      card.faction == Nilfgaard && card.combatTypes.contains(Melee)
    }.take(2) :+ CardCollection.getCardById("horn") :+
      CardCollection.frost :+
      CardCollection.clearWeather :+
      CardCollection.getCardById("scorch")
    val mockHand = mockCards :+
      CardCollection.getCardById("arachas2")
    val mockDeck = mockCards :+
      CardCollection.getCardById("arachas3") :+
      CardCollection.getCardById("arachasbehemoth")
    val playerStates = (0 to 1).map { i =>
      val path = if (i == 0) {
        actor.path / "0"
      } else {
        actor.path
      }
      InGamePlayerState(
        id = PlayerId(path),
        actor = actor,
        hand = Hand(mockHand),
        deck = Deck(mockDeck),
        discardPile = DiscardPile(Nil),
        life = 2,
        passed = false
      )
    }
    val inGameState = InGameState(
      player1 = playerStates(0),
      player2 = playerStates(1),
      board = BoardState.empty,
      player1sTurn = false,
      mockGame = true
    )
    val wrapper = new GameStateWrapper()
    wrapper.startMockGame(inGameState)
    wrapper
  }

}
