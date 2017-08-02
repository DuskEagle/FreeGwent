using System;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class FactionButton : MonoBehaviour, IPointerEnterHandler, IPointerExitHandler, IPointerClickHandler {
    
    public String faction;

    private Image image;
    private Boolean selected = false;
    [SerializeField] private FactionButtonManager factionManager;
    [SerializeField] private HiddenCards hiddenCards;
    [SerializeField] private AvailableCards availableCards;
    [SerializeField] private SelectedCards selectedCards;

    private Color32 defaultColor = new Color32(0xae, 0x84, 0x63, 0xff);
    private Color32 hoverColor = new Color32(0xa0, 0x7a, 0x59, 0xff);
    private Color32 selectedColor = new Color32(0x90, 0x69, 0x4a, 0xff);

    private void Awake() {
        image = this.GetComponent<Image>();
    }

    public void OnPointerClick(PointerEventData eventData) {
        Select();
    }

    public void Select() {
        if (!selected) {
            factionManager.Selected(this);
        }
    }

    public void MarkSelected(Boolean selected) {
        this.selected = selected;
        if (selected) {
            image.color = selectedColor;
            hiddenCards.AddCards(selectedCards.RemoveAllCards());
            hiddenCards.AddCards(availableCards.RemoveAllCards());
            availableCards.AddCards(hiddenCards.TakeCardsForFaction(faction));
        } else {
            image.color = defaultColor;
        }
    }
    
    public void OnPointerEnter(PointerEventData eventData) {
        if (!selected) {
            image.color = hoverColor;
        }
    }

    public void OnPointerExit(PointerEventData eventData) {
        if (!selected) {
            image.color = defaultColor;
        }
    }

}
