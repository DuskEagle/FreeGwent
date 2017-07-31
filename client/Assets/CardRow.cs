using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

public class CardRow : MonoBehaviour, IDropHandler {

    public CombatType combatType;
    public Player player;
    public String rowName;

    private GwentNetworkManager gwn;

    protected List<InGameCard> cards = new List<InGameCard>();

    private DiscardPile discardPile;

    protected virtual void Start() {
        this.discardPile = ((DiscardPile[])FindObjectsOfType(typeof(DiscardPile))).Where(d =>
          d.player == this.player
        ).First();
        gwn = (GwentNetworkManager)FindObjectOfType(typeof(GwentNetworkManager));
    }

    public IList<InGameCard> GetCards() {
        return cards.AsReadOnly();
    }

	public void OnDrop(PointerEventData eventData) {
        InGameCard card = eventData.pointerDrag.GetComponent<InGameCard>();
        try {
            this.AddCard(card);
        } catch (AddCardException) {}
    }

    public void UpdateCardOrder() {
        this.cards = this.cards.OrderBy(c => {
            return c.basePower.GetValueOrDefault(0);
        }).ToList<InGameCard>();
        for (int i = 0; i < cards.Count; i++) {
            cards[i].transform.SetSiblingIndex(i);
        }
    }

    private void UpdateScore() {
        (FindObjectsOfType(typeof(RowScore)) as RowScore[]).ToList<RowScore>().ForEach (rs =>
            rs.UpdateScore()
        );
    }

    /* Is it a legal move to add the card to this row? */
    protected virtual Boolean CanAddCard(InGameCard card) {
        return card != null &&
            card.combatTypes.Contains(this.combatType) &&
            ((!card.IsSpy() && player == Player.Us) ||
            (card.IsSpy() && player == Player.Them));
    }

    /* Attempt to add card to row. If the card can't be added, throws
     * an exception. Use skipCheck = true to always add the card */
    virtual public void AddCard(InGameCard card, Boolean skipCheck = false) {
        if (skipCheck || this.CanAddCard(card)) {
            TurnEvent turnEvent = card.SetCardRow(this);
            this.cards.Add(card);
            this.UpdateCardOrder();
            this.UpdateScore();
            gwn.SendTurn(new List<TurnEvent> { turnEvent });
            return;
        }
        throw new AddCardException(String.Format("Attempt to add card {0} to invalid row.", card.id));
    }

    /* Populate the cards in CardRow with cardsToAdd, replacing
     * any cards that used to be here.
     */
    public void Populate(IEnumerable<DeserializedCard> cardsToAdd) {
        Populate(cardsToAdd.Select(c => c.ToInGameCard(this)));
    }

    /* Populate the cards in CardRow with cardsToAdd, replacing
     * any cards that used to be here.
     */
    public void Populate(IEnumerable<InGameCard> cardsToAdd) {
        cards.ForEach(card => {
            Destroy(card.gameObject);
        });
        cards = new List<InGameCard>(cardsToAdd);
        cards.ForEach(card => {
            card.transform.SetParent(this.transform);
        });
        this.UpdateCardOrder();
        this.UpdateScore();
    }

    public void RemoveCard(InGameCard card) {
        this.cards.Remove(card);
        this.UpdateCardOrder();
        this.UpdateScore();
    }

    public void DiscardRow() {
        cards.ForEach ( card => {
            card.Discard(this.discardPile);
        });
    }
}

class AddCardException : Exception {
    public AddCardException(String message) : base(message) {}
}
