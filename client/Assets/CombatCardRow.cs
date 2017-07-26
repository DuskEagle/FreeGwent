using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using FreeGwent;

public class CombatCardRow : CardRow {

    public int GetScore() {
        // Will eventually need to do something fancier here to account
        // for card synergy effects.
        return cards.Aggregate(0, (v, c) => v + c.currentPower.GetValueOrDefault(0));
    }

}
