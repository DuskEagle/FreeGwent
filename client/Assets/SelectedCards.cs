using System;
using System.Collections.Generic;
using UnityEngine;

public class SelectedCards : DeckBuilderCardSlot {
    
    [SerializeField] private UnitCountText unitCountText;
    [SerializeField] private SpecialCountText specialCountText;
    [SerializeField] private LeaderCountText leaderCountText;
    [SerializeField] private SubmitButton submitButton;

    private void PropagateCardChanges(IList<DeckBuilderCard> cards) {
        unitCountText.UpdateText(cards);
        specialCountText.UpdateText(cards);
        leaderCountText.UpdateText(cards);
        submitButton.UpdateState(cards);
    }

    override public void AddCard(DeckBuilderCard card) {
        base.AddCard(card);
        PropagateCardChanges(GetCards());
    }

    override protected void RemoveCard(DeckBuilderCard card) {
        base.RemoveCard(card);
        PropagateCardChanges(GetCards());
    }
}
