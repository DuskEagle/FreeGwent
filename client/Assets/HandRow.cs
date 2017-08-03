using System;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

public class HandRow : CardRow {
    
    public HandRow() {
        this.combatType = CombatType.Hand;
    }

    public int GetHandSize() {
        return cards.Count;
    }

    public void Scorch(InGameCard card) {
        gwn.SendTurn(AddCard(card, true));
    }

}
