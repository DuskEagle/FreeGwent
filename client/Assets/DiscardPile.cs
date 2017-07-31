using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;

public class DiscardPile : CardRow, IPointerClickHandler {

    [SerializeField] private CardDisplay cardDisplay;    

    override public void AddCard(InGameCard card, Boolean skipCheck = false) {
        cards.Add(card);
        UpdateCardOrder();
    }

    public void OnPointerClick(PointerEventData eventData) {
        cardDisplay.Display(cards);
    }

}
