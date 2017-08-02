using System;
using System.Linq;
using System.Collections.Generic;
using RSG;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;
using FreeGwent;

/*
 * Used for showing cards from the discard pile, or cards from your opponent's
 * hand in the case of leader abilities that allow for that.
 */
public class CardDisplay : MonoBehaviour, IPointerClickHandler {

    [SerializeField] private CardDisplayRow cardDisplayRow;
    [SerializeField] private BoardManager boardManager;

    private Vector3 originalScale;
    private Vector3 hiddenScale = new Vector3(0, 0, 0);
    private Promise<IList<TurnEvent>> cardSelectPromise;
    private Boolean cardSelectBehaviour = false;

    private void Start() {
        this.originalScale = this.transform.localScale;
        this.GetComponent<Image>().color = new Color32(0x00, 0x00, 0x00, 0x88);
        Hide();
    }

    public void Display(IEnumerable<InGameCard> cards) {
        this.transform.localScale = originalScale;
        this.cardSelectBehaviour = false;
        // Need to copy the individual cards so they remain in their original
        // position (e.g. discard pile, hand) as well as being in the
        // CardDisplay.
        IEnumerable<DisplayCard> displayCards = cards.Select(card => {
            DisplayCard newCard = DisplayCard.CreateCard(card, this, false);
            newCard.SetSize(270, 360);
            return newCard;
        });
        cardDisplayRow.Populate(displayCards);
    }

    public IPromise<IList<TurnEvent>> DisplayForCardRevival(IEnumerable<InGameCard> cards) {
        cardSelectPromise = new Promise<IList<TurnEvent>>();
        this.transform.localScale = originalScale;
        this.cardSelectBehaviour = true;
        IEnumerable<DisplayCard> displayCards = cards.Where(card =>
            card.IsUnitType() && !card.IsHero()
        ).Select(card => {
            DisplayCard newCard = DisplayCard.CreateCard(card, this, true);
            newCard.SetSize(270, 360);
            return newCard;
        });
        cardDisplayRow.Populate(displayCards);
        return cardSelectPromise;
    }

    public void Hide() {
        this.transform.localScale = hiddenScale;
    }

    public void OnPointerClick(PointerEventData eventData) {
        if (cardSelectBehaviour == false) {
            Hide();
        }
        // Else, we rely on the DisplayCard to call
        // CardSelected to Hide() this.
    }

    public void CardSelected(DisplayCard card) {
        Hide();
        //TurnEvent turnEvent = boardManager.PlayCardFromDiscardPile(card);
        IPromise<IList<TurnEvent>> turnEvent = boardManager.PlayCardFromDiscardPile(card);
        turnEvent.Then(te =>
            cardSelectPromise.Resolve(te)
        );
    }

}
