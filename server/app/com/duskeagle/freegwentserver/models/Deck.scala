package com.duskeagle.freegwentserver.models

import scala.util.Random

case class Deck(
  cards: List[Card]
) {

  def draw(n: Int): (List[Card], Deck) = {
    val drawnCards = Random.shuffle(cards).take(n)
    (drawnCards, copy(cards = cards.diff(drawnCards)))
  }

  def muster(musterId: Option[String]): (List[Card], Deck) = {
    val drawnCards = cards.filter { musterId.isDefined && _.musterId == musterId }
    (drawnCards, copy(cards = cards.diff(drawnCards)))
  }

}
