package game;

import card.Card;
import card.Card.Suit; // Card 클래스 내부의 Suit enum 사용

import java.util.*;
import java.util.stream.Collectors;

//카드 족보 덱 설정 로직
public class HandEvaluator {

    // 카드 랭크 값 상수 설정
    private static final int ACE_RANK = 14;    // 에이스:A
    private static final int KING_RANK = 13;   // 킹:K
    private static final int QUEEN_RANK = 12;  // 퀸:Q
    private static final int JACK_RANK = 11;   // 잭:J
    private static final int FIVE_RANK = 5;    // 5 (A-5 스트레이트 확인용)
    private static final int TWO_RANK = 2;     // 2 (A-5 스트레이트 확인용)


    //7장의 카드 전체에서 가장 좋은 5장의 조합으로 족보를 평가    
    public static HandEvaluationResult evaluateHand(List<Card> sevenCards) {
        if (sevenCards == null || sevenCards.size() != 7) {
            throw new IllegalArgumentException("7장의 카드가 필요합니다.");
        }

        // 7장의 카드에서 5장을 선택하는 모든 가능한 조합
        List<List<Card>> allFiveCardCombinations = new ArrayList<>();
        generateCombinations(sevenCards, 5, 0, new ArrayList<>(), allFiveCardCombinations);

        HandEvaluationResult bestResult = null; // 가장 좋은 족보 결과를 저장할 변수

        // 모든 5장 조합에 대해 족보를 평가하고 가장 높은 족보
        for (List<Card> fiveCardHand : allFiveCardCombinations) {
            HandEvaluationResult currentResult = evaluate5CardHand(fiveCardHand);
            // 현재 조합의 족보가 이전에 찾은 최상의 족보보다 높으면 교체
            if (bestResult == null || HandEvaluationResult.compare(currentResult, bestResult) > 0) {
                bestResult = currentResult;
            }
        }
        return bestResult;
    }

    //주어진 카드 리스트에서 k(5)개의 카드를 선택하는 모든 조합을 생성 
    private static void generateCombinations(List<Card> allCards, int k, int start,
                                             List<Card> currentCombination, List<List<Card>> combinations) {
        if (k == 0) { // k개의 카드를 모두 선택한 경우
            combinations.add(new ArrayList<>(currentCombination)); // 현재 조합을 결과 리스트에 추가
            return;
        }
        for (int i = start; i <= allCards.size() - k; i++) {
            currentCombination.add(allCards.get(i)); // 카드 선택
            generateCombinations(allCards, k - 1, i + 1, currentCombination, combinations); // 다음 카드 선택
            currentCombination.remove(currentCombination.size() - 1); // 선택했던 카드 되돌리기
        }
    }

    //정확히 5장의 카드로 구성된 패의 족보를 평가
 static HandEvaluationResult evaluate5CardHand(List<Card> fiveCards) {
        if (fiveCards == null || fiveCards.size() != 5) {
            throw new IllegalArgumentException("5장의 카드가 필요합니다.");
        }

        // 카드를 랭크(숫자)가 높은 순서대로 정렬
        List<Card> sortedCards = fiveCards.stream()
                .sorted(Comparator.comparingInt(Card::getRank).reversed())
                .collect(Collectors.toList());

        // 각 랭크(숫자)별로 카드가 몇 장 있는지 계산
        Map<Integer, Long> rankCounts = sortedCards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        boolean isFlush = isFlush(sortedCards);         // 모든 카드의 무늬가 같은지 (플러시 체크)
        boolean isStraight = isStraight(sortedCards);   // 카드 랭크가 연속되는지 (스트레이트 체크)

        //스트레이트 플러시 (로얄 플러시 포함)
        if (isStraight && isFlush) {        
            if (sortedCards.get(0).getRank() == ACE_RANK &&
                sortedCards.get(4).getRank() == FIVE_RANK &&
                sortedCards.stream().anyMatch(c -> c.getRank() == 4) && // 4가 있는지 확인 (A,2,3,4,5 구성 확인)
                sortedCards.stream().anyMatch(c -> c.getRank() == 3) &&
                sortedCards.stream().anyMatch(c -> c.getRank() == TWO_RANK) &&
                !sortedCards.stream().anyMatch(c -> c.getRank() == KING_RANK)) { // K가 없어야 함(AKQJT 스트레이트와 구분)

                 List<Card> aceLowStraightFlushCards = new ArrayList<>();
                 aceLowStraightFlushCards.add(sortedCards.stream().filter(c -> c.getRank() == FIVE_RANK).findFirst().get());
                 aceLowStraightFlushCards.add(sortedCards.stream().filter(c -> c.getRank() == 4).findFirst().get());
                 aceLowStraightFlushCards.add(sortedCards.stream().filter(c -> c.getRank() == 3).findFirst().get());
                 aceLowStraightFlushCards.add(sortedCards.stream().filter(c -> c.getRank() == TWO_RANK).findFirst().get());
                 aceLowStraightFlushCards.add(sortedCards.stream().filter(c -> c.getRank() == ACE_RANK).findFirst().get()); // A를 마지막에 (낮은 순으로)
                 return new HandEvaluationResult(HandRank.STRAIGHT_FLUSH, aceLowStraightFlushCards);
            }
            // 일반적인 스트레이트 플러시 또는 로얄 플러시 (A K Q J T) T: 넘버
            return new HandEvaluationResult(HandRank.STRAIGHT_FLUSH, new ArrayList<>(sortedCards));
        }

        // 포카드 (같은 랭크 4장) 판별
        Optional<Map.Entry<Integer, Long>> fourOfAKindEntry = rankCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 4).findFirst();//랭크 별 카드 개수 중 4장 유무 확인
        
        if (fourOfAKindEntry.isPresent()) {
            int fourRank = fourOfAKindEntry.get().getKey(); // 포카드를 이루는 랭크
            //4장 필터링 후 리스트 생성
            List<Card> determiningCards = sortedCards.stream().filter(c -> c.getRank() == fourRank).collect(Collectors.toList());
            
            return new HandEvaluationResult(HandRank.FOUR_CARD, determiningCards);
        }

        // 풀 하우스 (트리플 + 페어)
        Optional<Map.Entry<Integer, Long>> tripleEntry = rankCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 3).findFirst(); // 3장 같은 랭크 (트리플)



        // 풀 하우스에 사용할 페어 조회
        Optional<Map.Entry<Integer, Long>> pairEntryForFullHouse = rankCounts.entrySet().stream()
            .filter(entry -> {
            	
                boolean hasAtLeastTwoCards = entry.getValue().longValue() >= 2L; //2장 이상인지
                boolean isNotTripleRank = true;									 //트리플 랭크와 동일 여부
                
                if (tripleEntry.isPresent()) {
                	//동일 시 제외
                    isNotTripleRank = !entry.getKey().equals(tripleEntry.get().getKey());
                }
                return hasAtLeastTwoCards && isNotTripleRank;
            })
            //가장 높은 페어 선택
            .max((e1, e2) -> e1.getKey().compareTo(e2.getKey()));        

       //풀하우스 조건(트리플, 페어 존재)
        if (tripleEntry.isPresent() && 
            pairEntryForFullHouse.isPresent() && // isPresent()로 값이 있는지 확인
            pairEntryForFullHouse.get().getValue().longValue() >= 2L) { // getValue 호출 전에 .get으로 실제 Entry 객체를 가져옴

             List<Card> determiningCards = new ArrayList<>();
             // 트리플 카드 추가
             determiningCards.addAll(sortedCards.stream().filter(c->c.getRank() == tripleEntry.get().getKey()).collect(Collectors.toList()));             
           
             // 페어 카드 추가
             determiningCards.addAll(sortedCards.stream().filter(c->c.getRank() == pairEntryForFullHouse.get().getKey()).limit(2).collect(Collectors.toList()));
             
             return new HandEvaluationResult(HandRank.FULL_HOUSE, determiningCards);
        }
        
        // 플러시
        if (isFlush) {
            // 5장의 카드 모두 높은 순으로 저장
            return new HandEvaluationResult(HandRank.FLUSH, new ArrayList<>(sortedCards));
        }

        // 스트레이트 (스트레이트 플러시가 아닌 경우)
        if (isStraight) {
            // A-5 스트레이트 플러시 조건 확인
            if (sortedCards.get(0).getRank() == ACE_RANK && //A 유무 확인(가장 큰 카드)
                sortedCards.get(4).getRank() == FIVE_RANK &&//5 유무 확인(가장 작은 카드)
                sortedCards.stream().anyMatch(c -> c.getRank() == 4) && //4 유무 확인
                sortedCards.stream().anyMatch(c -> c.getRank() == 3) &&//3 유무 확인
                sortedCards.stream().anyMatch(c -> c.getRank() == TWO_RANK) &&//2유무 확인
                !sortedCards.stream().anyMatch(c -> c.getRank() == KING_RANK)) { // K가 유무 확인
            	//조건 만족 시 리스트 생성
                 List<Card> aceLowStraightCards = new ArrayList<>();
                 aceLowStraightCards.add(sortedCards.stream().filter(c -> c.getRank() == FIVE_RANK).findFirst().get());
                 aceLowStraightCards.add(sortedCards.stream().filter(c -> c.getRank() == 4).findFirst().get());
                 aceLowStraightCards.add(sortedCards.stream().filter(c -> c.getRank() == 3).findFirst().get());
                 aceLowStraightCards.add(sortedCards.stream().filter(c -> c.getRank() == TWO_RANK).findFirst().get());
                 aceLowStraightCards.add(sortedCards.stream().filter(c -> c.getRank() == ACE_RANK).findFirst().get());
                 return new HandEvaluationResult(HandRank.STRAIGHT, aceLowStraightCards);
            }
            // 일반적인 스트레이트
            return new HandEvaluationResult(HandRank.STRAIGHT, new ArrayList<>(sortedCards));
        }

        // 트리플 (포카드나 풀하우스가 아닌 경우)
        if (tripleEntry.isPresent()) {
            int tripleRank = tripleEntry.get().getKey();
            List<Card> determiningCards = sortedCards.stream().filter(c -> c.getRank() == tripleRank).collect(Collectors.toList());
            // 나머지 2장의 높은 카드 추가
            List<Card> kickers = sortedCards.stream().filter(c -> c.getRank() != tripleRank)
                                   .limit(2).collect(Collectors.toList());
            determiningCards.addAll(kickers);
            return new HandEvaluationResult(HandRank.TRIPS, determiningCards);
        }

        // 투 페어
        List<Map.Entry<Integer, Long>> pairs = rankCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 2) // 정확히 2장인 랭크 (페어)
                .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey())) // 높은 랭크의 페어부터 정렬
                .collect(Collectors.toList());
        if (pairs.size() >= 2) { // 페어가 2개 이상이면 투 페어
            int highPairRank = pairs.get(0).getKey(); // 가장 높은 페어의 랭크
            int lowPairRank = pairs.get(1).getKey();  // 두 번째로 높은 페어의 랭크
            List<Card> determiningCards = new ArrayList<>();
            determiningCards.addAll(sortedCards.stream().filter(c->c.getRank() == highPairRank).collect(Collectors.toList()));
            determiningCards.addAll(sortedCards.stream().filter(c->c.getRank() == lowPairRank).collect(Collectors.toList()));
           
            Card kicker = sortedCards.stream().filter(c -> c.getRank() != highPairRank && c.getRank() != lowPairRank)
                                .findFirst().orElse(null);
            if(kicker != null) determiningCards.add(kicker);
            return new HandEvaluationResult(HandRank.TWO_PAIR, determiningCards);
        }

        // 원 페어
        if (pairs.size() == 1) { // 페어가 정확히 1개이면 원 페어
            int pairRank = pairs.get(0).getKey();
            List<Card> determiningCards = sortedCards.stream().filter(c -> c.getRank() == pairRank).collect(Collectors.toList());
          
            List<Card> kickers = sortedCards.stream().filter(c -> c.getRank() != pairRank)
                                   .limit(3).collect(Collectors.toList());
            determiningCards.addAll(kickers);
            return new HandEvaluationResult(HandRank.ONE_PAIR, determiningCards);
        }

        // 하이 카드 (위의 어떤 족보도 해당하지 않는 경우)
        // 5장의 카드를 높은 순으로 저장
        return new HandEvaluationResult(HandRank.HIGH_CARD, new ArrayList<>(sortedCards));
    }

    // 주어진 카드 리스트가 플러시(모든 카드의 무늬가 동일)인지 확인
    private static boolean isFlush(List<Card> cards) {
        if (cards == null || cards.isEmpty() || cards.size() < 5) return false; // 플러시는 5장의 카드가 필요
        Suit firstSuit = cards.get(0).getSuit(); // 첫 번째 카드의 무늬
        // 모든 카드의 무늬가 첫 번째 카드의 무늬와 같은지 확인
        return cards.stream().allMatch(card -> card.getSuit() == firstSuit);
    }

    //정렬된 5장의 카드 리스트가 스트레이트(랭크가 연속됨)인지 확인    
    private static boolean isStraight(List<Card> sortedCards) {
        if (sortedCards == null || sortedCards.size() != 5) return false;
      
        boolean isWheel = sortedCards.get(0).getRank() == ACE_RANK &&
                          sortedCards.get(1).getRank() == FIVE_RANK && // A, 5, 4, 3, 2 순서로 정렬되어 있다면
                          sortedCards.get(2).getRank() == 4 &&
                          sortedCards.get(3).getRank() == 3 &&
                          sortedCards.get(4).getRank() == TWO_RANK;
        if (isWheel) return true;
       
        // 카드는 이미 랭크 내림차순 정렬
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (sortedCards.get(i).getRank() - sortedCards.get(i+1).getRank() != 1) {
                return false; // 연속되지 않으면 스트레이트 아님
            }
        }
        return true; // 모든 카드가 연속되면 스트레이트
    }


    //2~4장의 오픈 카드만으로 부분 족보를 평가 (베팅 순서 결정)
    public static HandEvaluationResult evaluatePartialHand(List<Card> openCards) {
        if (openCards == null || openCards.isEmpty()) {
            return new HandEvaluationResult(HandRank.HIGH_CARD, new ArrayList<>()); // 카드가 없으면 하이
        }
        // 이 메서드는 2~4장의 카드를 대상으로 함. 그 이상은 5장 풀 핸드 평가로 넘어가야 함.
        // 여기서는 입력된 카드 수 그대로 평가 시도.
        List<Card> cardsToEvaluate = new ArrayList<>(openCards);
        cardsToEvaluate.sort(Comparator.comparingInt(Card::getRank).reversed()); // 랭크 높은 순 정렬

        Map<Integer, Long> rankCounts = cardsToEvaluate.stream()
            .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        // 트리플 (3장 또는 4장일 때 가능)
        if (cardsToEvaluate.size() >= 3) {
            Optional<Map.Entry<Integer, Long>> triple = rankCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() == 3).findFirst();
            if (triple.isPresent()) {
                int tripleRank = triple.get().getKey();
                List<Card> determining = cardsToEvaluate.stream().filter(c -> c.getRank() == tripleRank).collect(Collectors.toList());
                // 부분 패에서는 키커를 단순하게 남은 카드들로 구성
                List<Card> kickers = cardsToEvaluate.stream().filter(c -> c.getRank() != tripleRank).limit(Math.max(0, cardsToEvaluate.size() - 3)).collect(Collectors.toList());
                determining.addAll(kickers);
                return new HandEvaluationResult(HandRank.TRIPS, determining);
            }
        }

        // 투 페어 (4장일 때만 가능)
        if (cardsToEvaluate.size() == 4) {
            List<Map.Entry<Integer, Long>> pairs = rankCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() == 2)
                    .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey())) // 높은 페어부터
                    .collect(Collectors.toList());
            if (pairs.size() == 2) { // 정확히 두 개의 페어
                int highPairRank = pairs.get(0).getKey();
                int lowPairRank = pairs.get(1).getKey();
                List<Card> determining = new ArrayList<>();
                determining.addAll(cardsToEvaluate.stream().filter(c->c.getRank() == highPairRank).collect(Collectors.toList()));
                determining.addAll(cardsToEvaluate.stream().filter(c->c.getRank() == lowPairRank).collect(Collectors.toList()));
                return new HandEvaluationResult(HandRank.TWO_PAIR, determining);
            }
        }

        // 원 페어 (2장 이상일 때 가능)
        if (cardsToEvaluate.size() >= 2) {
             Optional<Map.Entry<Integer, Long>> pair = rankCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() == 2).findFirst();
            if (pair.isPresent()) {
                int pairRank = pair.get().getKey();
                List<Card> determining = cardsToEvaluate.stream().filter(c -> c.getRank() == pairRank).collect(Collectors.toList());
                List<Card> kickers = cardsToEvaluate.stream().filter(c -> c.getRank() != pairRank).limit(Math.max(0, cardsToEvaluate.size() - 2)).collect(Collectors.toList());
                determining.addAll(kickers);
                return new HandEvaluationResult(HandRank.ONE_PAIR, determining);
            }
        }
        
        // 하이 카드 (위 족보에 해당하지 않으면)
        return new HandEvaluationResult(HandRank.HIGH_CARD, new ArrayList<>(cardsToEvaluate));
    }

    //오픈된 카드 중 가장 높은 랭크의 카드를 반환
    public static Card getHighestOpenCard(List<Card> openCards) {
        if (openCards == null || openCards.isEmpty()) return null;
        return openCards.stream().max(Comparator.comparingInt(Card::getRank)).orElse(null);
    }
}