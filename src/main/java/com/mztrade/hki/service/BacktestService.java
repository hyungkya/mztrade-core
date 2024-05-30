package com.mztrade.hki.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.dto.BacktestResultResponse;
import com.mztrade.hki.dto.BacktestParameter;
import com.mztrade.hki.entity.*;
import com.mztrade.hki.entity.backtest.BacktestResult;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.repository.*;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BacktestService {
    private final TagRepositoryImpl tagRepositoryImpl;
    private final BacktestResultRepository backtestResultRepository;
    private final PositionRepository positionRepository;
    private final ObjectMapper objectMapper;
    private final IndicatorService indicatorService;
    private final TagService tagService;
    private final AccountHistoryRepository accountHistoryRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AccountRepository accountRepository;
    private AccountService accountService;
    private StockPriceService stockPriceService;
    private OrderService orderService;


    @Autowired
    public BacktestService(AccountService accountService,
                           StockPriceService stockPriceService,
                           OrderService orderService,
                           TagRepositoryImpl tagRepositoryImpl,
                           BacktestResultRepository backtestResultRepository,
                           PositionRepository positionRepository,
                           ObjectMapper objectMapper,
                           IndicatorService indicatorService,
                           TagService tagService,
                           AccountRepository accountRepository,
                           AccountHistoryRepository accountHistoryRepository,
                           OrderHistoryRepository orderHistoryRepository) {
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.tagRepositoryImpl = tagRepositoryImpl;
        this.backtestResultRepository = backtestResultRepository;
        this.positionRepository = positionRepository;
        this.objectMapper = objectMapper;
        this.indicatorService = indicatorService;
        this.tagService = tagService;
        this.accountHistoryRepository = accountHistoryRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.accountRepository = accountRepository;
    }

    public int execute(BacktestParameter backtestParameter) {
        Map<String, List<? extends Bar>> tickerBars = new HashMap<>();
        Map<String, Integer> tickerAids = new HashMap<>();
        // 전체 ticker 가격 불러오기
        if (backtestParameter.getTimeScale() == null) {
            for (String ticker : backtestParameter.getTickers()) {
                tickerBars.put(ticker,
                        stockPriceService.getDailyPrices(
                                ticker,
                                backtestParameter.parseStartDate(),
                                backtestParameter.parseEndDate())
                );
            }
        } else {
            for (String ticker : backtestParameter.getTickers()) {
                tickerBars.put(ticker,
                        stockPriceService.getMinutelyPrices(
                                ticker,
                                backtestParameter.parseStartDate(),
                                backtestParameter.parseEndDate())
                );
            }
        }

        for (String ticker : backtestParameter.getTickers()) {
            tickerAids.put(ticker, unitExecute(
                    ticker,
                    tickerBars.get(ticker),
                    backtestParameter));
        }

        // 백테스팅을 진행할 계좌 생성
        int aid = accountService.createAccount(backtestParameter.getUid());
        Long lastBalance = 0L;
        for (String ticker : backtestParameter.getTickers()) {
            lastBalance += accountService.getBalance(tickerAids.get(ticker));
        }
        accountService.deposit(aid, lastBalance);

        Map<LocalDateTime, Long> accountHistoryBalance = new HashMap<>();
        for (Bar bar : tickerBars.get(getLongestBars(tickerBars))) {
            Long sumOfBalance = 0L;
            for (String ticker : backtestParameter.getTickers()) {
                Optional<AccountHistory> accountHistory =
                        accountHistoryRepository.findByAidAndDate(tickerAids.get(ticker), bar.getDate());
                if (accountHistory.isPresent()) {
                    sumOfBalance += accountHistory.get().getBalance();
                } else {
                    sumOfBalance += backtestParameter.getInitialBalance() / backtestParameter.getTickers().size();
                }
            }
            accountHistoryBalance.put(bar.getDate(), sumOfBalance);
        }
        accountService.createAccountHistories(aid, accountHistoryBalance);

        // AID 조작
        for (String ticker : backtestParameter.getTickers()) {
            List<Order> orders = orderHistoryRepository.findByAccountAid(tickerAids.get(ticker));
            for (Order order : orders) {
                order.setAccount(accountRepository.getReferenceById(aid));
            }
        }
        // Position 옮기기
        for (String ticker : backtestParameter.getTickers()) {
            List<Position> positions = positionRepository.findByAccountAid(tickerAids.get(ticker));
            for (Position position : positions) {
                Position newPosition = Position.builder()
                        .account(accountRepository.getReferenceById(aid))
                        .qty(position.getQty())
                        .avgEntryPrice(position.getAvgEntryPrice())
                        .stockInfo(position.getStockInfo())
                        .build();
                positionRepository.save(newPosition);
            }
        }

        for (Condition condition : backtestParameter.getBuyConditions()) {
            if (condition.getBaseType().equals("indicator")) {
                String tname = condition.getBaseParam().split(",")[0];
                int tid = tagService.createIfNotExists(backtestParameter.getUid(), tname, TagCategory.BACKTEST_HISTORY);
                tagService.createBacktestResultTagLink(tid, aid);
            }
            if (condition.getTargetType().equals("indicator")) {
                String tname = condition.getTargetParam().split(",")[0];
                int tid = tagService.createIfNotExists(backtestParameter.getUid(), tname, TagCategory.BACKTEST_HISTORY);
                tagService.createBacktestResultTagLink(tid, aid);
            }
        }

        for (Condition condition : backtestParameter.getSellConditions()) {
            if (condition.getBaseType().equals("indicator")) {
                String tname = condition.getBaseParam().split(",")[0];
                int tid = tagService.createIfNotExists(backtestParameter.getUid(), tname, TagCategory.BACKTEST_HISTORY);
                tagService.createBacktestResultTagLink(tid, aid);
            }
            if (condition.getTargetType().equals("indicator")) {
                String tname = condition.getTargetParam().split(",")[0];
                int tid = tagService.createIfNotExists(backtestParameter.getUid(), tname, TagCategory.BACKTEST_HISTORY);
                tagService.createBacktestResultTagLink(tid, aid);
            }
        }

        return aid;
    }

    public String getLongestBars(Map<String, List<? extends Bar>> tickerBars) {
        int max = 0;
        String maxId = "";
        for (String ticker : tickerBars.keySet()) {
            if (tickerBars.get(ticker).size() > max) {
                max = tickerBars.get(ticker).size();
                maxId = ticker;
            }
        }
        return maxId;
    }

    public int unitExecute(String ticker, List<? extends Bar> bars, BacktestParameter backtestParameter) {
        bars.sort(Bar.COMPARE_BY_DATE);
        Map<LocalDateTime, Long> accountHistoryBalance = new HashMap<>();

        // 백테스팅을 진행할 계좌 생성
        int aid = accountService.createAccount(backtestParameter.getUid());
        long maxSingleTickerTradingBalance = backtestParameter.getInitialBalance() / backtestParameter.getTickers().size();
        accountService.deposit(aid, maxSingleTickerTradingBalance);

        // 포트폴리오 설정 (단일 종목 최대 주문 가능 금액 등)
        Integer dcaStatus = 0;
        Map<String, Integer> trailingStopStatus = new HashMap<>();

        // 조건 비교에 필요한 정보 로드
        // {"ticker": {"SMA1": {"2020-01-01": "34500", "2020-01-02": "34600"}, "RSI": {...}}}

        for (Condition condition : backtestParameter.getBuyConditions()) {
            condition.setup(bars, indicatorService, ticker);
        }
        for (Condition condition : backtestParameter.getSellConditions()) {
            condition.setup(bars, indicatorService, ticker);
        }

        for (Bar bar : bars) {
            // 신규 매수만 조건을 체크해서 진행
            if (orderService.getPosition(aid, ticker).isEmpty()) {
                int count = 0;
                for (Condition condition : backtestParameter.getBuyConditions()) {
                    if (condition.check(ticker, bar.getDate())) {
                        count++;
                    }
                }
                if (count >= backtestParameter.getBuyConditionLimit() && dcaStatus < backtestParameter.getDca().size()) {
                    // 종목 구매 금액 계산 (최대 거래 금액 * 분할 매수 1차 구매 비율)
                    double targetBuyAmount = backtestParameter.getDca().get(dcaStatus) * maxSingleTickerTradingBalance;
                    // 현재가 불러오기
                    int currentPrice = bar.getClose();
                    // 구매 가능 수량 계산
                    int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                    // 구매 가능 수량이 0 이상이라면 매수 진행 후 분할 매수 차수 업데이트
                    if (targetQty > 0) {
                        orderService.buy(aid, ticker, bar.getDate(), currentPrice, targetQty);
                        dcaStatus += 1;
                    }
                }
            } // 보유중인 종목에 대해서는 손절가 초과 시 분할 매수 진행
            else if (orderService.getPosition(aid, ticker).isPresent()) {
                Position p = orderService.getPosition(aid, ticker).get();
                int currentPrice = bar.getClose();
                // 현재 가격이 손절가 이하라면 분할 매수 남은 횟수 체크
                if (backtestParameter.getStopLoss() != null
                        && p.getAvgEntryPrice().doubleValue() * backtestParameter.getStopLoss() > currentPrice) {
                    // 남은 횟수가 있다면 분할 매수, 없다면 손절
                    if (dcaStatus < backtestParameter.getDca().size()) {
                        double targetBuyAmount = backtestParameter.getDca().get(dcaStatus) * maxSingleTickerTradingBalance;
                        int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                        if (targetQty > 0) {
                            orderService.buy(aid, ticker, bar.getDate(), currentPrice, targetQty);
                            dcaStatus += 1;
                        }
                    } else {
                        //손절
                        orderService.sell(aid, ticker, bar.getDate(), currentPrice, p.getQty());
                        //분할매수 리셋
                        dcaStatus = 0;
                        trailingStopStatus.remove(ticker);
                    }
                }
            }
            Optional<Position> position = orderService.getPosition(aid, ticker);
            if (position.isPresent()) {
                int currentPrice = bar.getClose();
                int count = 0;
                for (Condition condition : backtestParameter.getSellConditions()) {
                    if (condition.check(ticker, bar.getDate())) {
                        count++;
                    }
                }
                if (count >= backtestParameter.getSellConditionLimit()) {
                    orderService.sell(aid, ticker, bar.getDate(), currentPrice, position.get().getQty());
                    dcaStatus = 0;
                    // 매도 후 트레일링 스탑 감시 해제 (있다면)
                    trailingStopStatus.remove(ticker);
                }
                // 매도 조건에서 팔리지 않았을 경우
                else {
                    // 익절가 달성 여부 체크
                    if (backtestParameter.getStopProfit() != null &&
                            position.get().getAvgEntryPrice().doubleValue() * backtestParameter.getStopProfit() < currentPrice) {
                        // 트레일링 스탑 사용 중이라면 감시 시작
                        if (backtestParameter.getTrailingStop() != null) {
                            if (!trailingStopStatus.containsKey(ticker)) {
                                trailingStopStatus.put(ticker, currentPrice);
                            }
                        }
                        // 아니라면 전량 매도
                        else {
                            orderService.sell(aid, ticker, bar.getDate(), currentPrice, position.get().getQty());
                            dcaStatus = 0;
                            // 매도 후 트레일링 스탑 감시 해제 (있다면)
                            trailingStopStatus.remove(ticker);
                        }
                    }
                    // 트레일링 스탑 감시 중이라면
                    if (backtestParameter.getTrailingStop() != null &&
                            trailingStopStatus.containsKey(ticker)) {
                        // 현재가가 최고가 보다 높다면 최고가 갱신
                        if (trailingStopStatus.get(ticker) < currentPrice) {
                            trailingStopStatus.replace(ticker, currentPrice);
                        }
                        // 현재가가 트레일링 스탑 제한보다 낮아졌다면 전량 매도
                        else if (trailingStopStatus.get(ticker) * (1 - backtestParameter.getTrailingStop()) > currentPrice){
                            orderService.sell(aid, ticker, bar.getDate(), currentPrice, position.get().getQty());
                            dcaStatus = 0;
                            // 매도 후 트레일링 스탑 감시 해제
                            trailingStopStatus.remove(ticker);
                        }
                    }
                }
            }
            //계좌잔액기록
            long balance = accountService.getBalance(aid);

            for(Position p : positionRepository.findByAccountAid(aid)) {
                balance += (long) bar.getClose() * p.getQty();
            };
            accountHistoryBalance.put(bar.getDate(), balance);
        }
        accountService.createAccountHistories(aid, accountHistoryBalance);
        return aid;
    }

    public boolean create(BacktestResult backtestResult) {
        backtestResultRepository.save(backtestResult);
        return true;
    }

    @Transactional
    public boolean update(Integer aid, String param) {
        BacktestResult backtestResult = backtestResultRepository.getReferenceById(aid);
        backtestResult.setParam(param);
        return true;
    }

    public BacktestResultResponse get(int aid) {
        BacktestResultResponse backtestResultResponse = BacktestResultResponse.from(
                backtestResultRepository.getReferenceById(aid));
        return backtestResultResponse;
    }

    public List<BacktestResultResponse> getAllByPlratioDesc() {
        List<BacktestResultResponse> backtestResultResponse = backtestResultRepository.getAllByOrderByPlratioDesc()
                .stream()
                .map((b) -> BacktestResultResponse.from(b))
                .toList();
        return backtestResultResponse;
    }

    public BacktestParameter getBacktestParameter(int aid) throws NoSuchElementException {
        BacktestParameter backtestParameter;
        BacktestResult backtestResult = backtestResultRepository.findById(aid).orElseThrow();
        try {
            backtestParameter = objectMapper.readValue(backtestResult.getParam(), BacktestParameter.class);
            return backtestParameter;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> getTradedTickers(int aid) {
        List<String> tickers = getBacktestParameter(aid).getTickers();
        return tickers;
    }

    public List<BacktestResultResponse> searchByTitle(int uid, String title) {
        List<BacktestResultResponse> backtestResultResponses = backtestResultRepository.search(uid, title)
                .stream()
                .map((b) -> BacktestResultResponse.from(b))
                .toList();
        return backtestResultResponses;
    }

    public List<BacktestResultResponse> searchBacktestResultByTags(int uid, String title, List<Integer> tids) {
        List<BacktestResultResponse> backtestHistories = tagRepositoryImpl.findBacktestResultByTitleAndTags(uid, title, tids)
                .stream()
                .map((b) -> BacktestResultResponse.from(b))
                .toList();
        return backtestHistories;
    }

    public Double calculateFinalProfitLossRatio(int aid) {
        List<AccountHistory> accountHistories = accountHistoryRepository.findByAid(aid);
        double plratio = ((double) accountHistories.getLast().getBalance() / accountHistories.getFirst().getBalance()) - 1;
        return plratio;
    }
}
