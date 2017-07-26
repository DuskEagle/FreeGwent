using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using FreeGwent;

public class CombatCard : InGameCard, IDropHandler {
	/* A CombatCard is any card that is played in either the melee, ranged, or
	 * siege rows. It is a superset of the UnitCard type - in addition to all
	 * UnitCards, a CombatCard also represents cards such as Heroes or Dummies.
	 */

    new public int basePower;
    new public int currentPower;

    //new public static CombatCard CreateCard(
    //    CardRow cardRow,
    //    String id,
    //    IList<CombatType> combatTypes,
    //    int? basePower,
    //    int? currentPower,
    //    String faction) {
    //    return CreateCard(cardRow, id, combatTypes, basePower.Value, currentPower.Value, faction);
    //}

    //public static CombatCard CreateCard(
    //    CardRow cardRow,
    //    String id,
    //    IList<CombatType> combatTypes,
    //    int basePower,
    //    int currentPower,
    //    String faction) {
        
    //    Debug.Log("Combat card");
    //    GameObject cardObject = Instantiate((GameObject)Resources.Load("InGameCard"), cardRow.transform);
    //    Debug.Log(cardObject);
    //    CombatCard card = (CombatCard)cardObject.GetComponent<InGameCard>();
    //    card.cardObject = cardObject;
    //    card.id = id;
    //    card.combatTypes = new List<CombatType>(combatTypes);
    //    card.basePower = basePower;
    //    card.currentPower = currentPower;
    //    card.faction = faction;
    //    return card;
    //}

    public void OnDrop(PointerEventData eventData) {
        Decoy decoy = eventData.pointerDrag.GetComponent<Decoy>();
        if (decoy && this.cardRow is CombatCardRow) {
            this.hand.RemoveCard(decoy);
            this.cardRow.AddCard(decoy, true);
            this.cardRow.RemoveCard(this);
            this.hand.AddCard(this, true);
        } else {
            // Propagate OnDrop event to CardRow
            this.transform.parent.SendMessage("OnDrop", eventData);
        }
    }

}