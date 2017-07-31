using System;
using System.Collections.Generic;
using System.Linq;
using UnityEngine.UI;

public class DeserializedCards {

    public List<DeserializedCard> cards;

    public IList<DeckBuilderCard> ToDeckBuilderCards(
        HiddenCards hiddenCards,
        AvailableCards availableCards,
        SelectedCards selectedCards,
        Toggle doubleClickToggle
    ) {
        return this.cards.Select(dCard =>
            dCard.ToDeckBuilderCard(
                hiddenCards,
                availableCards,
                selectedCards,
                doubleClickToggle
            )
        ).ToList();
    }

    public IList<MulliganCard> ToMulliganCards(MulliganRow mulliganRow)
    {
        return this.cards.Select(dCard =>
            dCard.ToMulliganCard(mulliganRow)
        ).ToList();
    }

    public IList<InGameCard> ToInGameCards(HandRow handRow)
    {
        return this.cards.Select(dCard =>
            dCard.ToInGameCard(handRow)
        ).ToList();
    }
}
