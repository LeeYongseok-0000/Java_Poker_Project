package ui;

import game.*;
import player.ComputerPlayer; // ComputerPlayer 타입 확인용
import player.Player;       // Player 타입 확인용
import player.UserPlayer;   // UserPlayer 타입 확인용
import card.Card;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import exception.InvalidBetException;

public class GameGUI extends JFrame {

    private GameManager manager;

    // UI 컴포넌트 (이전과 동일)
    private JLabel potLabel = new JLabel("Pot: 0");
    private JLabel userCoinLabel = new JLabel("User Coin: 0");
    private JLabel computerCoinLabel = new JLabel("Computer Coin: 0");
    private JLabel currentBetLabel = new JLabel("라운드 베팅: 0");
    private JLabel infoLabel = new JLabel("게임 시작을 눌러주세요.", SwingConstants.CENTER);
    private JButton startButton = new JButton("게임 시작");
    private JButton restartButton = new JButton("게임 재시작");
    private JButton continueButton = new JButton("게임 이어하기");
    private JButton foldButton = new JButton("폴드");
    private JButton checkButton = new JButton("체크");
    private JButton callButton = new JButton("콜");
    private JButton halfRaiseButton = new JButton("하프");
    private JButton quarterRaiseButton = new JButton("쿼터");
    

    private JPanel userCardDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    private JPanel computerOpenDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    private enum CardSelectionPurpose { DISCARD, OPEN } //카드 선택 목적
    private CardSelectionPurpose currentCardSelectionPurpose;
    private volatile boolean awaitingUserInput = false; //사용자 입력(카드 선택, 베팅) 대기 상태

    public GameGUI(GameManager gameManager) {
    	this.manager = gameManager;
    	
        if (this.manager != null) {
            this.manager.setGUI(this);
        } else {//manager가 null인 경우의 예외 처리        
            this.manager = new GameManager();
            this.manager.setGUI(this);
        }
        
        setTitle("세븐포커");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        setupLayout();
        initializeButtonListeners();

        // 초기 UI 상태 설정
        restartButton.setVisible(false);
        updateUI(); // GameManager의 초기 상태를 반영하여 UI 업데이트
        
    }

    //버튼 기능
    private void initializeButtonListeners() {
    	 startButton.addActionListener(e -> {             
             manager.startGame(); // 코인 포함 모든 것 초기화 후 첫 핸드 시작
         });

         restartButton.addActionListener(e -> {
             manager.startGame(); // 코인 포함 모든 것 초기화 후 첫 핸드 시작
         });

         continueButton.addActionListener(e -> {
             manager.startNextHand(); // 코인 유지한 채 다음 핸드 시작
         });

        foldButton.addActionListener(e -> handleUserBettingAction(() -> manager.getBettingSystem().fold(manager.getUser()), "User 폴드."));
        checkButton.addActionListener(e -> handleUserBettingAction(() -> manager.getBettingSystem().check(manager.getUser()), "User 체크."));
        callButton.addActionListener(e -> handleUserBettingAction(() -> manager.getBettingSystem().call(manager.getUser()), "User 콜."));
        halfRaiseButton.addActionListener(e -> handleUserBettingAction(() -> manager.getBettingSystem().halfRaise(manager.getUser()), "User 하프 레이즈."));
        quarterRaiseButton.addActionListener(e -> handleUserBettingAction(() -> manager.getBettingSystem().quarterRaise(manager.getUser()), "User 쿼터 레이즈."));
    }

    // 화면 레이아웃
    private void setupLayout() {
        JPanel mainPanel = (JPanel) getContentPane();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 컴퓨터 패널 설정
        JPanel computerPanel = new JPanel(new BorderLayout(5, 5));
        computerPanel.setBorder(BorderFactory.createTitledBorder("Computer"));

        JPanel computerInfoSubPanel = new JPanel();
        computerInfoSubPanel.add(computerCoinLabel);
        computerPanel.add(computerInfoSubPanel, BorderLayout.NORTH);

        computerOpenDisplayPanel.setPreferredSize(new Dimension(760, 110));
        JScrollPane computerScrollPane = new JScrollPane(computerOpenDisplayPanel);
        computerScrollPane.setPreferredSize(new Dimension(780, 140));
        computerPanel.add(computerScrollPane, BorderLayout.CENTER);


        // 중앙 정보 패널 설정 (이전과 동일)
        JPanel centerAreaPanel = new JPanel(new BorderLayout(5, 5));
        infoLabel.setPreferredSize(new Dimension(300, 80));
        centerAreaPanel.add(infoLabel, BorderLayout.NORTH);

        JPanel potInfoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        potInfoPanel.add(potLabel);
        potInfoPanel.add(currentBetLabel);
        centerAreaPanel.add(potInfoPanel, BorderLayout.CENTER);


        // 유저 카드 표시 패널 설정
        userCardDisplayPanel.setPreferredSize(new Dimension(760, 110));

        JScrollPane userScrollPane = new JScrollPane(userCardDisplayPanel);
        userScrollPane.setPreferredSize(new Dimension(780, 140));


        // 유저 정보 패널 설정
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        userPanel.setBorder(BorderFactory.createTitledBorder("User"));

        JPanel userInfoSubPanel = new JPanel();
        userInfoSubPanel.add(userCoinLabel);
        userPanel.add(userInfoSubPanel, BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);


        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(startButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(continueButton);
        buttonPanel.add(foldButton);
        buttonPanel.add(checkButton);
        buttonPanel.add(callButton);
        buttonPanel.add(halfRaiseButton);
        buttonPanel.add(quarterRaiseButton);     


        // 하단 패널 (유저 패널과 버튼 패널 수직 배치)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(userPanel);
        bottomPanel.add(buttonPanel);

        
        add(computerPanel, BorderLayout.NORTH);
        add(centerAreaPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    //GameManager로부터 호출되는 메서드//

    //사용자에게 버릴 카드를 선택하도록 요청
    public void promptDiscardSelection() {
        SwingUtilities.invokeLater(() -> {
            showInfo("버릴 카드 1장을 선택하세요.");
            currentCardSelectionPurpose = CardSelectionPurpose.DISCARD;
            awaitingUserInput = true;
            updateUI(); // UI 갱신 (카드 클릭 가능하도록, 베팅 버튼 비활성화)
        });
    }

    //GameManager가 사용자에게 오픈할 카드를 선택하도록 요청
    public void promptOpenSelection() {
        SwingUtilities.invokeLater(() -> {
            showInfo("오픈할 카드 1장을 선택하세요.");
            currentCardSelectionPurpose = CardSelectionPurpose.OPEN;
            awaitingUserInput = true;
            updateUI();
        });
    }

    /** GameManager가 특정 플레이어부터 베팅을 시작하도록 GUI에 알립니다. */
    public void startBettingPhase(Player startingPlayer) {
        SwingUtilities.invokeLater(() -> {
            String startingPlayerName = manager.getPlayerName(startingPlayer);           
            showInfo(manager.getPlayerName(startingPlayer) + " 베팅 차례!");
            currentCardSelectionPurpose = null; // 카드 선택 모드 해제
            
            if (startingPlayer == manager.getUser() && !manager.getUser().isFolded()) {
                awaitingUserInput = true;
            } else if (startingPlayer == manager.getComputer() && !manager.getComputer().isFolded()) {
                awaitingUserInput = false; // 컴퓨터 턴 시작 전 딜레이
                new Timer(1000, e -> {
                    ((Timer)e.getSource()).stop(); // 타이머 종료
                    manager.triggerComputerTurn();
                }).start();               
            } else {
                // 선플레이어가 유저도 컴퓨터도 아니거나, 유효한 상태가 아닌 경우
                awaitingUserInput = false;
                // 이런 경우 베팅이 즉시 종료될 수 있으므로 GameManager에 확인 요청
                manager.checkBettingRoundOverAndProceed();
            }
            updateUI(); // UI 업데이트는 모든 조건 분기 후 한 번 호출
        });
    }

    //게임 결과를 GUI에 표시하도록 요청
    public void displayGameResult(String resultMessage) {
    	SwingUtilities.invokeLater(() -> {
            showInfo(resultMessage); //
        });
    }
    
   
    public void updateUI() {
        SwingUtilities.invokeLater(() -> {  
        	
            //베팅 및 코인 정보 업데이트 (유지)
            potLabel.setText("Pot: " + manager.getBettingSystem().getPot());
            currentBetLabel.setText("라운드 베팅: " + manager.getBettingSystem().getCurrentBet());
            if (manager.getUser() != null) userCoinLabel.setText("User 코인: " + manager.getUser().getCoin());
            if (manager.getComputer() != null) computerCoinLabel.setText("Computer 코인: " + manager.getComputer().getCoin());

            //카드 표시 업데이트 (유지)
            boolean isSelectionPhase = manager.getCurrentRound() == GameManager.DISCARD_OPEN_PHASE && awaitingUserInput;
            updateUserCardDisplay(isSelectionPhase);
            updateComputerCardDisplay(manager.getCurrentRound() == GameManager.SHOWDOWN_PHASE || manager.getCurrentRound() == GameManager.GAME_OVER);

            //버튼 상태 업데이트
            boolean isHandOver = (manager.getCurrentRound() == GameManager.GAME_OVER);
            boolean isTrulyInitialState = (manager.getCurrentRound() == 0 && 
                                           (manager.getUser() == null || manager.getUser().getHand().isEmpty()));
            
            // gameIsActivelyPlaying: 실제 게임 플레이가 진행 중인 상태 (카드 배분 후 ~ 쇼다운 전)
            boolean gameIsActivelyPlaying = !isTrulyInitialState && !isHandOver &&
                                            (manager.getCurrentRound() >= GameManager.INIT_DEAL && 
                                             manager.getCurrentRound() < GameManager.SHOWDOWN_PHASE);

            if (isHandOver) {
                // 핸드가 종료된 상태: "게임 재시작" 또는 "게임 이어하기"
                startButton.setEnabled(false);
                startButton.setVisible(false);

                restartButton.setEnabled(true);
                restartButton.setVisible(true);

                if (continueButton != null) {
                    continueButton.setEnabled(manager.canContinueGame()); // GameManager가 이어하기 가능 여부 판단
                    continueButton.setVisible(true);
                }

                disableBettingButtons(); // 핸드 종료 시 베팅 버튼들은 모두 비활성화
            } else if (isTrulyInitialState) {
                // 게임이 아직 시작되지 않은 완전 초기 상태: "게임 시작" 버튼만 활성화
                startButton.setEnabled(true);
                startButton.setVisible(true);

                restartButton.setEnabled(false);
                restartButton.setVisible(false);
                if (continueButton != null) {
                    continueButton.setEnabled(false);
                    continueButton.setVisible(false);
                }

                disableBettingButtons();
                
            } else { // 게임 진행 중 (카드 배분, 카드 선택, 베팅 등)
                startButton.setEnabled(false);
                startButton.setVisible(false);
                restartButton.setEnabled(false);
                restartButton.setVisible(false);
                
                if (continueButton != null) {
                    continueButton.setEnabled(false);
                    continueButton.setVisible(false);
                }

                // 베팅 버튼 활성화 로직
                if (awaitingUserInput && currentCardSelectionPurpose != null) { // 카드 선택 모드일 때
                    disableBettingButtons(); // 베팅 버튼 비활성화
                } else if (gameIsActivelyPlaying && awaitingUserInput && 
                           manager.getCurrentPlayer() == manager.getUser() && 
                           !manager.getUser().isFolded()) {
                    enableBettingButtonsForUser(); // 사용자 턴이고 베팅 가능 상태면 활성화
                } else { // 컴퓨터 턴이거나 사용자가 액션을 취할 수 없는 다른 상태
                    disableBettingButtons();
                }
            }
            if (isTrulyInitialState) {
                 showInfo("게임 시작을 눌러주세요.");
            }
            this.revalidate();
            this.repaint();
        });
    }
    public String getInfoLabelText() {
        return infoLabel.getText(); // HTML 태그 포함된 텍스트 반환
    }
    
    private void handleUserCardSelection(Card selectedCard) {
        if (!awaitingUserInput || currentCardSelectionPurpose == null) {
            return;
        }
        awaitingUserInput = false; 
        disableBettingButtons();   //카드 선택 중에는 베팅 버튼 비활성화

        if (currentCardSelectionPurpose == CardSelectionPurpose.DISCARD) {
            manager.userDiscardPhase(selectedCard);
            manager.userActionsForDiscardOpenCompleted(true); 
            
        } else if (currentCardSelectionPurpose == CardSelectionPurpose.OPEN) {
            manager.userActionsForDiscardOpenCompleted(false);
        }
    }

    private void handleUserBettingAction(Runnable gameLogicAction, String baseActionMessage) {
        if (!awaitingUserInput || manager.getCurrentPlayer() != manager.getUser()) {
            // 사용자 턴이 아니면 아무것도 하지 않음
            return;
        }
        // UserPlayer가 폴드했거나, 돈이 없는데 추가 베팅이 필요한 상황이면 액션 제한 가능
        if (manager.getUser().isFolded() || 
            (manager.getUser().getCoin() == 0 && manager.getBettingSystem().getCurrentBet() > manager.getUser().getCurrentBet())) {
            showInfo("이미 폴드했거나 올인 상태로 추가 액션이 불가합니다.");
            return;
        }

        try {
            gameLogicAction.run();
            showInfo(baseActionMessage);
            
            awaitingUserInput = false;
            disableBettingButtons(); // 다음 상태(컴퓨터 턴 또는 다음 라운드)로 넘어가기 전까지 버튼 비활성화
            manager.userBettingActionCompleted();

        } catch (InvalidBetException e) { // BettingSystem에서 발생한 베팅 규칙 위반 예외
            showInfo("오류: " + e.getMessage()); // 게임 내에 오류 메시지 표시
            awaitingUserInput = true; 
        } catch (Exception e) { // 그 외 예상치 못한 예외
            e.printStackTrace();
            showInfo("시스템 오류 발생: " + e.getMessage() + "\n게임을 재시작해야 할 수 있습니다.");
            awaitingUserInput = false;
            disableBettingButtons();
        }
    }
    
    
    private void updateUserCardDisplay(boolean selectionEnabled) {

        userCardDisplayPanel.removeAll();
        UserPlayer user = manager.getUser();
        if (user == null || user.getHand() == null) return;
        
        if (user == null || user.getHand() == null) {
            userCardDisplayPanel.revalidate();
            userCardDisplayPanel.repaint();
            return;
        }
        
        List<Card> userHand = user.getHand(); // 히든/오픈 구분 없이 사용자의 모든 카드    

        for (Card card : userHand) {
            String cardText = card.toString();
            JButton cardButton = new JButton(cardText);
            cardButton.setPreferredSize(new Dimension(75, 110));
            cardButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));

            // 문양별 색상 지정
            if (card.getSuit() == Card.Suit.DIAMONDS || card.getSuit() == Card.Suit.HEARTS) {
                cardButton.setForeground(Color.RED);
            } else {
                cardButton.setForeground(Color.BLACK);
            }


            if (selectionEnabled && awaitingUserInput) {
                if (currentCardSelectionPurpose == CardSelectionPurpose.DISCARD ||
                   (currentCardSelectionPurpose == CardSelectionPurpose.OPEN && !manager.getUser().getOpenCards().contains(card))) {
                   cardButton.addActionListener(e -> handleUserCardSelection(card));
               } else {
                   cardButton.setEnabled(false);
               }
           } else {
               cardButton.setEnabled(false);
           }
           
           userCardDisplayPanel.add(cardButton);
       }  
        userCardDisplayPanel.revalidate();
        userCardDisplayPanel.repaint();
    }

    private void updateComputerCardDisplay(boolean showAllComputerCards) {
        computerOpenDisplayPanel.removeAll();
        ComputerPlayer computer = manager.getComputer();
        if (computer == null || computer.getHand() == null) return;
        List<Card> computerHand = computer.getHand();
        List<Card> computerOpenCards = computer.getOpenCards();

        for (Card card : computerHand) {
            String cardText;
            if (showAllComputerCards || computerOpenCards.contains(card)) {
                cardText = card.toString();
            } else {
                cardText = "?";
            }
            JButton cardButton = new JButton(cardText);
            cardButton.setPreferredSize(new Dimension(75, 110));
            cardButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16)); // 폰트 이름과 크기 조절

            // 문양별 색상 지정: 다이아몬드와 하트는 빨간색, 나머지는 검정색
            if (card.getSuit() == Card.Suit.DIAMONDS || card.getSuit() == Card.Suit.HEARTS) {
                cardButton.setForeground(Color.RED);
            } else {
                cardButton.setForeground(Color.BLACK);
            }

            cardButton.setEnabled(false); // 컴퓨터 카드는 항상 클릭 비활성화
            computerOpenDisplayPanel.add(cardButton);
        }
        computerOpenDisplayPanel.revalidate();
        computerOpenDisplayPanel.repaint();
    }

    private void enableBettingButtonsForUser() {
        Player user = manager.getUser();
        boolean canAct = !user.isFolded() && 
                         !(user.getCoin() == 0 && manager.getBettingSystem().getCurrentBet() > user.getCurrentBet());
        
        // 사용자가 행동할 수 있는 상태일 때만 버튼들을 기본적으로 활성화
        foldButton.setEnabled(canAct);
        checkButton.setEnabled(canAct);
        callButton.setEnabled(canAct);
        quarterRaiseButton.setEnabled(canAct && user.getCoin() > 0); // 레이즈는 최소한 코인이 있어야 시도 가능
        halfRaiseButton.setEnabled(canAct && user.getCoin() > 0);
    }

    private void disableBettingButtons() {
        foldButton.setEnabled(false);
        checkButton.setEnabled(false);
        callButton.setEnabled(false);
        halfRaiseButton.setEnabled(false);
        quarterRaiseButton.setEnabled(false);
    }

    public void showInfo(String message) {
        // HTML을 사용하여 여러 줄 메시지 및 중앙 정렬 지원
        infoLabel.setText("<html><div style='text-align: center; padding: 5px;'>" + message.replaceAll("\n", "<br>") + "</div></html>");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameManager gameManager = new GameManager();
            GameGUI gui = new GameGUI(gameManager);
            gui.setVisible(true);
        });
    }
}