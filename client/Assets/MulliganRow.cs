using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;

public class MulliganRow : MonoBehaviour {

    private List<MulliganCard> cards = new List<MulliganCard>();
    private CardComparer cardComparer = new CardComparer();

    /* Populate the MulliganRow with cardsToAdd, replacing
     * any cards that used to be here.
     */
    public void Populate(IEnumerable<MulliganCard> cardsToAdd) {
        cards.ForEach(card => {
            Destroy(card.gameObject);
        });
        cards = new List<MulliganCard>(cardsToAdd);
        cards.ForEach(card => {
            card.transform.SetParent(this.transform);
        });
        this.UpdateCardOrder();
    }

    public void UpdateCardOrder() {
        cards.Sort(cardComparer);
        for (int i = 0; i < cards.Count; i++) {
            cards[i].transform.SetSiblingIndex(i);
        }
    }

    public void BlockRaycasts(Boolean blocksRaycasts) {
        this.cards.ForEach(card =>
            card.transform.GetComponent<CanvasGroup>().blocksRaycasts = blocksRaycasts
        );
    }
}
