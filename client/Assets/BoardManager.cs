using System;
using System.Collections.Generic;
using System.Linq;
using RSG;
using UnityEngine;
using FreeGwent;

public class BoardManager : MonoBehaviour {
    [SerializeField] private CombatCardRow theirSiegeRow;
    [SerializeField] private CombatCardRow theirRangedRow;
    [SerializeField] private CombatCardRow theirMeleeRow;
    [SerializeField] private CombatCardRow ourMeleeRow;
    [SerializeField] private CombatCardRow ourRangedRow;
    [SerializeField] private CombatCardRow ourSiegeRow;

    [SerializeField] private HornRow theirSiegeHorn;
    [SerializeField] private HornRow theirRangedHorn;
    [SerializeField] private HornRow theirMeleeHorn;
    [SerializeField] private HornRow ourMeleeHorn;
    [SerializeField] private HornRow ourRangedHorn;
    [SerializeField] private HornRow ourSiegeHorn;

    [SerializeField] private WeatherRow weatherRow;

    [SerializeField] private CardRow theirLeader;
    [SerializeField] private CardRow ourLeader;

    [SerializeField] private DiscardPile theirDiscardPile;
    [SerializeField] private DiscardPile ourDiscardPile;

    [SerializeField] private HandRow handRow;
    
    [SerializeField] private DeckCountText ourDeckCount;
    [SerializeField] private DeckCountText theirDeckCount;
    [SerializeField] private TheirHandSize theirHandSize;
    [SerializeField] private LifeManager lifeManager;

    [SerializeField] private UIBlocker uiBlocker;

    public void UpdateBoard(GameState gameState) {
        BoardState boardState = gameState.board;
        handRow.Populate(gameState.hand);
        UpdateCombatRows(boardState);
        UpdateHorns(boardState);
        UpdateWeather(boardState);
        UpdateDiscardPiles(gameState);
        UpdateDeckCounts(gameState);
        UpdateHandSize(gameState);
        lifeManager.UpdateLife(gameState.ourLife, gameState.theirLife);
        UpdateWhoseTurn(gameState.ourTurn);
    }

    private void UpdateCombatRows(BoardState boardState) {
        theirSiegeRow.Populate(boardState.siege2.cards);
        theirRangedRow.Populate(boardState.ranged2.cards);
        theirMeleeRow.Populate(boardState.melee2.cards);
        ourMeleeRow.Populate(boardState.melee1.cards);
        ourRangedRow.Populate(boardState.ranged1.cards);
        ourSiegeRow.Populate(boardState.siege1.cards);
    }

    private void UpdateHorns(BoardState boardState) {
        theirSiegeHorn.Populate(boardState.siege2.horn);
        theirRangedHorn.Populate(boardState.ranged2.horn);
        theirMeleeHorn.Populate(boardState.melee2.horn);
        ourMeleeHorn.Populate(boardState.melee1.horn);
        ourRangedHorn.Populate(boardState.ranged1.horn);
        ourSiegeHorn.Populate(boardState.siege1.horn);
    }

    private void UpdateWeather(BoardState boardState) {
        weatherRow.Populate(boardState.weather);
    }

    private void UpdateDiscardPiles(GameState gameState) {
        theirDiscardPile.Populate(gameState.theirDiscardPile);
        ourDiscardPile.Populate(gameState.ourDiscardPile);
    }

    private void UpdateDeckCounts(GameState gameState) {
        ourDeckCount.UpdateCount(gameState.ourDeckCount);
        theirDeckCount.UpdateCount(gameState.theirDeckCount);
    }

    private void UpdateHandSize(GameState gameState) {
        // ourHandSize is directly hooked up to our hand,
        // so we only need to update theirHandSize here.
        theirHandSize.UpdateSize(gameState.theirHandCount);
    }

    /*
     * If it's our turn, allow for board interaction.
     * If it's their turn, block board interaction.
     */
    public void UpdateWhoseTurn(Boolean ourTurn) {
        if (ourTurn) {
            uiBlocker.Unblock();
        } else {
            uiBlocker.Block();
        }
    }

    public IPromise<IList<TurnEvent>> PlayCardFromDiscardPile(DisplayCard displayCard) {
        InGameCard card = ourDiscardPile.PopCardById(displayCard.id);
        List<CombatType> eligibleCts = new List<CombatType> { CombatType.Melee, CombatType.Ranged, CombatType.Siege };
        if (card.reviveRow == null) {
            throw new AddCardException(
                String.Format("{0} card is missing a revive row.", card.id)
            );
        } else if (!eligibleCts.Contains(card.reviveRow.Value)) {
            throw new AddCardException(
                String.Format("{0} card has an ineligble revive row.", card.id)
            );
        }
        CombatType reviveRow = card.reviveRow.Value;
        IPromise<IList<TurnEvent>> result;
        if (card.IsSpy()) {
            if (reviveRow == CombatType.Siege) {
                result = theirSiegeRow.AddCard(card);
            } else if (reviveRow == CombatType.Ranged) {
                result = theirRangedRow.AddCard(card);
            } else {
                result = theirMeleeRow.AddCard(card);
            }
        } else {
            if (reviveRow == CombatType.Melee) {
                result = ourMeleeRow.AddCard(card);
            } else if (reviveRow == CombatType.Ranged) {
                result = ourRangedRow.AddCard(card);
            } else {
                result = ourSiegeRow.AddCard(card);
            }
        }
        return result;
    }
}
