package com.duskeagle.freegwentserver.models

import play.api.libs.json.Json

case class BoardState(
  siege1: CardRow,
  ranged1: CardRow,
  melee1: CardRow,
  melee2: CardRow,
  ranged2: CardRow,
  siege2: CardRow,
  weather: List[Card]
) {

  def flip: BoardState = {
    copy(
      siege1 = siege2,
      ranged1 = ranged2,
      melee1 = melee2,
      melee2 = melee1,
      ranged2 = ranged1,
      siege2 = siege1
    )
  }

  def addCard(card: Card, row: RowId): BoardState = {
    val tempState = if (card.isCommandersHorn) {
      addHornCard(card, row)
    } else if (card.isWeather) {
      addWeatherCard(card)
    } else {
      addCombatCard(card, row)
    }
    tempState.updateCardPower
  }

  private def addHornCard(card: Card, row: RowId): BoardState = {
    row match {
      case _: p1s => copy(siege1 = addHornToRow(card, siege1))
      case _: p1r => copy(ranged1 = addHornToRow(card, ranged1))
      case _: p1m => copy(melee1 = addHornToRow(card, melee1))
      case _: p2m => copy(melee2 = addHornToRow(card, melee2))
      case _: p2r => copy(ranged2 = addHornToRow(card, ranged2))
      case _: p2s => copy(siege2 = addHornToRow(card, siege2))
      case _: wea =>
        throw IllegalMoveException(s"Attempt to add card ${card.id} to weather row")
    }
  }

  private def addHornToRow(card: Card, row: CardRow): CardRow = {
    row.horn match {
      case Some(c) => throw IllegalMoveException(s"Horn already existed on row $row")
      case None => row.copy(horn = Some(card))
    }
  }

  private def addWeatherCard(card: Card): BoardState = {
    if (card == CardCollection.clearWeather) {
      copy(weather = Nil)
    } else if (!weather.contains(card)) {
      copy(weather = weather :+ card)
    } else {
      throw IllegalMoveException(s"${card.id} already exists in weather row")
    }
  }

  private def addCombatCard(card: Card, row: RowId): BoardState = {
    row match {
      case _: p1s => copy(siege1 = siege1.copy(cards = siege1.cards :+ card ))
      case _: p1r => copy(ranged1 = ranged1.copy(cards = ranged1.cards :+ card ))
      case _: p1m => copy(melee1 = melee1.copy(cards = melee1.cards :+ card ))
      case _: p2m => copy(melee2 = melee2.copy(cards = melee2.cards :+ card ))
      case _: p2r => copy(ranged2 = ranged2.copy(cards = ranged2.cards :+ card ))
      case _: p2s => copy(siege2 = siege2.copy(cards = siege2.cards :+ card ))
      case _: wea =>
        throw IllegalMoveException(s"Attempt to add card ${card.id} to weather row")
    }
  }

  private def updateCardPower: BoardState = {
    copy(
      siege1 = siege1.updateCardPower(weather.contains(CardCollection.rain)),
      ranged1 = ranged1.updateCardPower(weather.contains(CardCollection.fog)),
      melee1 = melee1.updateCardPower(weather.contains(CardCollection.frost)),
      melee2 = melee2.updateCardPower(weather.contains(CardCollection.frost)),
      ranged2 = ranged2.updateCardPower(weather.contains(CardCollection.fog)),
      siege2 = siege2.updateCardPower(weather.contains(CardCollection.rain))
    )
  }
}

object BoardState {

  implicit val format = Json.format[BoardState]

  def empty: BoardState = {
    BoardState(
      siege1 = CardRow.empty,
      ranged1 = CardRow.empty,
      melee1 = CardRow.empty,
      melee2 = CardRow.empty,
      ranged2 = CardRow.empty,
      siege2 = CardRow.empty,
      weather = Nil
    )
  }

}
