package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class TurnEvents(
  events: List[TurnEvent]
)

object TurnEvents {
  implicit val format = Json.format[TurnEvents]
}
