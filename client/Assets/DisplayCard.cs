using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

public class DisplayCard : InGameCard, IPointerClickHandler {

    [SerializeField] private CardDisplay cardDisplay;
    private Boolean cardSelectBehaviour;

    public static DisplayCard CreateCard(
        InGameCard inGameCard,
        CardDisplay display,
        Boolean cardSelectBehaviour) {

        GameObject cardObject = Instantiate((GameObject)Resources.Load("DisplayCard"), display.transform);
        DisplayCard card = cardObject.GetComponent<DisplayCard>();
        card.cardObject = cardObject;
        card.id = inGameCard.id;
        card.combatTypes = new List<CombatType>(inGameCard.combatTypes);
        card.attributes = new List<CardAttribute>(inGameCard.attributes);
        card.basePower = inGameCard.basePower;
        card.currentPower = inGameCard.currentPower;
        card.faction = inGameCard.faction;
        card.image = Resources.Load<Sprite>("CardImages/" + inGameCard.id);
        card.cardDisplay = display;
        card.cardSelectBehaviour = cardSelectBehaviour;
        return card;
    }
    
    public void OnPointerClick(PointerEventData eventData) {
        if (cardSelectBehaviour) {
            cardDisplay.CardSelected(this);
        } else {
            cardDisplay.transform.SendMessage("OnPointerClick", eventData);
        }
    }

}
