package com.duskeagle.freegwentserver.models

import akka.actor.ActorRef
import com.duskeagle.freegwentserver.actors.PlayerActor
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

import scala.collection.mutable.ListBuffer
import scala.util.Random

sealed trait PlayerState {
  val id: PlayerId
  val actor: ActorRef
  val faction: Faction
  val hand: Hand
  val deck: Deck
}

case class WaitingForMulligan(
  id: PlayerId,
  actor: ActorRef
) extends PlayerState {

  val faction = Neutral
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
    val leader = deck.cards.find { _.isLeader}
      .getOrElse(sys.error("No leader in deck!"))
    val nonLeaderCards = deck.cards.filter { !_.isLeader }
    val hand = Random.shuffle(nonLeaderCards).take(math.min(10, nonLeaderCards.length))
    val deckBuffer = nonLeaderCards.to[ListBuffer]
    hand.foreach { card =>
      deckBuffer -= card
    }
    val remainingDeck = deckBuffer.toList
    WaitingForOtherPlayer(
      id = id,
      actor = actor,
      faction = leader.faction,
      hand = Hand(hand),
      deck = Deck(remainingDeck),
      leader = leader
    )
  }
}

case class WaitingForOtherPlayer(
  id: PlayerId,
  actor: ActorRef,
  faction: Faction,
  hand: Hand,
  deck: Deck,
  leader: Card
) extends PlayerState {
  def playerFound(): MulliganPlayerState = {
    MulliganPlayerState(
      id,
      actor,
      faction,
      hand,
      deck,
      leader,
      cardsMulliganed = 0
    )
  }
}

case class MulliganPlayerState(
  id: PlayerId,
  actor: ActorRef,
  faction: Faction,
  hand: Hand,
  deck: Deck,
  leader: Card,
  cardsMulliganed: Int
) extends PlayerState {

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
        copy(
          hand = newHand,
          deck = newDeck,
          cardsMulliganed = cardsMulliganed + 1
        )
      } else {
        copy(cardsMulliganed = cardsMulliganed + 1)
      }
    }
  }

  def endMulliganPhase(): InGamePlayerState = {
    InGamePlayerState(
      id = id,
      actor = actor,
      faction = faction,
      hand = hand,
      deck = deck,
      leader = leader,
      discardPile = DiscardPile(Nil),
      leaderEnabled = true,
      life = 2,
      passed = false
    )
  }
}

case class InGamePlayerState(
  id: PlayerId,
  actor: ActorRef,
  faction: Faction,
  hand: Hand,
  deck: Deck,
  leader: Card,
  leaderEnabled: Boolean,
  discardPile: DiscardPile,
  life: Int,
  passed: Boolean
) extends PlayerState {

  def loseALife: InGamePlayerState = {
    copy(life = life - 1)
  }

  def northernRealmsFactionAbility: InGamePlayerState = {
    if (faction == NorthernRealms) {
      val (newCards, newDeck) = deck.draw(1)
      copy(hand = hand.add(newCards), deck = newDeck)
    } else {
      this
    }
  }

  def roundEndClearBoard(cardsOnOurBoard: List[Card]): (Option[Card], InGamePlayerState) = {
    val cardToRetain = if (faction == Monsters) {
      Random.shuffle(cardsOnOurBoard).take(1).headOption
    } else {
      None
    }
    (cardToRetain, copy(
      discardPile = discardPile.add(cardsOnOurBoard.diff(cardToRetain.toList)),
      passed = false
    ))
  }

}
