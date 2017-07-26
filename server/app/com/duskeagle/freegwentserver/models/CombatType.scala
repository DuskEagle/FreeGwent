package com.duskeagle.freegwentserver.models

import java.util.Locale

import play.api.libs.json._

sealed trait CombatType { val name: String }
case object Melee extends CombatType { val name = "melee" }
case object Ranged extends CombatType { val name = "ranged" }
case object Siege extends CombatType { val name = "siege" }
case object Leader extends CombatType { val name = "leader" }
case object HornType extends CombatType { val name = "horn" }
case object Weather extends CombatType { val name = "weather" }
case object Scorch extends CombatType { val name = "scorch" }

object CombatType {

  val utf8 = new Locale("UTF-8")

  def apply(ct: String): CombatType = {
    ct.toLowerCase(utf8) match {
      case Melee.name => Melee
      case Ranged.name => Ranged
      case Siege.name => Siege
      case Leader.name => Leader
      case HornType.name => HornType
      case Weather.name => Weather
      case Scorch.name => Scorch
      case _ => sys.error(s"$ct is not a CombatType")
    }
  }

  implicit val reads = new Reads[CombatType] {
    def reads(jsValue: JsValue) = new JsSuccess[CombatType](CombatType(jsValue.toString))
  }
  implicit val writes = new Writes[CombatType] {
    def writes(ct: CombatType) = JsString(ct.name)
  }
}
