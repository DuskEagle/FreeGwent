using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;

public class GoFirstOrSecondButton : MonoBehaviour, IPointerClickHandler {

    public Boolean goFirst;

    [SerializeField] WhoGoesFirstScreen whoGoesFirstScreen;
    private GwentNetworkManager gwn;

    private void Start() {
        gwn = (GwentNetworkManager)FindObjectOfType(typeof(GwentNetworkManager));
    }

    public void OnPointerClick(PointerEventData eventData) {
        whoGoesFirstScreen.Hide();
        gwn.SendWhoGoesFirst(goFirst);
    }

}
