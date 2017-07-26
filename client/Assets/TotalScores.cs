using System;
using UnityEngine;
using UnityEngine.UI;
using FreeGwent;

public class TotalScores : MonoBehaviour
{
    public RowScore theirSiegeScore;
    public RowScore theirRangedScore;
    public RowScore theirMeleeScore;
    public RowScore ourMeleeScore;
    public RowScore ourRangedScore;
    public RowScore ourSiegeScore;

    public GameObject ourScoreText;
    public GameObject theirScoreText;

    private int ourScore;
    private int theirScore;

    private Color winningScoreColor = new Color32(0x92, 0x00, 0xff, 0xff);
    private Color defaultScoreColor = new Color32(0x00, 0x00, 0x00, 0xff);

    public int GetOurScore() {
        return ourScore;
    }

    public int GetTheirScore() {
        return theirScore;
    }

    /* Get winning player. In the event of a tie, returns null. */
    public Player? GetWinningPlayer() {
        if (this.GetOurScore() > this.GetTheirScore()) {
            return Player.Us;
        } else if (this.GetOurScore() < this.GetTheirScore()) {
            return Player.Them;
        } else {
            return null;
        }
    }

    void Update() {
        this.ourScore = ourMeleeScore.GetScore() +
                        ourRangedScore.GetScore() +
                        ourSiegeScore.GetScore();
        this.theirScore = theirMeleeScore.GetScore() +
                          theirRangedScore.GetScore() +
                          theirSiegeScore.GetScore();
        ourScoreText.GetComponent<Text>().text = ourScore.ToString();
        theirScoreText.GetComponent<Text>().text = theirScore.ToString();
        ourScoreText.GetComponent<Text>().color = ourScore > theirScore ? winningScoreColor
                                                                        : defaultScoreColor;
        theirScoreText.GetComponent<Text>().color = theirScore > ourScore ? winningScoreColor
                                                                          : defaultScoreColor;
    }
}
