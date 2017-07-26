package com.duskeagle.freegwentserver.models

import play.Play
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.io.Source

object CardCollection {
  val cards = Play.application.getFile("conf/cards").listFiles.flatMap { file =>
    if (!file.getName.endsWith("json")) {
      None
    } else {
      val cardConfig = Json.fromJson[CardConfig](Json.parse(Source.fromFile(file).mkString)) match {
      case JsSuccess(c, path) => c
      case e: JsError => sys.error(s"Could not parse card file ${file.getName}")
      }
      (0 until cardConfig.copies.getOrElse(1)).map { i =>
        Card(
          id = cardConfig.id,
          basePower = cardConfig.basePower,
          currentPower = cardConfig.basePower,
          combatTypes = cardConfig.combatTypes.map { CombatType(_) },
          attributes = cardConfig.attributes.map { CardAttribute(_) },
          faction = Faction(cardConfig.faction),
          musterId = cardConfig.musterId,
          tightBondId = cardConfig.tightBondId
        )
      }
    }
  }.toList

  val getCardById = cards.map { card =>
    card.id -> card
  }.toMap

  val clearWeather = getCardById("clearweather")
  val frost = getCardById("bitingfrost") // Melee
  val fog = getCardById("impenetrablefog") // Ranged
  val rain = getCardById("torrentialrain") // Siege
}

case class CardCollectionSerialized(
  cards: List[Card]
)
object CardCollectionSerialized {
  implicit val format = Json.format[CardCollectionSerialized]
}
