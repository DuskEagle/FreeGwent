using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.UI;

public class SpecialCountText : MonoBehaviour {

    private int maxSpecialCards = 10;

    void Start() {
        UpdateText(new List<DeckBuilderCard>());
    }

    public void UpdateText(IList<DeckBuilderCard> selectedCards) {
        int specialCardsCount = selectedCards.Where(card =>
            card.IsSpecialType()
        ).Count();
        Text textComponent = this.GetComponent<Text>();
        textComponent.text = String.Format("{0}/{1}", specialCardsCount, maxSpecialCards);
        if (specialCardsCount <= maxSpecialCards) {
            textComponent.color = new Color32(0x0e, 0x78, 0x14, 0xff);
        } else {
            textComponent.color = new Color32(0x87, 0x00, 0x00, 0xff);
        }
    }
}
