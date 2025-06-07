package player;

import card.Card;
//플레이어 행동 정의
public interface IPlayerAction {
    void receiveCard(Card card);
    void receiveOpenCard(Card card);
    void receiveHiddenCard(Card card);
}
