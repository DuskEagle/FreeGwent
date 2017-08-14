package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class MulliganResponse(
  cards: List[Card],
  selectWhoGoesFirst: Boolean
)

object MulliganResponse {
  implicit val format = Json.format[MulliganResponse]

  def getResponseFromGameState(state: GameState, playerId: PlayerId): MulliganResponse = {
    val selectWhoGoesFirst = if (state.getPlayer(playerId).faction == Scoiatael &&
      state.getOtherPlayer(playerId).faction != Scoiatael) {
      true
    } else {
      false
    }
    MulliganResponse(
      cards = state.getPlayer(playerId).hand.cards,
      selectWhoGoesFirst = selectWhoGoesFirst
    )
  }

}
