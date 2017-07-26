package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class Card(
  id: String,
  basePower: Option[Int],
  currentPower: Option[Int],
  combatTypes: List[CombatType],
  attributes: List[CardAttribute],
  faction: Faction,
  musterId: Option[String],
  tightBondId: Option[String]
) {

  def isCommandersHorn: Boolean = {
    combatTypes.contains(HornType)
  }

  def isWeather: Boolean = {
    combatTypes.contains(Weather)
  }

  def hasHero: Boolean = {
    attributes.contains(Hero)
  }

  def hasSpy: Boolean = {
    attributes.contains(Spy)
  }

  def hasMoraleBoost: Boolean = {
    attributes.contains(MoraleBoost)
  }

  def hasHorn: Boolean = {
    attributes.contains(HornAttr)
  }

  def hasMuster: Boolean = {
    attributes.contains(Muster)
  }

}

object Card {
  implicit val format = Json.format[Card]
}

case class CardConfig(
  id: String,
  basePower: Option[Int],
  combatTypes: List[String],
  attributes: List[String],
  faction: String,
  musterId: Option[String],
  tightBondId: Option[String],
  copies: Option[Int]
)
object CardConfig {
  implicit val format = Json.format[CardConfig]
}
