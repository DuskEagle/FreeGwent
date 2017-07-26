using System;
using System.Collections.Generic;

public class CardComparer :
    IComparer<DeckBuilderCard>,
    IComparer<MulliganCard>,
    IComparer<InGameCard> {
    private int CompareCardType(Card c1, Card c2) {
        Boolean c1UnitType = c1.IsUnitType();
        Boolean c2UnitType = c2.IsUnitType();
        if (c1UnitType == c2UnitType) {
            return 0;
        } else if (c1UnitType) {
            return 1;
        } else {
            return -1;
        }
    }

    private int CompareBasePower(Card c1, Card c2) {
        if (c1.basePower == null && c2.basePower == null) {
            return 0;
        } else if (c1.basePower == null) {
            return 1;
        } else if (c2.basePower == null) {
            return -1;
        } else {
            return c1.basePower.Value - c2.basePower.Value;
        }
    }

    private int CompareNonNullCards(Card c1, Card c2) {
        int cardTypeComparison = CompareCardType(c1, c2);
        if (cardTypeComparison != 0) {
            return cardTypeComparison;
        }
        int basePowerComparison = CompareBasePower(c1, c2);
        if (basePowerComparison != 0) {
            return basePowerComparison;
        }
        return c1.id.CompareTo(c2.id);
    }

    public int Compare(Card c1, Card c2)
    {
        if (c1 == null && c2 == null)
        {
            return 0;
        }
        else if (c1 == null)
        {
            return -1;
        }
        else if (c2 == null)
        {
            return 1;
        }
        else
        {
            return CompareNonNullCards(c1, c2);
        }
    }

    public int Compare(DeckBuilderCard c1, DeckBuilderCard c2) {
        return Compare((Card)c1, (Card)c2);
    }

    public int Compare(MulliganCard c1, MulliganCard c2) {
        return Compare((Card)c1, (Card)c2);
    }

    public int Compare(InGameCard c1, InGameCard c2) {
        return Compare((Card)c1, (Card)c2);
    }

}
