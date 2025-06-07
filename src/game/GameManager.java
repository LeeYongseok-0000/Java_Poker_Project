package game;

import player.*;
import card.*;
import ui.GameGUI; // GameGUI 참조를 위해 임포트 (실제 패키지 경로에 맞게 조정 필요)
import java.util.List;
import exception.InvalidBetException;

public class GameManager {
	
	//필드
    private Deck deck = new Deck();
    private UserPlayer user = new UserPlayer();
    private ComputerPlayer computer = new ComputerPlayer();
    private Player currentPlayer;
    private BettingSystem bettingSystem;
    
    // 앤티 금액(초기 게임비)
    public static final int ANTE_AMOUNT = 5000;
    private GameGUI gui;
    private boolean gameTrulyOverNoMoney = false; // 한쪽 코인이 없어 완전 종료해야 하는지 여부

    // 게임 라운드 및 상태 정의
    public static final int INIT_DEAL = 0;                  // 초기 4장 배분
    public static final int DISCARD_OPEN_PHASE = 1;         // 1장 버리고 1장 오픈 단계
    public static final int FIRST_BETTING_ROUND_START = 2;  // 첫 번째 베팅 라운드 시작
    public static final int DEAL_4TH_STREET = 3;            // 4번째 카드(오픈) 배분
    public static final int SECOND_BETTING_ROUND_START = 4; // 두 번째 베팅 라운드 시작
    public static final int DEAL_5TH_STREET = 5;            // 5번째 카드(오픈) 배분
    public static final int THIRD_BETTING_ROUND_START = 6;  // 세 번째 베팅 라운드 시작
    public static final int DEAL_6TH_STREET = 7;            // 6번째 카드(오픈) 배분
    public static final int FOURTH_BETTING_ROUND_START = 8; // 네 번째 베팅 라운드 시작
    public static final int DEAL_7TH_STREET = 9;            // 7번째 카드(히든) 배분
    public static final int FIFTH_BETTING_ROUND_START = 10; // 다섯 번째 베팅 라운드 시작
    public static final int SHOWDOWN_PHASE = 11;            // 쇼다운
    public static final int GAME_OVER = 12;                 // 게임 종료
    
    private int currentRound;	// 현재 게임 라운드(진행 상태)
    private boolean userDiscardCompleted = false; // 사용자가 카드 버리기를 완료했는지
    private boolean userOpenCompleted = false;    // 사용자가 카드 오픈을 완료했는지
    
    //생성자
    public GameManager() {
        this.bettingSystem = new BettingSystem(this);
    }

    //GUI 참조 설정
    public void setGUI(GameGUI gui) {
        this.gui = gui;
    }
    
    //BettingSystem 반환
    public BettingSystem getBettingSystem() {
    	return bettingSystem;
    	}
    
    //상대 플레이어 반환
    public Player getOpponent(Player player) {
    	return player == user ? computer : user;
    	}
    
    //유저 플레이어 반환
    public UserPlayer getUser() {
    	return user;
    	}
    
    //컴퓨터 플레이어 반환
    public ComputerPlayer getComputer() {
    	return computer;
    	}
    
    //현재 플레이어 반환
    public Player getCurrentPlayer() {
    	return currentPlayer;
    	}
    //현재 라운드 상태 확인
    public int getCurrentRound() {
    	return currentRound;
    	}
    
    //게임 시작 로직(코인 유지)
    public void startGame() {
        deck.reset();
        user.clearAll();
        computer.clearAll();
        user.resetCoin(10000); // 초기 코인 설정
        computer.resetCoin(10000);
        bettingSystem.resetPot(); //팟 리셋
        int anteAmount = 5000; // 엔티
        bettingSystem.collectAnte(user, anteAmount);
        bettingSystem.collectAnte(computer, anteAmount);
        userDiscardCompleted = false;
        userOpenCompleted = false;
        currentRound = INIT_DEAL;
        if (gui != null) gui.updateUI(); // GUI 상태 업데이트
        proceedToNextPhase(); // 첫 게임 단계 시작
    }
    
    //게임 이어하기 로직
    public void startNextHand() {
        // 앤티를 낼 코인이 있는지 확인
        if (user.getCoin() < ANTE_AMOUNT || computer.getCoin() < ANTE_AMOUNT) {
            gameTrulyOverNoMoney = true; // 없을 시 완전 종료 상태로 설정
            currentRound = GAME_OVER;    // GAME_OVER 상태 유지
            //보유 코인 < 엔티 시
            if (gui != null) {
                gui.showInfo("코인이 부족합니다. 게임을 재시작하세요.");
                gui.updateUI();
            }
            return;
        }
        gameTrulyOverNoMoney = false; //게임 가능 상태
        prepareNewHandAndDeal(); // 새 핸드 준비 및 첫 단계 진행
    }

    //새 핸드 준비 및 첫 단계
    private void prepareNewHandAndDeal() {
        deck.reset();
        user.clearAll();
        computer.clearAll();
        bettingSystem.resetPot();
        try {
            bettingSystem.collectAnte(user, ANTE_AMOUNT);
            bettingSystem.collectAnte(computer, ANTE_AMOUNT);
        } catch (InvalidBetException e) { //startNextHand에 예외 처리 선언 했기에 여기선 생략
            gameTrulyOverNoMoney = true;
            currentRound = GAME_OVER;
            return;
        }        
        userDiscardCompleted = false;
        userOpenCompleted = false;
        currentRound = INIT_DEAL; // 초기 카드 배분 단계로 설정

        if (gui != null) gui.updateUI();
        proceedToNextPhase();
    }

    //게임 이어하기 버튼 활성화 여부 판단 로직
    public boolean canContinueGame() {
        return user.getCoin() >= ANTE_AMOUNT && computer.getCoin() >= ANTE_AMOUNT && !gameTrulyOverNoMoney;
    }

    //게임 재시작만 가능한 상황인지 판단 로직
    public boolean isGameTrulyOverNoMoney() {
        return gameTrulyOverNoMoney;
    }
    
    //게임 다음 단게 진행
    public void proceedToNextPhase() {       
    	
    	//라운드 별 베팅 리셋
    	if (currentRound == FIRST_BETTING_ROUND_START ||
                currentRound == SECOND_BETTING_ROUND_START ||
                currentRound == THIRD_BETTING_ROUND_START ||
                currentRound == FOURTH_BETTING_ROUND_START ||
                currentRound == FIFTH_BETTING_ROUND_START) {
                bettingSystem.resetRound(); // 새 베팅 라운드 시작 전 상태 초기화 (currentBet = 0 포함
            }
    	
    	//플레이어 폴드 시 처리 로
        if (currentRound < SHOWDOWN_PHASE && currentRound != GAME_OVER &&
            (user.isFolded() || computer.isFolded())) {
            currentRound = SHOWDOWN_PHASE;
        }
        //게임 종료 시 중복 업데이트 방지
        if (gui != null && currentRound != GAME_OVER) {
            gui.updateUI();
       }
        
        //라운드 별 처리 로직
        switch (currentRound) {
        	// 4장씩 배분
            case INIT_DEAL:
            	
                for (int i = 0; i < 4; i++) {
                    user.receiveCard(deck.draw());
                    computer.receiveCard(deck.draw());
                }
                currentRound = DISCARD_OPEN_PHASE;
                if (gui != null) gui.updateUI();
                proceedToNextPhase();
                break;

             //카드 버리기 / 오픈 단계 처리 로직
            case DISCARD_OPEN_PHASE:
            	
                if (!userDiscardCompleted) {
                	
                    if (gui != null) gui.promptDiscardSelection();
                } else if (!userOpenCompleted) {
                	
                    if (gui != null) gui.promptOpenSelection();
                }
                break;
            //첫번째 베팅 라운드
            case FIRST_BETTING_ROUND_START:
                bettingSystem.resetRound(); 
                determineFirstTurnLeader(); //첫 턴 결정(베팅)
                if (gui != null) {
                    gui.updateUI();
                    gui.startBettingPhase(currentPlayer);
                }
                break;
                
            // 4번째카드 배분(오픈)
            case DEAL_4TH_STREET:
                dealNextCardOpen();
                currentRound = SECOND_BETTING_ROUND_START;
                if (gui != null) gui.updateUI();
                proceedToNextPhase();
                break;
                
            //두번째 베팅 라운드
            case SECOND_BETTING_ROUND_START:
                bettingSystem.resetRound();
                determineBettingLeaderByOpenCards();
                if (gui != null) {
                    gui.updateUI();
                    gui.startBettingPhase(currentPlayer);
                }
                break;
                
            //5번째 카드 배분(오픈)
            case DEAL_5TH_STREET:
                dealNextCardOpen();
                currentRound = THIRD_BETTING_ROUND_START;
                if (gui != null) gui.updateUI();
                proceedToNextPhase();
                break;
            
            //세번째 베팅 라운
            case THIRD_BETTING_ROUND_START:
                bettingSystem.resetRound();
                determineBettingLeaderByOpenCards();
                if (gui != null) {
                    gui.updateUI();
                    gui.startBettingPhase(currentPlayer);
                }
                break;
             
            //6번째 카드 배분
            case DEAL_6TH_STREET:
                dealNextCardOpen();
                currentRound = FOURTH_BETTING_ROUND_START;
                if (gui != null) gui.updateUI();
                proceedToNextPhase();
                break;
                
            //4번째 베팅 라운드
            case FOURTH_BETTING_ROUND_START:
                bettingSystem.resetRound();
                determineBettingLeaderByOpenCards();
                if (gui != null) {
                    gui.updateUI();
                    gui.startBettingPhase(currentPlayer);
                }
                break;

            //7번 째 카드 배분(히든)
            case DEAL_7TH_STREET:
                dealNextCardHidden();
                currentRound = FIFTH_BETTING_ROUND_START;
                if (gui != null) gui.updateUI();
                proceedToNextPhase();
                break;

            //5번째 베팅 라운
            case FIFTH_BETTING_ROUND_START:
                bettingSystem.resetRound();
                determineBettingLeaderByOpenCards();
                if (gui != null) {
                    gui.updateUI();
                    gui.startBettingPhase(currentPlayer);
                }
                break;
                
                
            //쇼다운(게임 종료)
            case SHOWDOWN_PHASE:
                String result = showdown(); //pot 승자에게 지급
                currentRound = GAME_OVER;   // 핸드 종료 상태로 전환
                if (gui != null) {
                    gui.displayGameResult(result);
                    gui.updateUI();
                }
                break;

            //게임 종료 후 재시작 or 이어하기 선택
            case GAME_OVER:
                if (gui != null) {
                    if(gameTrulyOverNoMoney) {
                        gui.showInfo(gui.getInfoLabelText() + "<br>코인이 부족하여 '게임 재시작'만 가능합니다.");
                    } else {
                        gui.showInfo(gui.getInfoLabelText() + "<br>'게임 이어하기' 또는 '게임 재시작'을 선택하세요.");
                    }
                }
                break;

            default:
                currentRound = GAME_OVER;
                if (gui != null) gui.updateUI();
                break;
        }
    }

    //카드 버리기 및 오픈 완료 로직
    public void userActionsForDiscardOpenCompleted(boolean isDiscardPhaseDone) {
        if (currentRound == DISCARD_OPEN_PHASE) {
            if (isDiscardPhaseDone && !userDiscardCompleted) {
                computerDiscardPhase(); // 컴퓨터 카드 버림
                userDiscardCompleted = true;
                if (gui != null) gui.updateUI(); // 컴퓨터가 카드를 버린 후 UI 업데이트
                proceedToNextPhase(); // 다시 DISCARD_OPEN_PHASE로 진입하여 오픈 단계 요청
            } else if (!isDiscardPhaseDone && userDiscardCompleted && !userOpenCompleted) {
                computerOpenPhase(); // 컴퓨터 카드 오픈
                userOpenCompleted = true;
                if (gui != null) gui.updateUI(); // 컴퓨터가 카드를 오픈한 후 UI 업데이트
                currentRound = FIRST_BETTING_ROUND_START; // 다음 단계는 첫 베팅 라운드
                proceedToNextPhase(); // 첫 베팅 라운드 시작
            }
        }
    }

   //베팅 완료 후 로직
    public void userBettingActionCompleted() {
        if (gui != null) gui.updateUI();
        checkBettingRoundOverAndProceed(); // 베팅 라운드 종료 여부 확인 및 다음 진행
    }

 // 컴퓨터 턴 진행 로직
    public void triggerComputerTurn() {        
        if (currentPlayer == computer && !computer.isFolded() && !user.isFolded()) { //모든 플레이어 폴드 안했을 시
            try {
                bettingSystem.autoAction(computer); // 자동 액션 처리
            } catch (Exception e) {
                e.printStackTrace();
                bettingSystem.fold(computer); // 예외 발생 시 안전하게 폴드 처리
                computer.setActedThisRound(true); // 액션한 것으로 처리
            }
            checkBettingRoundOverAndProceed();
        } else { 
            if (bettingSystem.isBettingOver(user, computer)) {
                checkBettingRoundOverAndProceed();
            } else if (currentPlayer == user && gui != null) {
                gui.startBettingPhase(user);
            } else {
                if (!user.isFolded() && !computer.isFolded() && currentPlayer != computer && gui != null) {
                    nextTurn();
                    gui.startBettingPhase(user);
                }
            }
        }
    }

    //베팅 라운드 종료 확인 후 다음 액션 처리 로직
    public void checkBettingRoundOverAndProceed() {
        if (bettingSystem.isBettingOver(user, computer)) {
            switch (currentRound) {
                case FIRST_BETTING_ROUND_START: currentRound = DEAL_4TH_STREET; break;
                case SECOND_BETTING_ROUND_START: currentRound = DEAL_5TH_STREET; break;
                case THIRD_BETTING_ROUND_START: currentRound = DEAL_6TH_STREET; break;
                case FOURTH_BETTING_ROUND_START: currentRound = DEAL_7TH_STREET; break;
                case FIFTH_BETTING_ROUND_START: currentRound = SHOWDOWN_PHASE; break;
                
                default:
                   currentRound = SHOWDOWN_PHASE;
            }
            proceedToNextPhase(); // 다음 게임 단계로 진행
        } else {
            nextTurn(); // 다음 플레이어로 턴 넘김 (currentPlayer 변경)
            if (gui != null) gui.updateUI(); // 턴 변경 후 UI 업데이트

            if (currentPlayer == computer) {
                triggerComputerTurn();
            } else { // 사용자 턴
                if (gui != null) gui.startBettingPhase(currentPlayer);
            }
        }
    }

    //카드 조작 및 선 플레이어 결정 메서드//
    //유저 카드 버리기 단계
    public void userDiscardPhase(Card card) {
        user.setSelectedDiscardCard(card);
        Card discarded = user.chooseCardToDiscard();
    }
    
    //컴퓨터 카드 버리기 단계
    public void computerDiscardPhase() {
        Card discarded = computer.chooseCardToDiscard();
    }
    
    //사용자 카드 오픈 단계
    public void userOpenPhase(Card card) {
        user.setSelectedOpenCard(card);
        Card opened = user.chooseCardToOpen();
    }
    
    //컴퓨터 카드 오픈 단계
    public void computerOpenPhase() {
        Card opened = computer.chooseCardToOpen();
    }
    
    //플레이어에게 카드 배분 단계
    public void dealNextCardOpen() {
        Card userCard = deck.draw();
        user.receiveOpenCard(userCard);
        Card computerCard = deck.draw();
        computer.receiveOpenCard(computerCard);
    }

    //7번째 히든 카드 배분 단계
    public void dealNextCardHidden() {
        Card userCard = deck.draw();
        user.receiveHiddenCard(userCard);
        Card computerCard = deck.draw();
        computer.receiveHiddenCard(computerCard);
    }
    
    //첫 턴 결정 로직
    public void determineFirstTurnLeader() {
        List<Card> userOpen = user.getOpenCards();
        List<Card> computerOpen = computer.getOpenCards();
        
        //첫 번째 오픈 카드 기준으로 선 플레이어 결정 로직
        Card userHigh = (userOpen != null && !userOpen.isEmpty()) ? userOpen.get(0) : null;
        Card computerHigh = (computerOpen != null && !computerOpen.isEmpty()) ? computerOpen.get(0) : null;

        if (userHigh == null && computerHigh == null) {
            currentPlayer = (Math.random() < 0.5) ? user : computer;
        } else if (userHigh == null) {
            currentPlayer = computer;
        } else if (computerHigh == null) {
            currentPlayer = user;
        } else {
            if (userHigh.getRank() > computerHigh.getRank()) currentPlayer = user;
            else if (userHigh.getRank() < computerHigh.getRank()) currentPlayer = computer;
            else { // 랭크가 같으면 무늬로 (게임 규칙에 따라 다를 수 있음)
                if (userHigh.getSuit().ordinal() > computerHigh.getSuit().ordinal()) currentPlayer = user;
                else if (userHigh.getSuit().ordinal() < computerHigh.getSuit().ordinal()) currentPlayer = computer;
                else currentPlayer = (Math.random() < 0.5) ? user : computer;
            }
        }
    }

    //오픈된 카드 기준으로 선 베팅 플레이어 결정
    public void determineBettingLeaderByOpenCards() {
        HandEvaluationResult userOpenEval = HandEvaluator.evaluatePartialHand(user.getOpenCards());//유저 오픈 카드 족보 평가
        HandEvaluationResult computerOpenEval = HandEvaluator.evaluatePartialHand(computer.getOpenCards());// 컴퓨터 오픈 카드 족보 평가

        int comparison = HandEvaluationResult.compare(userOpenEval, computerOpenEval);//플레이어 오픈 카드 족보 비교
        if (comparison > 0) currentPlayer = user;
        else if (comparison < 0) currentPlayer = computer;
    }
    
    //턴 변경
    public void nextTurn() {
        currentPlayer = getOpponent(currentPlayer);
    }

    //쇼다운 처리 및 승자 결정
    public String showdown() {
        if (user.isFolded()) {
            bettingSystem.awardPot(computer);
            return getPlayerName(computer) + " 승리! (유저가 폴드했습니다!)";
        }
        if (computer.isFolded()) {
            bettingSystem.awardPot(user);
            return getPlayerName(user) + " 승리! (컴퓨터가 폴드했습니다!)";
        }

        //7장 전체 카드로 최종 족보 평가
        HandEvaluationResult userResult = HandEvaluator.evaluateHand(user.getHand());
        HandEvaluationResult computerResult = HandEvaluator.evaluateHand(computer.getHand());

        String userInfo = String.format("%s: %s (패: %s)", getPlayerName(user), userResult.getHandRank(), userResult.getDeterminingCards());
        String computerInfo = String.format("%s: %s (패: %s)", getPlayerName(computer), computerResult.getHandRank(), computerResult.getDeterminingCards());
        
        Player winner;
        int comparison = HandEvaluationResult.compare(userResult, computerResult);

        if (comparison > 0) winner = user;
        else if (comparison < 0) winner = computer;
        else {
            winner = computer;
        }
        
        bettingSystem.awardPot(winner);
        String winnerText = getPlayerName(winner) + " 승리!";
        
        return userInfo + "\n" + computerInfo + "\n\n" + winnerText;
    }

    // 플레이어 이름 반환
    public String getPlayerName(Player player) {
        if (player == null) return "N/A";
        if (player == user) return "User";
        if (player == computer) return "Computer";
        return "UnknownPlayer";
    }

}