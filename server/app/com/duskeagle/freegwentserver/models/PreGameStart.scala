package com.duskeagle.freegwentserver.models

sealed trait PreGameState

case class GameNotStarted(
  player: WaitingForMulligan
) extends PreGameState

case class WaitingForPlayersGameState(
  player: WaitingForOtherPlayer
) extends PreGameState
