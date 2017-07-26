using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using FreeGwent;

public class MulliganCard : Card, IPointerClickHandler {

    private GameObject cardObject;
    private GwentNetworkManager gwn;

    public static MulliganCard CreateCard(
        MulliganRow mulliganRow,
        String id,
        IList<CombatType> combatTypes,
        IList<CardAttribute> attributes,
        int? basePower,
        String faction) {
        
        GameObject cardObject = Instantiate((GameObject)Resources.Load("MulliganCard"), mulliganRow.transform);
        MulliganCard card = cardObject.GetComponent<MulliganCard>();
        card.cardObject = cardObject;
        card.id = id;
        card.combatTypes = new List<CombatType>(combatTypes);
        card.attributes = new List<CardAttribute>(attributes);
        card.basePower = basePower;
        card.faction = faction;
        return card;
    }

    private void Start() {
        this.cardObject.GetComponent<Image>().sprite = Resources.Load<Sprite>("CardImages/" + this.id);
        gwn = (GwentNetworkManager)FindObjectOfType(typeof(GwentNetworkManager));
    }

    public void OnPointerClick(PointerEventData eventData) {
        if (eventData.clickCount == 2) {
            gwn.MulliganCard(this);
        }
    }

}
