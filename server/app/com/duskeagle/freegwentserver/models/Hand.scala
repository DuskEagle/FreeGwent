package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class Hand(
  cards: List[Card]
) {
  def contains(card: Card): Boolean = cards.contains(card)
  def contains(card: String): Boolean = contains(CardCollection.getCardById(card))
  def remove(card: Card): Hand = {
    if (contains(card)) {
      copy(cards = cards.diff(List(card)))
    } else {
      throw IllegalMoveException(s"${card.id} not in hand")
    }
  }
  def remove(card: String): Hand = remove(CardCollection.getCardById(card))
  def add(card: Card): Hand = add(List(card))
  def add(cardsToAdd: List[Card]): Hand = {
    copy(cards = cards ++ cardsToAdd)
  }

  def muster(musterId: Option[String]): (List[Card], Hand) = {
    val drawnCards = cards.filter { musterId.isDefined && _.musterId == musterId }
    (drawnCards, copy(cards = cards.diff(drawnCards)))
  }

}

object Hand {
  implicit val format = Json.format[Hand]
}