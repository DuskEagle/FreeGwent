using System;
using System.Collections.Generic;
using UnityEngine;
using FreeGwent;

public class HandRow : CardRow {
    
    public HandRow() {
        this.combatType = CombatType.Hand;
    }

    public int GetHandSize() {
        return cards.Count;
    }

}
