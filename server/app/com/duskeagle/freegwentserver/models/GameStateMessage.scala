package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class GameStateMessage(
  board: BoardState,
  hand: List[Card],
  ourDiscardPile: List[Card],
  theirDiscardPile: List[Card],
  ourLife: Int,
  theirLife: Int,
  theirHandCount: Int,
  ourDeckCount: Int,
  theirDeckCount: Int,
  ourTurn: Boolean
)

object GameStateMessage {
  implicit val format = Json.format[GameStateMessage]
}
