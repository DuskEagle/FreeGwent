using System;
using UnityEngine;

public class WaitingForOtherPlayer : MonoBehaviour {
	
    [SerializeField] private TextField text;

    private void Start() {
        Hide();
    }

    public void Hide() {
        this.GetComponent<Behaviour>().enabled = false;
        text.UpdateText("");
    }

    public void Display() {
        this.GetComponent<Behaviour>().enabled = true;
        text.UpdateText("Finding Opponent...");
    }
    
}
