using System;
using System.Collections.Generic;
using UnityEngine;
using FreeGwent;

public class LifeManager : MonoBehaviour {

    public TotalScores totalScores;
    public List<LifeCircle> ourLifeCircles;
    public List<LifeCircle> theirLifeCircles;

    public void UpdateLife(int ourLife, int theirLife) {
        UpdateLifeHelper(ourLife, ourLifeCircles);
        UpdateLifeHelper(theirLife, theirLifeCircles);
    }

    private void UpdateLifeHelper(int life, IList<LifeCircle> lifeCircles) {
        for (int i = 0; i < lifeCircles.Count; i++) {
            if (i < life) {
                lifeCircles[i].markAlive();
            } else {
                lifeCircles[i].markDead();
            }
        }
    }
}
