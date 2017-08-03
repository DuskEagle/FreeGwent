using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using FreeGwent;

abstract public class Card : MonoBehaviour {
    public String id;
    public List<CombatType> combatTypes;
    public List<CardAttribute> attributes;
    public int? basePower;
    public int? currentPower;
    public String faction;
    public Sprite image;

    protected GameObject cardObject;

    private static List<CombatType> unitTypes = new List<CombatType> { CombatType.Melee, CombatType.Ranged, CombatType.Siege };

    public Boolean IsUnitType() {
        if (id != Decoy.decoyId &&
            // combatTypes is subset of unitTypes
            !combatTypes.Except(Card.unitTypes).Any()) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean IsLeaderType() {
        return combatTypes.Contains(CombatType.Leader);
    }

    public Boolean IsSpecialType() {
        return !IsUnitType() && !IsLeaderType();
    }

    public Boolean IsScorch() {
        return combatTypes.Contains(CombatType.Scorch);
    }
}
