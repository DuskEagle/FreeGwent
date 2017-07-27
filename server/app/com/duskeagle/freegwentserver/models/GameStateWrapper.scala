package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

class GameStateWrapper() {
  private var stateOpt: Option[GameState] = None

  def receive(id: PlayerId, message: String): Option[String] = {
    this.synchronized {
      stateOpt match {
        case None => sys.error("state has not been instantiated yet")
        case Some(state) =>
          state match {
            case gameState: MulliganGameState if message == "player found" =>
              // This is a bad way to do it
              Some(Json.toJson[Hand](gameState.getPlayer(id).hand).toString)
            case gameState: MulliganGameState =>
              val newState = gameState.mulligan(id, message)
              stateOpt = Some(newState)
              Some(Json.toJson[Hand](newState.getPlayer(id).hand).toString)
            case gameState: InGameState if message == "game start" =>
              Some(gameState.gameStateMessageString(id))
            case gameState: InGameState =>
              //Update gameState here, if it's this player's turn
              val newState = gameState.updateGameState(id, message)
              stateOpt = Some(newState)
              if (newState.mockGame) {
                newState.player2.actor ! newState
              } else {
                newState.player1.actor ! newState
                newState.player2.actor ! newState
              }
              None
          }
      }
    }
  }

  def startGame(player1: MulliganPlayerState, player2: MulliganPlayerState): Unit = {
    stateOpt = Some(MulliganGameState(player1, player2))
  }

  def startMockGame(inGameState: InGameState): Unit = {
    stateOpt = Some(inGameState)
  }
}
