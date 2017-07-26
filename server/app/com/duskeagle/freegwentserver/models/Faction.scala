package com.duskeagle.freegwentserver.models

import java.util.Locale

import play.api.libs.json._

sealed trait Faction { val name: String }
case object Monsters extends Faction { val name = "monsters" }
case object Neutral extends Faction { val name = "neutral" }
case object Nilfgaard extends Faction { val name = "nilfgaard" }
case object NorthernRealms extends Faction { val name = "northernrealms" }
case object Scoiatael extends Faction { val name = "scoiatael" }

object Faction {

  val utf8 = new Locale("UTF-8")

  def apply(s: String): Faction = {
    s.toLowerCase(utf8) match {
      case Monsters.name => Monsters
      case Neutral.name => Neutral
      case Nilfgaard.name => Nilfgaard
      case NorthernRealms.name => NorthernRealms
      case Scoiatael.name => Scoiatael
      case _ => sys.error(s"$s is not a faction")
    }
  }

  implicit val reads = new Reads[Faction] {
    def reads(jsValue: JsValue) = new JsSuccess[Faction](Faction(jsValue.toString))
  }
  implicit val writes = new Writes[Faction] {
    def writes(f: Faction) = JsString(f.name)
  }
}
