using System;
using System.Collections.Generic;
using UnityEngine;

public class AvailableCards : DeckBuilderCardSlot {

    public GwentNetworkManager gwn;

    public void Start() {
        gwn.RetrieveCardCollection().Then(cards => {
            this.cards = new List<DeckBuilderCard>(cards);
            UpdateCardOrder();
        }).Catch(e => {
            Debug.LogError(e);
        });
    }
}
