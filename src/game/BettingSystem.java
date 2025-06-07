package game;

import player.Player;
import card.Card;
import exception.InvalidBetException;
import java.util.*;

public class BettingSystem {
	//필드
    private GameManager manager;  
    private int pot = 0;
    private int currentBet = 0;
    private int checkCount = 0; //private 로 수정했음.(현재 문제 없음)
    
    //플레이어 레이즈 횟수 저장
    private Map<Player, Integer> playerRaiseCount = new HashMap<>(); 
    
    //Check 횟수 조회
    public int getCheckCount() {    	
    	return checkCount;    	
    }
    
    //현재 라운드 Bet 조회
    public int getCurrentBet() {    	
    	return currentBet;
    }
    
    //현재 pot 값 조회
    public int getPot() { 
    	
    	return pot;
    }
    
    //GameManager를 BettingSystem과 연결
    public BettingSystem(GameManager manager) {    	
    	this.manager = manager;    	
    }
    
    //고정 참가비
    public void collectAnte(Player player, int anteAmount) {
        player.payAnte(anteAmount); 
        this.pot += anteAmount;        
    }

    //라운드 리셋
    public void resetRound() {    	
        currentBet = 0;
        
        playerRaiseCount.clear();
        if (manager != null && manager.getUser() != null) manager.getUser().resetBet();
        if (manager != null && manager.getComputer() != null) manager.getComputer().resetBet();
        
        checkCount = 0;
        
        if (manager != null && manager.getUser() != null) manager.getUser().setActedThisRound(false);
        if (manager != null && manager.getComputer() != null) manager.getComputer().setActedThisRound(false);
    }
    
    //Pot 0으로 리셋
    public void resetPot() {    
    	pot = 0;   	
    }

    //check 로직
    public void check(Player player) throws InvalidBetException {
    	 if (isAllIn(player) && player.getCurrentBet() < currentBet) {
             player.setActedThisRound(true);             
             return;
        }       
    	 if (player.getCurrentBet() < currentBet) {
             throw new InvalidBetException("상대가 베팅(" + currentBet + ")했습니다. 체크 대신 콜 또는 레이즈 하세요. (나의 베팅: " + player.getCurrentBet() + ")");
         }       
        checkCount++;
        player.setActedThisRound(true);
    }    
    
    //call 로직
    public void call(Player player) throws InvalidBetException {
        int amountToCall = currentBet - player.getCurrentBet();
        if (amountToCall <= 0) { 
            if (currentBet == 0) {
                throw new InvalidBetException("아무도 베팅하지 않아 콜할 수 없습니다. 체크 또는 베팅하세요.");
            } else {
                throw new InvalidBetException("이미 현재 베팅액(" + currentBet + ")을 맞췄습니다. 폴드 혹은 체크 하세요.");
            }
        }
        // safeBet에서 코인 부족 시 올인 처리
        safeBet(player, amountToCall);
        player.setActedThisRound(true);
    }    
    
    // fold 로직
    public void fold(Player player) {
        player.fold();
        player.setActedThisRound(true);
    }
    
    //쿼터레이즈 로직
    public void quarterRaise(Player player) throws InvalidBetException {
        if (pot <= 0 && currentBet == 0) throw new InvalidBetException("초기 팟과 베팅이 없어 팟 기반 레이즈를 할 수 없습니다. 먼저 베팅하세요.");        
        int raiseAmount = pot / 4;
        performRaise(player, Math.max(1, raiseAmount));
    }
    
    //하프 레이즈 로직
    public void halfRaise(Player player) throws InvalidBetException {
        if (pot <= 0 && currentBet == 0) throw new InvalidBetException("초기 팟과 베팅이 없어 팟 기반 레이즈를 할 수 없습니다. 먼저 베팅하세요.");
        int raiseAmount = pot / 2;
        performRaise(player, Math.max(1, raiseAmount));
    }
    
    //레이즈 액션 로직
    private void performRaise(Player player, int raiseAmount) throws InvalidBetException {
        validateRaisePossible(player); // 레이즈 가능 여부 검증 (횟수, 코인)
        
        if (raiseAmount <= 0) {
            throw new InvalidBetException("레이즈 금액은 0보다 커야 합니다.");
        }

        int amountToCall = currentBet - player.getCurrentBet();
        //레이즈 0 이하 나올 시 0처리
        if (amountToCall < 0) amountToCall = 0; // 이미 더 많이 냈을 경우는 없지만 방어적으로

        int totalBetThisAction = amountToCall + raiseAmount; // 이번 액션에 내는 총 금액

        if (player.getCoin() < totalBetThisAction) {
            throw new InvalidBetException("코인이 부족하여 해당 금액(" + totalBetThisAction + ")으로 레이즈할 수 없습니다. (현재 코인: " + player.getCoin() + ")");
        }

        safeBet(player, totalBetThisAction); // 콜 + 레이즈 금액만큼 베팅
        currentBet += raiseAmount;           // 라운드의 기준 베팅액을 새 레이즈 금액만큼 올림
        playerRaiseCount.put(player, playerRaiseCount.getOrDefault(player, 0) + 1);
        player.setActedThisRound(true);
    }    
    
    //레이즈 제한 로직
    private void validateRaisePossible(Player player) throws InvalidBetException {
    	
        if (playerRaiseCount.getOrDefault(player, 0) >= 1) { // 한 라운드에 레이즈는 한 번으로 제한
            throw new InvalidBetException("한 라운드에 한 번만 레이즈할 수 있습니다.");
        }
        
        int amountToCall = Math.max(0, currentBet - player.getCurrentBet());
        
        if (player.getCoin() <= amountToCall) { // 콜할 금액 이하거나, 콜할 금액도 없는 경우 (이미 올인 포함)
        	
             if (player.getCoin() > 0 && amountToCall > 0) { // 코인은 있지만 콜 금액보다 적으면 올인 콜만 가능
            	 
                 throw new InvalidBetException("코인이 부족하여 레이즈할 수 없습니다. 올인 콜만 가능합니다.");
                 
             } else if (player.getCoin() == 0) {
            	 
                 throw new InvalidBetException("코인이 없어 레이즈할 수 없습니다.");
             }            
        }
    }
    
    //레이즈 수행 여부 로직
    public boolean canRaise(Player player) {
        return playerRaiseCount.getOrDefault(player, 0) < 1;
    }
    
    //레이즈 대응 베팅, 코인 부족 시 All in 처리 로직
    private void safeBet(Player player, int amount) {
        if (amount >= player.getCoin()) {
            int allIn = player.getCoin();
            player.bet(allIn); pot += allIn;
        } else {
            player.bet(amount); pot += amount;
        }
    }    
    
    //올인 판별 로직
    private boolean isAllIn(Player player) { 
    	return player.getCoin() <= 0; 
    	}
    
    //승자 pot 지급 로직
    public void awardPot(Player winner) { 
    	if (pot > 0) { 
    		winner.receivePot(pot); 
    		pot = 0; 
    		} 
    	}
    
    //컴퓨터 자동 행동 로직
    public String autoAction(Player player) {

        int callAmount = getCurrentBet() - player.getCurrentBet();
        
        // 올인 상태 (보유 코인 < 베팅 코인)
        if (player.getCoin() <= callAmount && callAmount > 0) {
            call(player); // 자동으로 올인 콜
            player.setActedThisRound(true);
            return "올인 콜";
        }
        
        // 올인 상태(보유코인 0 일 때)
        if (player.getCoin() == 0 && callAmount <=0) {
             player.setActedThisRound(true);
             return "올인 상태 (액션 없음)";
        }
        
        //플레이어 오픈카드 리스트 호출
        List<Card> playerCards = player.getOpenCards();
        List<Card> opponentCards = manager.getOpponent(player).getOpenCards();
        //오픈 카드 중 최고 랭크 오픈 카드로 설정 로직
        int highestOpen = playerCards.isEmpty() ? 0 : playerCards.stream().mapToInt(Card::getRank).max().orElse(0);
        int opponentHighest = opponentCards.isEmpty() ? 0 : opponentCards.stream().mapToInt(Card::getRank).max().orElse(0);
        //패 인식 후 베팅 방향 결정
        boolean isStrong = highestOpen >= 10;
        boolean isBehind = highestOpen < opponentHighest;
        double rand = Math.random();
        String decidedAction = "행동 오류. 코드 확인 바람.";        
        
        //컴퓨터 선 턴일 경우 처리 로직
        if (getCurrentBet() == 0) {
        	//패가 강하다고 인식할 경우 처리 로직
            if (isStrong) {
            	//75% 확률로 레이즈 시도
                if (rand < 0.75) {
                    decidedAction = safeRaise(player, true);
                } else {
                    check(player); decidedAction = "체크";
                }
              //패가 약하다고 인식할 경우 처리 로직
            } else {
                if (isBehind && rand < 0.3) {
                    decidedAction = safeRaise(player, false);
                } else if (rand < 0.85) {
                    check(player); decidedAction = "체크";
                } else {
                    decidedAction = safeRaise(player, false);
                }
            }
            
         //User 가 베팅한 상황 처리 로직
        } else {
        	//레이즈 할 수 없을 경우 처리 로직
            if (!canRaise(player)) {
                if (isStrong || (!isBehind && rand < 0.8) || (isBehind && rand < 0.3) ) {
                    call(player); decidedAction = "콜";
                } else {
                    fold(player); decidedAction = "폴드";
                }
              // 레이즈가 가능할 경우 처리 로직
            } else {
                if (isStrong && !isBehind) {
                    if (rand < 0.5) { decidedAction = safeRaise(player, true); }
                    else { call(player); decidedAction = "콜"; }
                  // 덱이 강하다고 판단 될 경우 로직
                } else if (isStrong) { 
                     if (rand < 0.3) { decidedAction = safeRaise(player, false); }
                     else if (rand < 0.8) {call(player); decidedAction = "콜";}
                     else {fold(player); decidedAction = "폴드";}
                }
                // 패가 약하다고 판단될 경우 로직
                else {
                    if (isBehind && rand < 0.85) {
                        fold(player); decidedAction = "폴드";
                    } else if (!isBehind && rand < 0.6) { 
                        fold(player); decidedAction = "폴드";
                    }
                    // 블러핑 로직 처리
                    else {
                         if (rand < 0.85) {call(player); decidedAction = "콜";}
                         else {decidedAction = safeRaise(player, false);}
                     }
                }
            }
        }
        // autoAction종료 
        player.setActedThisRound(true);
        return decidedAction;
    }
    
    //안전 베팅 처리 로직
    private String safeRaise(Player player, boolean isHalf) {
        try {
            if (isHalf) {
                halfRaise(player); 
            } else {
                quarterRaise(player);
            }
            return isHalf ? "하프 레이즈" : "쿼터 레이즈";
        } catch (IllegalStateException e) {        	
            int callAmountWhenRaiseFails = getCurrentBet() - player.getCurrentBet();            
            if (getCurrentBet() > 0 && player.getCoin() >= callAmountWhenRaiseFails && callAmountWhenRaiseFails >=0) {
                call(player);
                return "콜 (레이즈 실패 후)";
            } else if (getCurrentBet() == 0 || callAmountWhenRaiseFails <= 0) { 
                check(player);
                return "체크 (레이즈 실패 후)";
            } else {
                fold(player);
                return "폴드 (레이즈 실패 및 콜 불가)";
            }
        }
    }
    
    //플레이어 초기 베팅금 처리 로직
    public void applyAnteBet(Player player, int amount) {
        if (player.isFolded()) {
            return;
        }
        try {
            player.bet(amount);
            this.pot += amount;
            this.currentBet = amount;
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }
    }    
    
    //라운드 별 베팅 액션 관리/처리 로직
    public boolean isBettingOver(Player user, Player computer) {
    	
        if (user.isFolded() || computer.isFolded()) {
            return true;
        }
        
        boolean userAllIn = isAllIn(user);
        boolean computerAllIn = isAllIn(computer);
        
        if (userAllIn && computerAllIn) {
            return true;
        }        
        
        if (userAllIn && computer.hasActedThisRound()) {
            return true;
        }
        
        if (computerAllIn && user.hasActedThisRound()) {
            return true;
        }
       
        if (currentBet == 0 && user.hasActedThisRound() && computer.hasActedThisRound()) {
            return true;
        }
        
        if (currentBet > 0 && user.getCurrentBet() == currentBet && computer.getCurrentBet() == currentBet &&
            user.hasActedThisRound() && computer.hasActedThisRound()) {
            return true;
        }
        
        if (userAllIn && computer.getCurrentBet() >= currentBet && computer.hasActedThisRound()) {
             return true;
        }
        
        if (computerAllIn && user.getCurrentBet() >= currentBet && user.hasActedThisRound()) {
             return true;
        }
        
        return false;
    }
}
