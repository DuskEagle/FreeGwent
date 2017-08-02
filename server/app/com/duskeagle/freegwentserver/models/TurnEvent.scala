package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class TurnEvent(
  cardId: String,
  row: String
)

object TurnEvent {
  implicit val format = Json.format[TurnEvent]
}
