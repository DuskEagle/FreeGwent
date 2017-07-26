using System;
using UnityEngine;
using UnityEngine.UI;

public class RowScore : MonoBehaviour {
    public CombatCardRow combatCardRow;
    public HornRow hornRow;
    //public WeatherCardRow weatherCardRow;

    private int score;

    public int GetScore() {
        return this.score;
    }

    public void UpdateScore() {
        this.score = combatCardRow.GetScore();
        this.GetComponent<Text>().text = score.ToString();
    }
}