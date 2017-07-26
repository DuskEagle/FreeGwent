using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

abstract public class DeckBuilderCardSlot : MonoBehaviour {
    
    protected List<DeckBuilderCard> cards = new List<DeckBuilderCard>();
    private CardComparer cardComparer = new CardComparer();

    public IList<DeckBuilderCard> GetCards() {
        return cards.AsReadOnly();
    }

    public void UpdateCardOrder() {
        cards.Sort(cardComparer);
        for (int i = 0; i < cards.Count; i++) {
            cards[i].transform.SetSiblingIndex(i);
        }
    }
    
    virtual public void AddCard(DeckBuilderCard card) {
        if (card.cardSlot && card.cardSlot != this) {
            card.cardSlot.RemoveCard(card);
        }
        card.cardSlot = this;
        card.transform.SetParent(this.transform);
        this.cards.Add(card);
        this.UpdateCardOrder();
    }

    virtual protected void RemoveCard(DeckBuilderCard card) {
        this.cards.Remove(card);
        this.UpdateCardOrder();
    }
  
}
