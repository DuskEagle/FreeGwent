using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.UI;

public class UnitCountText : MonoBehaviour {

    private int requiredUnitCards = 22;

    void Start() {
        UpdateText(new List<DeckBuilderCard>());
    }

    public void UpdateText(IList<DeckBuilderCard> selectedCards) {
        int unitCardsCount = selectedCards.Where(card =>
            card.IsUnitType()
        ).Count();
        Text textComponent = this.GetComponent<Text>();
        if (unitCardsCount < requiredUnitCards) {
            textComponent.text = String.Format("{0}/{1}", unitCardsCount, requiredUnitCards);
            textComponent.color = new Color32(0x87, 0x00, 0x00, 0xff);
        } else {
            textComponent.text = String.Format("{0}", unitCardsCount);
            textComponent.color = new Color32(0x0e, 0x78, 0x14, 0xff);
        }
    }
}
