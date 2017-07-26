package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class CardJson(
  id: String
)
object CardJson {
  // Boilerplate for Play Json handling.
  // Ideally I wouldn't have to repeat this for every serializable case class,
  // but I haven't figured out how to avoid needing to do this yet.
  implicit val format = Json.format[CardJson]
}

case class DeckJson(
  state: String,
  cards: List[CardJson]
)
object DeckJson {
  implicit val format = Json.format[DeckJson]
}

case class MulliganJson(
  state: String,
  card: CardJson
)
object MulliganJson {
  implicit val format = Json.format[MulliganJson]
}