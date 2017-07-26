using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using UnityEngine.UI;

public class LeaderCountText : MonoBehaviour {

    void Start() {
        UpdateText(new List<DeckBuilderCard>());
    }

    public void UpdateText(IList<DeckBuilderCard> selectedCards) {
        int leaderCount = selectedCards.Where(card =>
            card.IsLeaderType()
        ).Count();
        Text textComponent = this.GetComponent<Text>();
        textComponent.text = String.Format("{0}/1", leaderCount);
        if (leaderCount == 1) {
            textComponent.color = new Color32(0x0e, 0x78, 0x14, 0xff);
        } else {
            textComponent.color = new Color32(0x87, 0x00, 0x00, 0xff);
        }
    }
}
