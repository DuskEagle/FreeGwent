package com.duskeagle.freegwentserver.models

import java.util.Locale

import play.api.libs.json.{JsString, JsSuccess, JsValue, Reads, Writes}

sealed trait CardAttribute { val name: String }
case object Hero extends CardAttribute { val name = "hero" }
case object HornAttr extends CardAttribute { val name = "horn" }
case object Decoy extends CardAttribute { val name = "decoy" }
case object Muster extends CardAttribute { val name = "muster" }
case object MoraleBoost extends CardAttribute { val name = "moraleBoost" }
case object TightBond extends CardAttribute { val name = "tightBond" }
case object Medic extends CardAttribute { val name = "medic" }
case object Spy extends CardAttribute { val name = "spy" }
case object ScorchMelee extends CardAttribute { val name = "scorchMelee" }
case object ScorchRanged extends CardAttribute { val name = "scorchRanged" }
case object ScorchSiege extends CardAttribute { val name = "scorchSiege" }

object CardAttribute {

  val utf8 = new Locale("UTF-8")

  def apply(attr: String): CardAttribute = {
    attr match {
      case Hero.name => Hero
      case HornAttr.name => HornAttr
      case Decoy.name => Decoy
      case Muster.name => Muster
      case MoraleBoost.name => MoraleBoost
      case TightBond.name => TightBond
      case Medic.name => Medic
      case Spy.name => Spy
      case ScorchMelee.name => ScorchMelee
      case ScorchRanged.name => ScorchRanged
      case ScorchSiege.name => ScorchSiege
      case _ => sys.error(s"$attr is not a CardAttribute")
    }
  }

  implicit val reads = new Reads[CardAttribute] {
    def reads(jsValue: JsValue) = new JsSuccess[CardAttribute](CardAttribute(jsValue.toString))
  }
  implicit val writes = new Writes[CardAttribute] {
    def writes(attr: CardAttribute) = JsString(attr.name)
  }

}
