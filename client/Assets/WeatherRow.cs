using System;
using System.Linq;
using UnityEngine;

public class WeatherRow : CardRow {
    
    override protected Boolean CanAddCard(InGameCard card) {
        return card != null &&
            card.combatTypes.Contains(this.combatType) &&
            !this.cards.Select(c => c.id).Contains(card.id);
    }

}
