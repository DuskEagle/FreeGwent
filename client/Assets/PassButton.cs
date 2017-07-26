using System;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class PassButton : MonoBehaviour {

    private LifeManager lifeManager;

    private void Start() {
        // Would prefer to hook this up through the Unity UI; but buttons
        // interact differently than other GameObjects and this is the easiest
        // way I thought of for how to set this value.
        this.lifeManager = ((LifeManager)FindObjectOfType(typeof(LifeManager)));
    }

    public void OnSubmit() {
        lifeManager.HandleRoundEnd();

        ((CardRow[])FindObjectsOfType(typeof(CardRow))).ToList<CardRow>().Where ( cr =>
            !(cr is HandRow)
        ).ToList().ForEach ( cr =>
            cr.DiscardRow()
        );
    }
}
