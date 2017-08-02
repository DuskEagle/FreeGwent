using System;

public class TurnEvent {
    public String cardId;
    public String row;

    public static TurnEvent Pass() {
        TurnEvent e = new TurnEvent("", "");
        return e;
    }

    public TurnEvent(String cardId, String row) {
        this.cardId = cardId;
        this.row = row;
    }

}
