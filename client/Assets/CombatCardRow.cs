using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using FreeGwent;

public class CombatCardRow : CardRow {

    public int GetScore() {
        return cards.Aggregate(0, (v, c) => v + c.currentPower.GetValueOrDefault(0));
    }

}
