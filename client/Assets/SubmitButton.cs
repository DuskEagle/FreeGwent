using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;

public class SubmitButton : MonoBehaviour, IPointerClickHandler {
    
    // Todo: Store these values in a single place, rather than across
    // SubmitButton, UnitCountText, SpecialCountText, and possibly
    // other places.
    private int requiredUnitCards = 1;
    private int maxSpecialCards = 10;

    [SerializeField] private GwentNetworkManager gwn;
    [SerializeField] private SelectedCards selectedCards;
    [SerializeField] private WaitingForOtherPlayer waitingScreen;
    private Vector3 originalScale;
    private Vector3 hiddenScale;

    void Start() {
        originalScale = this.transform.localScale;
        hiddenScale = new Vector3(0, 0, 0);
        UpdateState(new List<DeckBuilderCard>());
    }

    public void UpdateState(IList<DeckBuilderCard> cards) {
        int unitCardsCount = cards.Where(card =>
            card.IsUnitType()
        ).Count();
        int specialCardsCount = cards.Where(card =>
            card.IsSpecialType()
        ).Count();
        int leaderCardsCount = cards.Where(card =>
            card.IsLeaderType()
        ).Count();
        if (unitCardsCount >= requiredUnitCards &&
            specialCardsCount <= maxSpecialCards &&
            leaderCardsCount == 1) {
            Show();    
        } else {
            Hide();
        }
    }

    private void Show() {
        this.transform.localScale = originalScale;
    }

    private void Hide() {
        this.transform.localScale = hiddenScale;
    }

    public void OnPointerClick(PointerEventData eventData) {
        waitingScreen.Display();
        gwn.SubmitDeck(selectedCards.GetCards());
    }
}
