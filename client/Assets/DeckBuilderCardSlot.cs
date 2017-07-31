using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

abstract public class DeckBuilderCardSlot : MonoBehaviour {
    
    protected List<DeckBuilderCard> cards = new List<DeckBuilderCard>();
    private CardComparer cardComparer = new CardComparer();

    protected String neutral = "neutral";

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

    public void AddCards(IList<DeckBuilderCard> cardsToAdd) {
        for (int i = 0; i < cardsToAdd.Count; i++) {
            AddCard(cardsToAdd[i]);
        }
    }

    virtual protected void RemoveCard(DeckBuilderCard card) {
        this.cards.Remove(card);
        this.UpdateCardOrder();
    }

    virtual public IList<DeckBuilderCard> RemoveAllCards() {
        IList<DeckBuilderCard> result = this.cards;
        this.cards = new List<DeckBuilderCard>();
        return result;
    }
  
}
