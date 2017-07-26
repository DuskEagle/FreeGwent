using System;
using System.Collections.Generic;
using FreeGwent;

public class Decoy : CombatCard {

    public static String decoyId = "decoy";

    new private void Start() {
        base.Start();
        this.id = Decoy.decoyId;
        this.basePower = 0;
        this.combatTypes = new List<CombatType> { CombatType.Melee, CombatType.Ranged, CombatType.Siege };
    }
}
