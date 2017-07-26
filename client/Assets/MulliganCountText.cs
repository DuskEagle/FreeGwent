using System;
using UnityEngine;
using UnityEngine.UI;

public class MulliganCountText : MonoBehaviour {
    
    private String baseText = "Choose a card to mulligan {0}/2";
    private int count = 0;

    private void Start() {
        UpdateText();
    }

    public int Count() {
        return count;
    }

    public void Increment() {
        count += 1;
        UpdateText();
    }

    private void UpdateText() {
        this.GetComponent<Text>().text = String.Format(baseText, count);
    }

}
