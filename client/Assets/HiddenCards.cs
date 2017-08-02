using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;

public class HiddenCards : DeckBuilderCardSlot {
	
    [SerializeField] private GwentNetworkManager gwn;
    [SerializeField] private FactionButton defaultFaction;

    public void Start() {
        gwn.RetrieveCardCollection().Then(cards => {
            this.cards = new List<DeckBuilderCard>(cards);
            defaultFaction.Select();
        }).Catch(e => {
            Debug.LogError(e);
        });
    }

    override public void AddCard(DeckBuilderCard card) {
        base.AddCard(card);
        // Hide the card when it's in this slot
        card.GetComponent<Behaviour>().enabled = false;
    }

    public IList<DeckBuilderCard> TakeCardsForFaction(String faction) {
        IList<DeckBuilderCard> result = this.cards.Where(card =>
            card.faction == faction || card.faction == neutral
        ).Select(card => {
            card.GetComponent<Behaviour>().enabled = true;
            return card;
        }).ToList();
        this.cards = this.cards.Where(card =>
            card.faction != faction && card.faction != neutral
        ).ToList();
        return result;
    }

}
