using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using FreeGwent;

public class HornRow : CardRow {

    public int GetMultiplier() {
        return this.cards.Count == 0 ? 1 : 2;
    }

    override protected Boolean CanAddCard(InGameCard card) {
        return this.cards.Count == 0 && base.CanAddCard(card);
    }

    /* Populate the HornRow with `cardToAdd` if it's non-null.
     * Else, just clear the HornRow.
     */
    public void Populate(DeserializedCard cardToAdd) {
        cards.ForEach(card => {
            Destroy(card.gameObject);
        });
        if (cardToAdd != null) {
            cards = new List<InGameCard> { cardToAdd.ToInGameCard(this) };
            cards.ForEach(card => {
                card.transform.SetParent(this.transform);
            });
        } else {
            cards = new List<InGameCard>();
        }
    }

}