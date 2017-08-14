package com.duskeagle.freegwentserver.models

case class GameEndException(
  gameState: InGameState
) extends Exception
