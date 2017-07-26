package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

sealed trait RowId
case class p1m() extends RowId
case class p2m() extends RowId
case class p1r() extends RowId
case class p2r() extends RowId
case class p1s() extends RowId
case class p2s() extends RowId
case class wea() extends RowId

object RowId {

  val theirSiegeString = "Their Siege"
  val theirRangedString = "Their Ranged"
  val theirMeleeString = "Their Melee"
  val ourMeleeString = "Our Melee"
  val ourRangedString = "Our Ranged"
  val ourSiegeString = "Our Siege"
  val weather = "Weather"

  /*
   * Take a row string from the player of the form, e.g., "Our Melee", and
   * convert it to a RowId
   */
  def rowStringToId(rowString: String, thisIsPlayer1: Boolean): RowId = {
    (rowString, thisIsPlayer1) match {
      case (`theirMeleeString`, true) => p2m()
      case (`theirMeleeString`, false) => p1m()
      case (`theirRangedString`, true) => p2r()
      case (`theirRangedString`, false) => p1r()
      case (`theirSiegeString`, true) => p2s()
      case (`theirSiegeString`, false) => p1s()
      case (`ourMeleeString`, true) => p1m()
      case (`ourMeleeString`, false) => p2m()
      case (`ourRangedString`, true) => p1r()
      case (`ourRangedString`, false) => p2r()
      case (`ourSiegeString`, true) => p1s()
      case (`ourSiegeString`, false) => p2s()
      case (`weather`, _) => wea()
      case (s: String, _) => sys.error(s"Unrecognized row string: $s")
    }
  }

  /*
   * Convert a CombatType to a RowId from this player's persepctive.
   * Must negate `thisIsPlayer1` in the case of spy units.
   */
  def combatTypeToId(combatType: CombatType, thisIsPlayer1: Boolean): RowId = {
    (combatType, thisIsPlayer1) match {
      case (Melee, true) => p1m()
      case (Melee, false) => p2m()
      case (Ranged, true) => p1r()
      case (Ranged, false) => p2r()
      case (Siege, true) => p1s()
      case (Siege, false) => p2s()
      case (Weather, _) => wea()
      case _ => sys.error(s"Unsupported combat type $combatType")
    }
  }

}

case class RowIdException(
  message: String
) extends Exception


