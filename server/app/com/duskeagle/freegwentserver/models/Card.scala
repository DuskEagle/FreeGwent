package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

/**
  *
  * @param id
  * @param basePower
  * @param currentPower
  * @param combatTypes
  * @param attributes
  * @param faction
  * @param musterId
  * @param tightBondId
  * @param reviveRow The row to revive this card on, should a medic be played
  *                  and this card chosen. While most units could just be
  *                  revived onto the one row they fit on, in the case of
  *                  agile units, they get revived onto the row they were
  *                  last on. This will be set to None before the card is
  *                  played, and won't be set to a defined value on non-combat
  *                  cards.
  */
case class Card(
  id: String,
  basePower: Option[Int],
  currentPower: Option[Int],
  combatTypes: List[CombatType],
  attributes: List[CardAttribute],
  faction: Faction,
  musterId: Option[String],
  tightBondId: Option[String],
  reviveRow: Option[CombatType]
) {

  def isCommandersHorn: Boolean = {
    combatTypes.contains(HornType)
  }

  def isScorch: Boolean = {
    combatTypes.contains(Scorch)
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

  def hasMedic: Boolean = {
    attributes.contains(Medic)
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
