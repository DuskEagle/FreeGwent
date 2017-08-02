using System;
using System.Collections.Generic;
using UnityEngine;

public class FactionButtonManager : MonoBehaviour {

    public FactionButton monsters;
    public FactionButton nilfgaard;
    public FactionButton northern;
    public FactionButton scoiatael;

    private List<FactionButton> buttons;

    private void Start() {
        buttons = new List<FactionButton> { monsters, nilfgaard, northern, scoiatael };
    }

    public void Selected(FactionButton factionButton) {
        buttons.ForEach(b => b.MarkSelected(false));
        factionButton.MarkSelected(true);
    }

}
