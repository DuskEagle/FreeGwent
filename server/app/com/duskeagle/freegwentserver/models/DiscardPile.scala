package com.duskeagle.freegwentserver.models

case class DiscardPile(
  cards: List[Card]
) {
  def add(cardsToAdd: List[Card]): DiscardPile = {
    copy(cards = cards ++ cardsToAdd)
  }
}
