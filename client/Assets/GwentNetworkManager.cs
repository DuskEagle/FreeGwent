using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using RSG;
using Newtonsoft.Json;
using System.Linq;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using FreeGwent;

public class GwentNetworkManager : MonoBehaviour {

    private static GwentNetworkManager gwn = null;

    [SerializeField] private Toggle doubleClickToggle;
    [SerializeField] private HiddenCards hiddenCards;
    [SerializeField] private AvailableCards availableCards;
    [SerializeField] private SelectedCards selectedCards;

    private MulliganRow mulliganRow = null;
    private MulliganCountText mulliganCountText;
    private WhoGoesFirstScreen whoGoesFirstScreen;
    private Promise<WebSocket> ws = new Promise<WebSocket>();
    private BoardManager boardManager = null;

    private Boolean useMock = false;

    private void Awake() {
        if (GwentNetworkManager.gwn == null) {
            GwentNetworkManager.gwn = this;
        } else if (GwentNetworkManager.gwn != this) {
            Destroy(gameObject);
        }
        DontDestroyOnLoad(gameObject);
    }

    private void Start() {
        StartCoroutine(Connect());
        if (useMock) {
            ReceiveGameState().Then(response => {
                InGameSceneSetup(response);
            }).Catch(e =>
                Debug.LogError(e)
            );
        }
    }

    private IEnumerator Connect() {
        WebSocket __ws__ = new WebSocket(new Uri("ws://localhost:9000"));
        yield return StartCoroutine(__ws__.Connect());
        this.ws.Resolve(__ws__);
    }

    private IPromise<String> RecvString() {
        return this.ws.Then(ws => {
            Promise<String> ps = new Promise<String>();
            StartCoroutine(__RecvString__(ps, ws));
            return ps;
        }).Catch(e =>
            Debug.LogError(e)
        );
    }

    private IEnumerator __RecvString__(Promise<String> ps, WebSocket ws) {
        String str = ws.RecvString();
        while (str == null) {
            yield return null;
            str = ws.RecvString();
        }
        ps.Resolve(str);
    }

    private void SendString(String str) {
        this.ws.Then(ws => {
            ws.SendString(str);
        });
    }

    public IPromise<IList<DeckBuilderCard>> RetrieveCardCollection() {
        return RecvString().Then(str => {
            Promise<IList<DeckBuilderCard>> pc = new Promise<IList<DeckBuilderCard>>();
            IList<DeckBuilderCard> cards = JsonConvert
                .DeserializeObject<DeserializedCards>(str)
                .ToDeckBuilderCards(
                    hiddenCards,
                    availableCards,
                    selectedCards,
                    doubleClickToggle
                );
            pc.Resolve(cards);
            return pc;
        });
    }

    public void SubmitDeck(IList<DeckBuilderCard> submitCards) {
        SerializedCards serializedCards = new SerializedCards("sendDeck", submitCards.Select(card =>
            new SerializedCard(card.id)
        ).ToList());
        SendString(JsonConvert.SerializeObject(serializedCards));
        ReceiveMulliganHand().Then(hand => {
            StartCoroutine(StartMulliganScene(hand));
        });
    }

    private IPromise<MulliganResponse> ReceiveMulliganHand() {
        return RecvString().Then(str => {
            Promise<MulliganResponse> pc = new Promise<MulliganResponse>();
            MulliganResponse response = JsonConvert
                .DeserializeObject<MulliganResponse>(str);
            pc.Resolve(response);
            return pc;
        });
    }

    private IPromise<GameState> ReceiveGameState() {
        return RecvString().Then(str => {
            Debug.Log(str);
            Promise<GameState> pc = new Promise<GameState>();
            GameState receivedCards = JsonConvert
                .DeserializeObject<GameState>(str);
            pc.Resolve(receivedCards);
            return pc;
        }).Catch(e =>
            Debug.LogError(e)
        );
    }

    private IEnumerator<System.Object> StartMulliganScene(MulliganResponse hand) {
        SceneManager.LoadScene("Mulligan");
        // After a LoadScene, we must wait one frame before calling FindObjectsOfType
        yield return null;
        mulliganRow = (MulliganRow)FindObjectOfType(typeof(MulliganRow));
        whoGoesFirstScreen = (WhoGoesFirstScreen)FindObjectOfType(typeof(WhoGoesFirstScreen));
        mulliganCountText = (MulliganCountText)FindObjectOfType(typeof(MulliganCountText));
        mulliganRow.Populate(hand.cards.Select(c => c.ToMulliganCard(mulliganRow)));
    }

    public void MulliganCard(MulliganCard mulliganCard) {
        // Disable selecting further cards for mulliganing until
        // we receive a response from the server
        // TODO: TEST THIS
        //mulliganRow.BlockRaycasts(true);
        SerializedMulligan message = new SerializedMulligan(
            "mulligan",
            new SerializedCard(mulliganCard.id));
        SendString(JsonConvert.SerializeObject(message));
        ReceiveMulliganHand().Then(hand => {
            mulliganRow.Populate(hand.cards.Select(c => c.ToMulliganCard(mulliganRow)));
            mulliganCountText.Increment();
            if (mulliganCountText.Count() == 2) {
                if (hand.selectWhoGoesFirst) {
                    whoGoesFirstScreen.Show();
                } else {
                    WaitForStartGameMessage();
                }
            } else {
                //mulliganRow.BlockRaycasts(false);
            }
        });
    }

    private void WaitForStartGameMessage() {
        ReceiveGameState().Then(response => {
            StartCoroutine(StartInGameScene(response));
        });
    }

    public void SendWhoGoesFirst(Boolean weGoFirst) {
        String s;
        if (weGoFirst) {
            s = "first";
        } else {
            s = "second";
        }
        TurnEvents ev = new TurnEvents(new List<TurnEvent>{ new TurnEvent(s, "")});
        SendString(JsonConvert.SerializeObject(ev));
        ReceiveMulliganHand().Then(hand =>
            WaitForStartGameMessage()
        );
    }

    private IEnumerator<System.Object> StartInGameScene(GameState response) {
        SceneManager.LoadScene("InGame");
        // Must wait one frame
        yield return null;
        InGameSceneSetup(response);
    }

    private void InGameSceneSetup(GameState response) {
        boardManager = (BoardManager)FindObjectOfType(typeof(BoardManager));
        boardManager.UpdateBoard(response);
        if (!response.ourTurn) {
            WaitForOurTurn();
        }
    }

    public void SendTurn(IPromise<IList<TurnEvent>> eventsPromise) {
        eventsPromise.Then(events => {
            boardManager.UpdateWhoseTurn(false);
            TurnEvents turnEvents = new TurnEvents(events);
            SendString(JsonConvert.SerializeObject(turnEvents));
            WaitForOurTurn();
        });
    }

    public void SendTurn(IList<TurnEvent> events) {
        SendTurn(Promise<IList<TurnEvent>>.Resolved(events));
    }

    private void WaitForOurTurn() {
        ReceiveGameState().Then(gameState => {
            boardManager.UpdateBoard(gameState);
            if (gameState.ourLife == 0 || gameState.theirLife == 0) {
                EndGame(gameState);
            } else if (!gameState.ourTurn) {
                WaitForOurTurn();
            }
        }).Catch(e =>
            Debug.LogError(e)
        );
    }

    public void Pass() {
        IPromise<IList<TurnEvent>> promise = Promise<IList<TurnEvent>>
            .Resolved(new List<TurnEvent>());
        SendTurn(promise);
    }

    private void EndGame(GameState gameState) {
        GameEndScreen endScreen = (GameEndScreen)FindObjectOfType(typeof(GameEndScreen));
        if (gameState.ourLife == 0 && gameState.theirLife == 0) {
            endScreen.DisplayDraw();
        } else if (gameState.ourLife == 0) {
            endScreen.DisplayLoss();
        } else {
            endScreen.DisplayWin();
        }
    }

}
