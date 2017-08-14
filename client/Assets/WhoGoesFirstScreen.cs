using System;
using UnityEngine;
using UnityEngine.UI;

public class WhoGoesFirstScreen : MonoBehaviour {

    private Vector3 originalScale;
    private Vector3 hiddenScale = new Vector3(0, 0, 0);

    private void Start() {
        originalScale = this.transform.localScale;
        this.GetComponent<Image>().color = new Color32(0x00, 0x00, 0x00, 0x88);
        Hide();
    }

    public void Show() {
        this.transform.localScale = originalScale;
    }

    public void Hide() {
        this.transform.localScale = hiddenScale;
    }

}
