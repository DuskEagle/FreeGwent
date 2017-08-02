using System;
using System.Collections.Generic;
using RSG;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;

public class DiscardPile : CardRow, IPointerClickHandler {

    [SerializeField] private CardDisplay cardDisplay;

    override public void Populate(IEnumerable<InGameCard> cardsToAdd) {
        base.Populate(cardsToAdd);
        UpdateImage();
    }

    private void UpdateImage() {
        if (cards.Count > 0) {
            this.GetComponent<Image>().sprite = cards[0].image;
        } else {
            this.GetComponent<Image>().sprite = null;
        }
    }

    override public void RemoveCard(InGameCard card) {
        base.RemoveCard(card);
        UpdateImage();
    }

    public void OnPointerClick(PointerEventData eventData) {
        cardDisplay.Display(cards);
    }

    public IPromise<IList<TurnEvent>> ShowCardsForRevival() {
        return cardDisplay.DisplayForCardRevival(cards);
    }

    public InGameCard PopCardById(String id) {
        InGameCard result = cards.Find(c => c.id == id);
        if (result == null) {
            throw new AddCardException(
                String.Format("Card {0} not found in Discard pile.")
            );
        }
        RemoveCard(result);
        return result;
    }

}
