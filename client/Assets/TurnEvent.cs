using System;

public class TurnEvent {
    public String cardId;
    public String row;
    public Boolean pass;

    public static TurnEvent Pass() {
        TurnEvent e = new TurnEvent("", "");
        e.pass = true;
        return e;
    }

    public TurnEvent(String cardId, String row) {
        this.cardId = cardId;
        this.row = row;
        this.pass = false;
    }

}
