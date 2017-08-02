using System;
using UnityEngine;
using UnityEngine.UI;

public class GameEndScreen : MonoBehaviour {
    
    [SerializeField] private TextField endGameText;

    private Vector3 originalScale;

    private void Start() {
        this.GetComponent<Image>().color = new Color32(0x00, 0x00, 0x00, 0x88);
        originalScale = this.transform.localScale;
        this.transform.localScale = new Vector3(0, 0, 0);
    }

    public void DisplayWin() {
        DisplayHelper();
        endGameText.UpdateText("Victory!");
    }

    public void DisplayDraw() {
        DisplayHelper();
        endGameText.UpdateText("Draw!");
    }

    public void DisplayLoss() {
        DisplayHelper();
        endGameText.UpdateText("Defeat!");
    }

    private void DisplayHelper() {
        this.transform.localScale = originalScale;
        this.GetComponent<Image>().color = new Color32(0xff, 0xff, 0xff, 0x88);
    }

}
