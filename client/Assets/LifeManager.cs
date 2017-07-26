using System;
using System.Collections.Generic;
using UnityEngine;
using FreeGwent;

public class LifeManager : MonoBehaviour {

    public TotalScores totalScores;
    public List<LifeCircle> ourLifeCircles;
    public List<LifeCircle> theirLifeCircles;

    public void HandleRoundEnd() {
        Debug.Log("HandleRoundEnd");

        if (totalScores.GetWinningPlayer() != Player.Us) {
            ourLifeCircles[0].markDead();
            ourLifeCircles.RemoveAt(0);
        }
        if (totalScores.GetWinningPlayer() != Player.Them) {
            theirLifeCircles[0].markDead();
            theirLifeCircles.RemoveAt(0);
        }
        this.CheckForGameCompletion();
    }

    private void CheckForGameCompletion() {
    }

    public void UpdateLife(int ourLife, int theirLife) {
        UpdateLifeHelper(ourLife, ourLifeCircles);
        UpdateLifeHelper(theirLife, theirLifeCircles);
    }

    private void UpdateLifeHelper(int life, IList<LifeCircle> lifeCircles) {
        for (int i = 0; i < lifeCircles.Count; i++) {
            if (i <= life) {
                lifeCircles[i].markAlive();
            } else {
                lifeCircles[i].markDead();
            }
        }
    }
}
