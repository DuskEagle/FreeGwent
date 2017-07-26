using System;
using UnityEngine;
using UnityEngine.UI;
using FreeGwent;

public class LifeCircle : MonoBehaviour {

    public void markAlive() {
        this.GetComponent<Image>().sprite = Resources.Load<Sprite>("redcircle");
    }

    public void markDead() {
        this.GetComponent<Image>().sprite = Resources.Load<Sprite>("greycircle");
    }

}
