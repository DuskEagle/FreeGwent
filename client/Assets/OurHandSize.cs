using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class OurHandSize : MonoBehaviour {
    
    public HandRow hand;

    void Update() {
        this.GetComponent<Text>().text = hand.GetCards().Count.ToString();
    }
}
