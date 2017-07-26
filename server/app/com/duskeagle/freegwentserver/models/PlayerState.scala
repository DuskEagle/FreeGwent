package com.duskeagle.freegwentserver.models

import akka.actor.ActorRef
import com.duskeagle.freegwentserver.actors.PlayerActor
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

import scala.collection.mutable.ListBuffer
import scala.util.Random

sealed trait PlayerState {
  val id: PlayerId
  val actor: ActorRef
  val hand: Hand
  val deck: Deck
}

case class WaitingForMulligan(
  id: PlayerId,
  actor: ActorRef
) extends PlayerState {

  val hand = Hand(Nil)
  val deck = Deck(Nil)

  def receiveDeck(deckStr: String): WaitingForOtherPlayer = {

    val deck: DeckJson = Json.fromJson[DeckJson](Json.parse(deckStr)) match {
      case JsSuccess(d: DeckJson, path: JsPath) => d
      case e: JsError => sys.error(e.toString)
    }
    val cards = deck.cards.map { card =>
      CardCollection.getCardById(card.id)
    }
    this.receiveDeck(Deck(cards))
  }

  def receiveDeck(deck: Deck): WaitingForOtherPlayer = {
    println("Received deck: " + deck.cards.mkString(", "))
    /* TODO: Verify that deck.cards is a subset of CardCollection.cards,
     * and that deck has correct amount of unit, special, and leader types.
     */
    val hand = Random.shuffle(deck.cards).take(math.min(10, deck.cards.length))
    val deckBuffer = deck.cards.to[ListBuffer]
    hand.foreach { card =>
      deckBuffer -= card;
    }
    val remainingDeck = deckBuffer.toList
    WaitingForOtherPlayer(
      id = id,
      actor = actor,
      hand = Hand(hand),
      deck = Deck(remainingDeck)
    )
  }
}

case class WaitingForOtherPlayer(
  id: PlayerId,
  actor: ActorRef,
  hand: Hand,
  deck: Deck
) extends PlayerState {
  def playerFound(): MulliganPlayerState = {
    MulliganPlayerState(
      id,
      actor,
      hand,
      deck,
      cardsMulliganed = 0
    )
  }
}

case class MulliganPlayerState(
  id: PlayerId,
  actor: ActorRef,
  hand: Hand,
  deck: Deck,
  cardsMulliganed: Int
) extends PlayerState {

  def mulligan(str: String): MulliganPlayerState = {
    Json.fromJson[MulliganJson](Json.parse(str)) match {
      case JsSuccess(m: MulliganJson, _) => mulligan(m.card)
      case e: JsError => sys.error(e.toString)
    }
  }

  def mulligan(cardJson: CardJson): MulliganPlayerState = {
    val card = CardCollection.getCardById(cardJson.id)
    if (cardsMulliganed >= 2) {
      sys.error(s"Player $id already mulliganed max number of cards")
    } else if (!hand.contains(card)) {
      sys.error(s"$card is not in hand for mulligan")
    } else {
      if (deck.cards.nonEmpty) {
        val newCard = deck.cards(Random.nextInt(deck.cards.length))
        val newHand = hand.copy(
          cards = newCard :: hand.cards.diff(List(card))
        )
        val newDeck = deck.copy(
          cards = card :: deck.cards.diff(List(newCard))
        )
        MulliganPlayerState(id, actor, newHand, newDeck, cardsMulliganed + 1)
      } else {
        MulliganPlayerState(id, actor, hand, deck, cardsMulliganed + 1)
      }
    }
  }

  def endMulliganPhase(): InGamePlayerState = InGamePlayerState(
    id = id,
    actor = actor,
    hand = hand,
    deck = deck,
    passed = false
  )
}

case class InGamePlayerState(
  id: PlayerId,
  actor: ActorRef,
  hand: Hand,
  deck: Deck,
  passed: Boolean
) extends PlayerState
