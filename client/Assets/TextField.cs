using System;
using UnityEngine;
using UnityEngine.UI;

public class TextField : MonoBehaviour {
    
    public void UpdateText(String text) {
        this.GetComponent<Text>().text = text;
    }

}
