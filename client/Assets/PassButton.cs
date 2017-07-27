using System;
using System.Linq;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class PassButton : MonoBehaviour, IPointerClickHandler {

    private GwentNetworkManager gwn;

    void Start() {
        gwn = (GwentNetworkManager)FindObjectOfType(typeof(GwentNetworkManager));
    }

    public void OnPointerClick(PointerEventData eventData) {
        gwn.Pass();
    }

}
