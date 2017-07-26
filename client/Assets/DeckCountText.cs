using System;
using UnityEngine;
using UnityEngine.UI;

public class DeckCountText : MonoBehaviour {

    public void UpdateCount(int count) {
        this.GetComponent<Text>().text = String.Format("Deck: {0}", count);
    }

}
