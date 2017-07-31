using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using FreeGwent;

public class DeckBuilderCard : Card, IPointerClickHandler {

    public DeckBuilderCardSlot cardSlot;
    
    private DeckBuilderCardSlot hiddenCards;
    private AvailableCards availableCards;
    private SelectedCards selectedCards;
    private Toggle doubleClickToggle;
    private GameObject cardObject;

    public static DeckBuilderCard CreateCard(
        DeckBuilderCardSlot hiddenCards,
        AvailableCards availableCards,
        SelectedCards selectedCards,
        Toggle doubleClickToggle,
        String id,
        IList<CombatType> combatTypes,
        IList<CardAttribute> attributes,
        int? basePower,
        String faction) {
        
        GameObject cardObject = Instantiate((GameObject)Resources.Load("DeckBuilderCard"), hiddenCards.transform);
        DeckBuilderCard card = cardObject.GetComponent<DeckBuilderCard>();
        card.cardObject = cardObject;
        card.id = id;
        card.combatTypes = new List<CombatType>(combatTypes);
        card.attributes = new List<CardAttribute>(attributes);
        card.basePower = basePower;
        card.faction = faction;
        card.hiddenCards = hiddenCards;
        card.availableCards = availableCards;
        card.selectedCards = selectedCards;
        card.cardSlot = hiddenCards;
        card.doubleClickToggle = doubleClickToggle;
        return card;
    }

    private void Start() {
        this.cardObject.GetComponent<Image>().sprite = Resources.Load<Sprite>("CardImages/" + this.id);
    }

    private int RequiredClicks() {
        return this.doubleClickToggle.isOn ? 2 : 1;
    }

    public void OnPointerClick(PointerEventData eventData) {
        if (eventData.clickCount == RequiredClicks()) {
            if (cardSlot == availableCards) {
                selectedCards.AddCard(this);
            } else if (cardSlot == selectedCards) {
                availableCards.AddCard(this);
            } else {
                Debug.LogError("Card selected from invalid DeckBuilderCardSlot");
            }
        }
    }

}
