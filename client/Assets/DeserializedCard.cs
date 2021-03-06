﻿using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using FreeGwent;
using System.Linq;

public class DeserializedCard {
    public String id;
    public List<String> combatTypes;
    public List<String> attributes;
    public int? basePower;
    public int? currentPower;
    public String faction;
    public String reviveRow;

    private static String Capitalize(String s) {
        String capitalizedString = null;
        if (String.IsNullOrEmpty(s))
            capitalizedString = s;
        else
            capitalizedString = s.Remove(1).ToUpper() + s.Substring(1);
        return capitalizedString;
    }

    private static CombatType? ParseCombatType(String unparsedCt) {
        if (unparsedCt == null) {
            return null;
        } else {
            return (CombatType)Enum.Parse(typeof(CombatType), Capitalize(unparsedCt));
        }
    }

    private static IList<CombatType> ParseCombatTypes(IList<String> unparsedCt) {
        return unparsedCt.Select(ct =>
            ParseCombatType(ct)
        ).Where(ct =>
            ct != null
        ).Select(ct =>
            (CombatType)ct
        ).ToList();
    }

    private static IList<CardAttribute> ParseCardAttributes(IList<String> unparsedAttr) {
        return unparsedAttr.Select(attr =>
            (CardAttribute)Enum.Parse(typeof(CardAttribute), Capitalize(attr))
        ).ToList();
    }

    public DeckBuilderCard ToDeckBuilderCard(
        HiddenCards hiddenCards,
        AvailableCards availableCards,
        SelectedCards selectedCards,
        Toggle doubleClickToggle) {

        return DeckBuilderCard.CreateCard(
            hiddenCards,
            availableCards,
            selectedCards,
            doubleClickToggle,
            id,
            ParseCombatTypes(this.combatTypes),
            ParseCardAttributes(this.attributes),
            basePower,
            faction);
    }

    public MulliganCard ToMulliganCard(MulliganRow mulliganRow) {
        return MulliganCard.CreateCard(
            mulliganRow,
            id,
            ParseCombatTypes(this.combatTypes),
            ParseCardAttributes(this.attributes),
            basePower,
            faction);
    }

    public InGameCard ToInGameCard(CardRow row) {
        return InGameCard.CreateCard(
            row,
            id,
            ParseCombatTypes(this.combatTypes),
            ParseCardAttributes(this.attributes),
            basePower,
            currentPower,
            faction,
            ParseCombatType(reviveRow));
    }
}
