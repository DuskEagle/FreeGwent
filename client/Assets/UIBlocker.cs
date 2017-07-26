using System;
using UnityEngine;
using UnityEngine.EventSystems;

/*
 * When it's not our turn, we need a way to prevent the player from interacting
 * with the board. The simplest, crudest way to do that is to draw a transparent
 * panel over top of the game that blocks all UI events from propagating.
 * 
 * Eventually, if we want elements of the UI to be interactable while it's not
 * our turn, we'll need a more sophisticated approach. But for now, KISS.
 */
public class UIBlocker : MonoBehaviour {

    public void Start() {
        Block();
    }

    public void Block() {
        GetComponent<CanvasGroup>().blocksRaycasts = true;
    }

    public void Unblock() {
        GetComponent<CanvasGroup>().blocksRaycasts = false;
    }

}
