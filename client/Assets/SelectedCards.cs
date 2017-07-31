using System;
using System.Collections.Generic;
using UnityEngine;

public class SelectedCards : DeckBuilderCardSlot {
    
    [SerializeField] private UnitCountText unitCountText;
    [SerializeField] private SpecialCountText specialCountText;
    [SerializeField] private LeaderCountText leaderCountText;
    [SerializeField] private SubmitButton submitButton;

    private void PropagateCardChanges() {
        IList<DeckBuilderCard> cards = GetCards();
        unitCountText.UpdateText(cards);
        specialCountText.UpdateText(cards);
        leaderCountText.UpdateText(cards);
        submitButton.UpdateState(cards);
    }

    override public void AddCard(DeckBuilderCard card) {
        base.AddCard(card);
        PropagateCardChanges();
    }

    override protected void RemoveCard(DeckBuilderCard card) {
        base.RemoveCard(card);
        PropagateCardChanges();
    }

    override public IList<DeckBuilderCard> RemoveAllCards() {
        IList<DeckBuilderCard> result = base.RemoveAllCards();
        PropagateCardChanges();
        return result;
    }
}
