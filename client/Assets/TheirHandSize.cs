using System;
using UnityEngine;
using UnityEngine.UI;

public class TheirHandSize : MonoBehaviour {
    
    public void UpdateSize(int size) {
        this.GetComponent<Text>().text = size.ToString();
    }

}
