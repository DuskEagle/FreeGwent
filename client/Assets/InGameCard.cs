using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using FreeGwent;

[Serializable]
public class InGameCard : Card, IBeginDragHandler, IDragHandler, IEndDragHandler {

    private Vector2 mouseOffset;
    private GameObject placeholder = null;
    protected GameObject cardObject;

    // Figure out a way to track beginDragRow using cardRow or hand
    private Transform beginDragRow;
    protected CardRow cardRow;
    protected HandRow hand;

    public static InGameCard CreateCard(
        CardRow cardRow,
        String id,
        IList<CombatType> combatTypes,
        IList<CardAttribute> attributes,
        int? basePower,
        int? currentPower,
        String faction) {
        
        GameObject cardObject = Instantiate((GameObject)Resources.Load("InGameCard"), cardRow.transform);
        InGameCard card = cardObject.GetComponent<InGameCard>();
        card.cardObject = cardObject;
        card.id = id;
        card.combatTypes = new List<CombatType>(combatTypes);
        card.attributes = new List<CardAttribute>(attributes);
        card.basePower = basePower;
        card.currentPower = currentPower;
        card.faction = faction;
        return card;
    }

    public InGameCard Copy() {
        return InGameCard.CreateCard(
            cardRow,
            id,
            new List<CombatType>(combatTypes),
            new List<CardAttribute>(attributes),
            basePower,
            currentPower,
            faction
        );
    }

    public void SetSize(int width, int height) {
        this.GetComponent<LayoutElement>().preferredWidth = width;
        this.GetComponent<LayoutElement>().preferredHeight = height;
    }

    public void Start() {
        this.beginDragRow = this.transform.parent;
        this.hand = (HandRow)FindObjectsOfType(typeof(HandRow))[0];
        this.cardRow = hand;
        this.cardObject.GetComponent<Image>().sprite = Resources.Load<Sprite>("CardImages/" + this.id);
    }

    public void OnBeginDrag(PointerEventData eventData) {
        if (this.cardRow != this.hand) {
            eventData.pointerDrag = null; // cancel the drag event
            return;
        }

        GetComponent<CanvasGroup>().blocksRaycasts = false;
        mouseOffset = eventData.position - (Vector2)this.transform.position;

        placeholder = new GameObject();
        placeholder.transform.SetParent(this.transform.parent);
        LayoutElement le = placeholder.AddComponent<LayoutElement>();
        LayoutElement ourLE = GetComponent<LayoutElement>();
        le.preferredWidth = ourLE.preferredWidth;
        le.preferredHeight = ourLE.preferredHeight;
        le.flexibleWidth = ourLE.flexibleWidth;
        le.flexibleHeight = ourLE.flexibleHeight;
        placeholder.transform.SetSiblingIndex(this.transform.GetSiblingIndex());

        beginDragRow = this.transform.parent;
        this.transform.SetParent(beginDragRow.parent);
    }

    public void OnDrag(PointerEventData eventData) {
        this.transform.position = eventData.position - mouseOffset;
    }

    public void OnEndDrag(PointerEventData eventData) {
        GetComponent<CanvasGroup>().blocksRaycasts = true;
        this.transform.SetParent(beginDragRow);
        //this.cardRow.UpdateCardOrder(); // Shouldn't be necessary; test this before final removal.
        Destroy(placeholder);
    }

    public TurnEvent SetCardRow(CardRow row) {
        if (this.cardRow && this.cardRow != row) {
            this.cardRow.RemoveCard(this);
        }
        this.beginDragRow = row.transform;
        this.transform.SetParent(beginDragRow);
        //do we need this?
        this.transform.position = beginDragRow.position;
        //do we need this?
        this.cardRow = row;
        
        return new TurnEvent(id, cardRow.rowName);
    }

    public void Discard(DiscardPile discardPile) {
        this.transform.SetParent(discardPile.transform);
        this.transform.position = discardPile.transform.position;
        discardPile.AddCard(this);
        if (this.cardRow) {
            this.cardRow.RemoveCard(this);
        }
        this.beginDragRow = null;
        this.cardRow = null;
    }

    public Boolean IsSpy() {
        return attributes.Contains(CardAttribute.Spy);
    }

    virtual protected void Battlecry() {
        return;
    }
}