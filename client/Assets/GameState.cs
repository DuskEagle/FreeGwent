using System;
using System.Collections.Generic;

public class GameState {
    public BoardState board;
    public IList<DeserializedCard> hand;
    public IList<DeserializedCard> ourDiscardPile;
    public IList<DeserializedCard> theirDiscardPile;
    public int ourLife;
    public int theirLife;
    public int theirHandCount;
    public int ourDeckCount;
    public int theirDeckCount;
    public Boolean ourTurn;
}
