package com.duskeagle.freegwentserver.models

case class DiscardPile(
  cards: List[Card]
) {
  def contains(card: Card): Boolean = cards.contains(card)
  def add(cardsToAdd: List[Card]): DiscardPile = {
    copy(cards = cards ++ cardsToAdd)
  }
  def remove(cardId: String): DiscardPile = {
    cards.find { card =>
      card.id == cardId
    }.map { card =>
      copy(cards = cards.diff(List(card)))
    }.getOrElse(
      throw IllegalMoveException(s"$cardId not in discard pile")
    )
  }
}
