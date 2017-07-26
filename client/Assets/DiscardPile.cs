using System;

public class DiscardPile : CardRow {
    
    override public void AddCard(InGameCard card, Boolean skipCheck = false) {
        cards.Add(card);
        UpdateCardOrder();
    }

}
