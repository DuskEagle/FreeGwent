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

  def applyToRow(row: RowId, f: CardRow => CardRow): BoardState = {
    row match {
      case _: p1s => copy(siege1 = f(siege1))
      case _: p1r => copy(ranged1 = f(ranged1))
      case _: p1m => copy(melee1 = f(melee1))
      case _: p2m => copy(melee2 = f(melee2))
      case _: p2r => copy(ranged2 = f(ranged2))
      case _: p2s => copy(siege2 = f(siege2))
      case _ => throw IllegalMoveException(s"Illegal row $row")
    }
  }

  def applyToAllRows(f: CardRow => CardRow): BoardState = {
    copy(
      siege1 = f(siege1),
      ranged1 = f(ranged1),
      melee1 = f(melee1),
      melee2 = f(melee2),
      ranged2 = f(ranged2),
      siege2 = f(siege2)
    )
  }

  private def addHornCard(card: Card, row: RowId): BoardState = {
    applyToRow(row, (cr: CardRow) => addHornToRow(card, cr))
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
    applyToRow(row, (cr: CardRow) =>
      cr.copy(cards = cr.cards :+ card.copy(reviveRow = Some(cr.combatType)))
    )
  }

  def removeCard(cardId: String, row: RowId): BoardState = {
    applyToRow(row, (cr: CardRow) => cr.remove(cardId))
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

  def clearBoard(retainCards: List[Card] = Nil): BoardState = {
    val clearRow = (cr: CardRow) => cr.filter((card: Card) => retainCards.contains(card))
    applyToAllRows(clearRow).updateCardPower
  }
}

object BoardState {

  implicit val format = Json.format[BoardState]

  def empty: BoardState = {
    BoardState(
      siege1 = CardRow.empty(Siege),
      ranged1 = CardRow.empty(Ranged),
      melee1 = CardRow.empty(Melee),
      melee2 = CardRow.empty(Melee),
      ranged2 = CardRow.empty(Ranged),
      siege2 = CardRow.empty(Siege),
      weather = Nil
    )
  }

}
