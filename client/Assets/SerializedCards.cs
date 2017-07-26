using System;
using System.Collections.Generic;

public class SerializedCards {
    public String state;
    public List<SerializedCard> cards;

    public SerializedCards(String state, IList<SerializedCard> cards) {
        this.state = state;
        this.cards = new List<SerializedCard>(cards);
    }
}
