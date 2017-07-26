package com.duskeagle.freegwentserver.actors

import akka.actor._
import play.api.libs.json.Json
import com.duskeagle.freegwentserver.models.{CardCollection, CardCollectionSerialized, GameManager, GameStateWrapper, InGameState, PlayerId, PlayerState, WaitingForMulligan}

object PlayerActor {
  def props(out: ActorRef) = Props(new PlayerActor(out))
  val useMockGame = false
}

class PlayerActor(out: ActorRef) extends Actor {
  val playerId = PlayerId(self.path)
  private var playerState: PlayerState = WaitingForMulligan(playerId, self)
  private val initialPlayerState = playerState match {
    case state: WaitingForMulligan => state
    case _ => sys.error("Player state can only be WaitingForMulligan here")
  }
  private var gameOpt: Option[GameStateWrapper] = None
  private val utf8 = "UTF-8"

  private val collection = Json.toJson[CardCollectionSerialized](
    CardCollectionSerialized(CardCollection.cards)
  )

  if (PlayerActor.useMockGame) {
    val game = GameManager.mockGame(self)
    gameOpt = Some(game)
    println("Using mock game")
    game.receive(playerId, "game start").foreach { response =>
      send(response)
    }
  } else {
    send(collection.toString)
  }

  def receive = {
    case msg: Array[Byte] => // A message from client to server
      val s = new String(msg, utf8)
      println("Received message " + s)
      gameOpt match {
        case None if !PlayerActor.useMockGame =>
          val waitingForOtherPlayer = initialPlayerState.receiveDeck(s)
          gameOpt = Some(GameManager.createGame(self, waitingForOtherPlayer))
        case None => sys.error(s"Unexpected message $s while using mock game")
        case Some(game) =>
          game.receive(playerId, s).foreach { response =>
            send(response)
          }
      }
    case gameState: InGameState => // A message from server back to client.
      send(gameState.gameStateMessageString(playerId))
    case n: Any =>
      sys.error(s"Unexpected message format: ${n.getClass.toString}")
  }

  def send(message: String): Unit = {
    out ! message.getBytes(utf8)
  }

  def illegalStateError(state: PlayerState): Unit = {
    sys.error(s"Game in illegal state ${state.getClass.getSimpleName}")
  }
}
