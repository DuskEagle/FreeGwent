package com.duskeagle.freegwentserver.models

import com.fasterxml.jackson.annotation.JsonIgnore
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class CardRow(
  cards: List[Card],
  horn: Option[Card]
) {

  /*
   * Return a new CardRow where each card has had its current power updated
   * based off of the rest of the cards in the row. Once weather is added,
   * we'll need to pass weather state into updateCardPower.
   */
  def updateCardPower(weatherAffected: Boolean): CardRow = {
    val (heroCards, nonHeroCards) = cards.partition { _.hasHero }
    val newNonHeroCards = applyHorn(
                          applyMoraleBoost(
                          applyTightBond(
                          applyWeather(weatherAffected,
                          resetCurrentPower(nonHeroCards)))))
    copy(cards = newNonHeroCards ++ heroCards)
  }

  private def resetCurrentPower(cards: List[Card]): List[Card] = {
    cards.map { card =>
      card.copy(currentPower = card.basePower)
    }
  }

  private def applyWeather(weatherAffected: Boolean, cards: List[Card]): List[Card] = {
    if (weatherAffected) {
      cards.map { card =>
        card.copy(currentPower = card.currentPower.map { p => 1 })
      }
    } else {
      cards
    }
  }

  private def applyTightBond(cards: List[Card]): List[Card] = {
    cards.groupBy { card =>
      card.tightBondId
    }.flatMap { case (bondIdOpt, bondIdCards) =>
      bondIdOpt match {
        case None => bondIdCards
        case Some(bondId) =>
          bondIdCards.map { card =>
            card.copy(currentPower = card.currentPower.map { p =>
              p * bondIdCards.length
            })
          }
      }
    }.toList
  }

  private def applyMoraleBoost(cards: List[Card]): List[Card] = {
    val moraleBoostCards = cards.filter { _.hasMoraleBoost }
    cards.map { card =>
      val increase = if (moraleBoostCards.contains(card)) {
        moraleBoostCards.length - 1
      } else {
        moraleBoostCards.length
      }
      card.copy(currentPower = card.currentPower.map { p =>
        p + increase
      })
    }
  }

  private def applyHorn(cards: List[Card]): List[Card] = {
    horn match {
      case Some(_) =>
        cards.map { card =>
          card.copy(currentPower = card.currentPower.map { p => p * 2 })
        }
      case None => applyHornFromCombatCard(cards)
    }
  }

  /*
   * Apply a horn that exists on a non-commander's horn card, such as
   * Dandelion. This only gets applied if a Commander's Horn is not
   * already present on a row. A horn card of this type applies to all
   * cards on the row except itself. Multiple horn cards on a row do
   * not stack, but will have the effect of applying their effect
   * to the other horn cards on the row.
   *
   * For example, say on a row you have Dandelion (2 power horn) and 2
   * Mahakaman Dwarfs (5 power card with no abilities). This row would
   * have 22 power (5*2 + 5*2 + 2). If you added a second Dandelion, the
   * row would then have 28 power (5*2 + 5*2 + 2*2 + 2*2). If instead
   * of the second Dandelion you had added a Commander's Horn, the row
   * would have 24 power (5*2 + 5*2 + 2*2).
   */
  private def applyHornFromCombatCard(cards: List[Card]): List[Card] = {
    val hornCount = cards.count { _.hasHorn }
    if (hornCount == 0) {
      cards
    } else if (hornCount == 1) {
      cards.map { card =>
        val cardPower = if (card.hasHorn) {
          card.currentPower
        } else {
          card.currentPower.map { _ * 2 }
        }
        card.copy(currentPower = cardPower)
      }
    } else {
      cards.map { card =>
        card.copy(currentPower = card.currentPower.map { _ * 2 })
      }
    }
  }

}

object CardRow {

  implicit val format = Json.format[CardRow]

  def empty: CardRow = {
    CardRow(
      cards = Nil,
      horn = None
    )
  }

}
