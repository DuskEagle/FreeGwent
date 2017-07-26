package com.duskeagle.freegwentserver.models

class CombatCardRow(combatType: CombatType) {

}

class MeleeCardRow extends CombatCardRow(Melee)
class RangedCardRow extends CombatCardRow(Ranged)
class SiegeCardRow extends CombatCardRow(Siege)
