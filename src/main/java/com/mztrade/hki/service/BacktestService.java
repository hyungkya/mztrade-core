package com.mztrade.hki.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.dto.BacktestHistoryResponse;
import com.mztrade.hki.dto.BacktestRequest;
import com.mztrade.hki.entity.StockPrice;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.repository.BacktestHistoryRepository;
import com.mztrade.hki.repository.PositionRepository;
import com.mztrade.hki.repository.TagRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class BacktestService {
    private final TagRepositoryImpl tagRepositoryImpl;
    private final BacktestHistoryRepository backtestHistoryRepository;
    private final PositionRepository positionRepository;
    private final ObjectMapper objectMapper;
    private final IndicatorService indicatorService;
    private AccountService accountService;
    private StockPriceService stockPriceService;
    private OrderService orderService;

    @Autowired
    public BacktestService(AccountService accountService,
                           StockPriceService stockPriceService,
                           OrderService orderService,
                           TagRepositoryImpl tagRepositoryImpl, BacktestHistoryRepository backtestHistoryRepository, PositionRepository positionRepository, ObjectMapper objectMapper, IndicatorService indicatorService) {
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.tagRepositoryImpl = tagRepositoryImpl;
        this.backtestHistoryRepository = backtestHistoryRepository;
        this.positionRepository = positionRepository;
        this.objectMapper = objectMapper;
        this.indicatorService = indicatorService;
    }

    public int execute(BacktestRequest backtestRequest) {
        LocalDateTime startDate = backtestRequest.parseStartDate();
        LocalDateTime endDate = backtestRequest.parseEndDate();

        // 백테스팅을 진행할 계좌 생성
        int aid = accountService.createAccount(backtestRequest.getUid());
        accountService.deposit(aid, backtestRequest.getInitialBalance());

        // 포트폴리오 설정 (단일 종목 최대 주문 가능 금액 등)
        long maxSingleTickerTradingBalance = backtestRequest.getInitialBalance() / backtestRequest.getTickers().size();
        Map<String, Integer> dcaStatus = new HashMap<>();
        for (String ticker : backtestRequest.getTickers()) {
            dcaStatus.put(ticker, 0);
        }
        Map<String, Integer> trailingStopStatus = new HashMap<>();

        // 가격 정보 로드
        Map<String, Map<LocalDateTime, StockPrice>> bars = new HashMap<>();
        for (String ticker : backtestRequest.getTickers()) {
            Map<LocalDateTime, StockPrice> prices = new HashMap<>();
            List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
            for (StockPrice stockPrice : stockPrices) {
                prices.put(stockPrice.getDate(), stockPrice);
            }
            bars.put(ticker, prices);
        }

        // 조건 비교에 필요한 정보 로드
        // {"ticker": {"SMA1": {"2020-01-01": "34500", "2020-01-02": "34600"}, "RSI": {...}}}
        for (String ticker : backtestRequest.getTickers()) {
            for (Condition condition : backtestRequest.getBuyConditions()) {
                condition.load(stockPriceService, indicatorService, ticker, startDate, endDate);
            }
            for (Condition condition : backtestRequest.getSellConditions()) {
                condition.load(stockPriceService, indicatorService, ticker, startDate, endDate);
            }
        }

        for (LocalDateTime currentDate = startDate; currentDate.isBefore(endDate); currentDate = currentDate.plus(1, ChronoUnit.DAYS)) {
            for (String ticker : backtestRequest.getTickers()) {
                if (!bars.get(ticker).containsKey(currentDate)) {
                    continue;
                }
                // 신규 매수만 조건을 체크해서 진행
                if (orderService.getPosition(aid, ticker).isEmpty()) {
                    boolean buyFlag = false;
                    for (Condition condition : backtestRequest.getBuyConditions()) {
                        if (condition.check(ticker, currentDate)) {
                            buyFlag = true;
                        }
                    }
                    if (buyFlag && dcaStatus.get(ticker) < backtestRequest.getDca().size()) {
                        // 종목 구매 금액 계산 (종목별 최대 거래 금액 * 분할 매수 1차 구매 비율)
                        double targetBuyAmount = backtestRequest.getDca().get(dcaStatus.get(ticker)) * maxSingleTickerTradingBalance;
                        // 현재가 불러오기
                        int currentPrice = bars.get(ticker).get(currentDate).getClose();
                        // 구매 가능 수량 계산
                        int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                        // 구매 가능 수량이 0 이상이라면 매수 진행 후 분할 매수 차수 업데이트
                        if (targetQty > 0) {
                            orderService.buy(aid, ticker, currentDate, targetQty);
                            dcaStatus.replace(ticker, dcaStatus.get(ticker) + 1);
                        }
                    }
                } // 보유중인 종목에 대해서는 손절가 초과 시 분할 매수 진행
                else if (orderService.getPosition(aid, ticker).isPresent()) {
                    Position p = orderService.getPosition(aid, ticker).get();
                    int currentPrice = bars.get(ticker).get(currentDate).getClose();
                    // 현재 가격이 손절가 이하라면 분할 매수 남은 횟수 체크
                    if (backtestRequest.getStopLoss() != null
                            && p.getAvgEntryPrice().doubleValue() * backtestRequest.getStopLoss() > currentPrice) {
                        // 남은 횟수가 있다면 분할 매수, 없다면 손절
                        if (dcaStatus.get(ticker) < backtestRequest.getDca().size()) {
                            double targetBuyAmount = backtestRequest.getDca().get(dcaStatus.get(ticker)) * maxSingleTickerTradingBalance;
                            int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                            if (targetQty > 0) {
                                orderService.buy(aid, ticker, currentDate, targetQty);
                                dcaStatus.replace(ticker, dcaStatus.get(ticker) + 1);
                            }
                        } else {
                            //손절
                            orderService.sell(aid, ticker, currentDate, p.getQty());
                            //분할매수 리셋
                            dcaStatus.replace(ticker, 0);
                            trailingStopStatus.remove(ticker);
                        }
                    }
                }
                Optional<Position> position = orderService.getPosition(aid, ticker);
                if (position.isPresent()) {
                    int currentPrice = bars.get(position.get().getStockInfo().getTicker()).get(currentDate).getClose();
                    boolean sellFlag = false;
                    for (Condition condition : backtestRequest.getSellConditions()) {
                        if (condition.check(ticker, currentDate)) {
                            sellFlag = true;
                        }
                    }
                    if (sellFlag) {
                        orderService.sell(aid, position.get().getStockInfo().getTicker(), currentDate, position.get().getQty());
                        dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                        // 매도 후 트레일링 스탑 감시 해제 (있다면)
                        trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                        break;
                    }
                    // 매도 조건에서 팔리지 않았을 경우
                    else {
                        // 익절가 달성 여부 체크
                        if (backtestRequest.getStopProfit() != null &&
                                position.get().getAvgEntryPrice().doubleValue() * backtestRequest.getStopProfit() < currentPrice) {
                            // 트레일링 스탑 사용 중이라면 감시 시작
                            if (backtestRequest.getTrailingStop() != null) {
                                if (!trailingStopStatus.containsKey(position.get().getStockInfo().getTicker())) {
                                    trailingStopStatus.put(position.get().getStockInfo().getTicker(), currentPrice);
                                }
                            }
                            // 아니라면 전량 매도
                            else {
                                orderService.sell(aid, position.get().getStockInfo().getTicker(), currentDate, position.get().getQty());
                                dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                                // 매도 후 트레일링 스탑 감시 해제 (있다면)
                                trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                            }
                        }
                        // 트레일링 스탑 감시 중이라면
                        if (backtestRequest.getTrailingStop() != null &&
                                trailingStopStatus.containsKey(position.get().getStockInfo().getTicker())) {
                            // 현재가가 최고가 보다 높다면 최고가 갱신
                            if (trailingStopStatus.get(position.get().getStockInfo().getTicker()) < currentPrice) {
                                trailingStopStatus.replace(position.get().getStockInfo().getTicker(), currentPrice);
                            }
                            // 현재가가 트레일링 스탑 제한보다 낮아졌다면 전량 매도
                            else if (trailingStopStatus.get(position.get().getStockInfo().getTicker()) * (1 - backtestRequest.getTrailingStop()) > currentPrice){
                                orderService.sell(aid, position.get().getStockInfo().getTicker(), currentDate, position.get().getQty());
                                dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                                // 매도 후 트레일링 스탑 감시 해제
                                trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                            }
                        }
                    }
                }
            }

            //계좌잔액기록
            long balance = accountService.getBalance(aid);

            for(Position p : positionRepository.findByAccountAid(aid)){
                StockPrice stockPrice = stockPriceService.getAvailablePriceBefore(p.getStockInfo().getTicker(),currentDate).orElseThrow();
                balance += (long) stockPrice.getClose() * p.getQty();
            };

            accountService.createAccountHistory(aid,currentDate,balance);
        }
        return aid;
    }

    /*public int execute(
            int uid,
            long initialBalance,
            List<List<Condition>> buyConditions,
            List<List<Condition>> sellConditions,
            List<Float> dca,
            Double stopLoss,
            Double stopProfit,
            Double trailingStop,
            int maxTradingCount,
            List<String> targetTickers,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        // Account Settings
        int aid = accountService.createAccount(uid);
        accountService.deposit(aid, initialBalance);

        // Portfolio Settings
        long maxSingleTickerTradingBalance = initialBalance / maxTradingCount;
        Map<String, Integer> dcaStatus = new HashMap<>();
        for (String ticker : targetTickers) {
            dcaStatus.put(ticker, 0);
        }
        Map<String, Integer> trailingStopStatus = new HashMap<>();

        // Get Bars
        Map<String, List<StockPrice>> collectedBars = new HashMap<>();
        boolean buyFlag = false;
        boolean sellFlag = false;

        for (; startDate.isBefore(endDate); startDate = startDate.plus(1, ChronoUnit.DAYS)) {
            for (String ticker : targetTickers) {
                // Stacking retrievable bar
                try {
                    StockPrice stockPrice = stockPriceService.getPrice(ticker, startDate);
                    if (!collectedBars.containsKey(ticker)) {
                        collectedBars.put(ticker, new ArrayList<>());
                    }
                    collectedBars.get(ticker).add(stockPrice);
                } catch (EmptyResultDataAccessException e) {
                    log.trace("[BacktestService] " + e);
                    continue;
                }
                // 보유중이지 않은 종목이면서 동시 매매 종목 자리가 남아있을 때만 매수 조건 체크
                if (orderService.getPosition(aid, ticker).isEmpty() && orderService.getPositions(aid).size() < maxTradingCount) {
                    // 그룹별은 OR 적용이고 그룹 내 조건들은 AND로 만족이 되어야함
                    for (List<Condition> buyConditionGroup : buyConditions) {
                        buyFlag = true;
                        for (Condition buyCondition : buyConditionGroup) {
                            if (!buyCondition.check(collectedBars.get(ticker))) {
                                buyFlag = false;
                            }
                        }
                        if (buyFlag && dcaStatus.get(ticker) < dca.size()) {
                            // 종목 구매 금액 계산 (종목별 최대 거래 금액 * 분할 매수 1차 구매 비율)
                            double targetBuyAmount = dca.get(dcaStatus.get(ticker)) * maxSingleTickerTradingBalance;
                            // 현재가 불러오기
                            int currentPrice = collectedBars.get(ticker).getLast().getClose();
                            // 구매 가능 수량 계산
                            int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                            // 구매 가능 수량이 0 이상이라면 매수 진행 후 분할 매수 차수 업데이트
                            if (targetQty > 0) {
                                orderService.buy(aid, ticker, startDate, targetQty);
                                dcaStatus.replace(ticker, dcaStatus.get(ticker) + 1);
                            }
                        }
                    }
                } // 보유중인 종목에 대해서는 손절가 초과 시 분할 매수 진행
                else if (orderService.getPosition(aid, ticker).isPresent()) {
                    Position p = orderService.getPosition(aid, ticker).get();
                    int currentPrice = collectedBars.get(ticker).getLast().getClose();
                    // 현재 가격이 손절가 이하라면 분할 매수 남은 횟수 체크
                    if (stopLoss != null && p.getAvgEntryPrice().doubleValue() * stopLoss > currentPrice) {
                        // 남은 횟수가 있다면 분할 매수, 없다면 손절
                        if (dcaStatus.get(ticker) < dca.size()) {
                            double targetBuyAmount = dca.get(dcaStatus.get(ticker)) * maxSingleTickerTradingBalance;
                            int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                            if (targetQty > 0) {
                                orderService.buy(aid, ticker, startDate, targetQty);
                                dcaStatus.replace(ticker, dcaStatus.get(ticker) + 1);
                            }
                        } else {
                            //손절
                            orderService.sell(aid, ticker, startDate, p.getQty());
                            //분할매수 리셋
                            dcaStatus.replace(ticker, 0);
                            trailingStopStatus.remove(ticker);
                        }
                    }
                }
                Optional<Position> position = orderService.getPosition(aid, ticker);
                if (position.isPresent()) {
                    int currentPrice = collectedBars.get(position.get().getStockInfo().getTicker()).getLast().getClose();
                    for (List<Condition> sellConditionGroup : sellConditions) {
                        sellFlag = true;
                        for (Condition sellCondition : sellConditionGroup) {
                            if (!sellCondition.check(collectedBars.get(position.get().getStockInfo().getTicker()))) {
                                sellFlag = false;
                            }
                        }
                        if (sellFlag) {
                            orderService.sell(aid, position.get().getStockInfo().getTicker(), startDate, position.get().getQty());
                            dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                            // 매도 후 트레일링 스탑 감시 해제 (있다면)
                            trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                            break;
                        }
                    }
                    // 매도 조건에서 팔리지 않았을 경우
                    if (!sellFlag) {
                        // 익절가 달성 여부 체크
                        if (stopProfit != null && position.get().getAvgEntryPrice().doubleValue() * stopProfit < currentPrice) {
                            // 트레일링 스탑 사용 중이라면 감시 시작
                            if (trailingStop != null) {
                                if (!trailingStopStatus.containsKey(position.get().getStockInfo().getTicker())) {
                                    trailingStopStatus.put(position.get().getStockInfo().getTicker(), currentPrice);
                                }
                            }
                            // 아니라면 전량 매도
                            else {
                                orderService.sell(aid, position.get().getStockInfo().getTicker(), startDate, position.get().getQty());
                                dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                                // 매도 후 트레일링 스탑 감시 해제 (있다면)
                                trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                            }
                        }
                        // 트레일링 스탑 감시 중이라면
                        if (trailingStop != null && trailingStopStatus.containsKey(position.get().getStockInfo().getTicker())) {
                            // 현재가가 최고가 보다 높다면 최고가 갱신
                            if (trailingStopStatus.get(position.get().getStockInfo().getTicker()) < currentPrice) {
                                trailingStopStatus.replace(position.get().getStockInfo().getTicker(), currentPrice);
                            }
                            // 현재가가 트레일링 스탑 제한보다 낮아졌다면 전량 매도
                            else if (trailingStopStatus.get(position.get().getStockInfo().getTicker()) * (1 - trailingStop) > currentPrice){
                                orderService.sell(aid, position.get().getStockInfo().getTicker(), startDate, position.get().getQty());
                                dcaStatus.replace(position.get().getStockInfo().getTicker(), 0);
                                // 매도 후 트레일링 스탑 감시 해제
                                trailingStopStatus.remove(position.get().getStockInfo().getTicker());
                            }
                        }
                    }
                }
            }

            //계좌잔액기록
            long balance = accountService.getBalance(aid);

            for(Position p : positionRepository.findByAccountAid(aid)){
                StockPrice stockPrice = stockPriceService.getAvailablePriceBefore(p.getStockInfo().getTicker(),startDate).orElseThrow();
                balance += (long) stockPrice.getClose() * p.getQty();
            };

            accountService.createAccountHistory(aid,startDate,balance);
        }
        log.debug(String.format("[BacktestService] execute(uid: %d, initialBalance: %d, buyConditions: %s, " +
                "sellConditions: %s, dca: %s, stopLoss: %s, stopProfit: %s, maxTradingCount: %d, targetTickers: %s, " +
                "startDate: %s, endDate: %s) -> aid: %d", uid, initialBalance, buyConditions, sellConditions, dca, stopLoss, stopProfit,
                maxTradingCount, targetTickers, startDate, endDate, aid));
        return aid;
    }
*/
    public boolean create(BacktestHistory backtestHistory) {
        backtestHistoryRepository.save(backtestHistory);
        log.debug(String.format("[BacktestService] create(backtestHistory: %s) -> isSuccess: %b", backtestHistory, true));
        return true;
    }

    @Transactional
    public boolean update(Integer aid, String param) {
        BacktestHistory backtestHistory = backtestHistoryRepository.getReferenceById(aid);
        backtestHistory.setParam(param);
        log.debug(String.format("[BacktestService] update(backtestHistory: %s) -> isSuccess: %b", backtestHistory, true));
        return true;
    }

    public BacktestHistoryResponse get(int aid) {
        BacktestHistoryResponse backtestHistoryResponse = BacktestHistoryResponse.from(backtestHistoryRepository.getReferenceById(aid));
        log.debug(String.format("[BacktestService] get(int: %d) -> backtestHistory: %s", aid, backtestHistoryResponse));
        return backtestHistoryResponse;
    }

    public List<BacktestHistoryResponse> getBacktestTop5(int uid) {
        List<BacktestHistoryResponse> backtestHistoryResponse = backtestHistoryRepository.findTop5ByUserUidOrderByPlratioDesc(uid)
                .stream()
                .map((b) -> BacktestHistoryResponse.from(b))
                .toList();
        log.debug(String.format("[BacktestService] getBacktestTop5(uid: %s) -> backtestHistories: %s", uid, backtestHistoryResponse));
        return backtestHistoryResponse;
    }

    public List<BacktestHistoryResponse> getRanking() {
        List<BacktestHistoryResponse> backtestHistoryResponse = backtestHistoryRepository.findTop5ByOrderByPlratioDesc()
                .stream()
                .map((b) -> BacktestHistoryResponse.from(b))
                .toList();
        log.debug(String.format("[BacktestService] getRanking() -> backtestHistories: %s", backtestHistoryResponse));
        return backtestHistoryResponse;
    }

    public BacktestRequest getBacktestRequest(int aid) throws NoSuchElementException {
        BacktestRequest backtestRequest;
        BacktestHistory backtestHistory = backtestHistoryRepository.findById(aid).orElseThrow();
        try {
            backtestRequest = objectMapper.readValue(backtestHistory.getParam(), BacktestRequest.class);
            log.debug(String.format("[BacktestService] getBacktestRequest(aid: %d) -> backtestRequest: %s", aid, backtestRequest));
            return backtestRequest;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> getTradedTickers(int aid) {
        List<String> tickers = getBacktestRequest(aid).getTickers();
        log.debug(String.format("[BacktestService] getTradedTickers(aid: %d) -> tickers: %s", aid, tickers));
        return tickers;
    }

    public List<BacktestHistoryResponse> searchByTitle(int uid, String title) {
        List<BacktestHistoryResponse> backtestHistoryResponses = backtestHistoryRepository.searchByTitle(uid, title)
                .stream()
                .map((b) -> BacktestHistoryResponse.from(b))
                .toList();
        log.debug(String.format("[BacktestService] searchByTitle(uid: %d, title: %s) -> backtestHistories: %s", uid, title, backtestHistoryResponses));
        return backtestHistoryResponses;
    }

    public List<BacktestHistoryResponse> searchBacktestHistoryByTags(int uid, String title, List<Integer> tids) {
        List<BacktestHistoryResponse> backtestHistories = tagRepositoryImpl.findBacktestHistoryByTitleAndTags(uid, title, tids)
                .stream()
                .map((b) -> BacktestHistoryResponse.from(b))
                .toList();
        log.debug(String.format("[BacktestService] searchBacktestHistoryByTags(uid: %d, title: %s, tids: %s) -> backtestHistories: %s", uid, title, tids, backtestHistories));
        return backtestHistories;
    }

    public Integer getNumberOfHistoryByUid(int uid) {
        int num = backtestHistoryRepository.countByUserUid(uid);
        log.debug(String.format("[BacktestService] getNumberOfHistoryByUid(uid: %d) -> num: %d", uid, num));
        return num;
    }

    public Optional<Integer> getHighestProfitLossRatio(int uid) {
        double highestProfitLossRatio = -1;
        Optional<Integer> highestAid = Optional.empty();
        for (int aid : accountService.getAllBacktestAccountIds(uid)) {
            double currentProfitLossRatio = backtestHistoryRepository.getReferenceById(aid).getPlratio();
            if (currentProfitLossRatio > highestProfitLossRatio) {
                highestProfitLossRatio = currentProfitLossRatio;
                highestAid = Optional.of(aid);
            }
        }
        log.debug(String.format("[BacktestService] getHighestProfitLossRatio(uid: %d) -> highestAid: %s", uid, highestAid));
        return highestAid;
    }

    public Double calculateFinalProfitLossRatio(long initialBalance, int aid, LocalDateTime backtestEndDate) {
        long finalBalance = accountService.getBalance(aid);
        List<Position> positions = positionRepository.findByAccountAid(aid);
        for (Position position : positions) {
            Integer finalClosePrice = stockPriceService.getAvailablePriceBefore(position.getStockInfo().getTicker(), backtestEndDate, 10).orElseThrow().getClose();
            finalBalance += (long) position.getQty() * finalClosePrice;
        }
        double plratio = (finalBalance / (double) initialBalance) - 1;
        log.debug(String.format("[BacktestService] calculateFinalProfitLossRatio(initialBalance: %d, aid: %d, " +
                "backtestEndDate: %s) -> plratio: %f", initialBalance, aid, backtestEndDate, plratio));
        return plratio;
    }
}
