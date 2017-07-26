using System;

public class SerializedMulligan {
    public String state;
    public SerializedCard card;

    public SerializedMulligan(String state, SerializedCard card) {
        this.state = state;
        this.card = card;
    }
}
