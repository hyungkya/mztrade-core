package com.mztrade.hki.service;

import com.mztrade.hki.dto.OrderResponse;
import com.mztrade.hki.dto.PositionResponse;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.StockPrice;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.OrderHistoryRepository;
import com.mztrade.hki.repository.PositionRepository;
import com.mztrade.hki.repository.StockInfoRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {
    private final AccountService accountService;
    private final StockPriceService stockPriceService;
    private final PositionRepository positionRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AccountRepository accountRepository;
    private final StockInfoRepository stockInfoRepository;

    @Autowired
    public OrderService(AccountService accountService, StockPriceService stockPriceService, PositionRepository positionRepository, OrderHistoryRepository orderHistoryRepository, AccountRepository accountRepository, StockInfoRepository stockInfoRepository) {
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
        this.positionRepository = positionRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.accountRepository = accountRepository;
        this.stockInfoRepository = stockInfoRepository;
    }

    public Integer buy(Integer aid, String ticker, Integer qty) {
        Integer oid = null;
        StockPrice currentPrice = stockPriceService.getCurrentPrice(ticker);
        StockInfo stockInfo = stockInfoRepository.getByTicker(ticker);
        Account account = accountRepository.getReferenceById(aid);
        Order order = Order.builder()
                .stockInfo(stockInfo)
                .account(account)
                .price(currentPrice.getClose())
                .filledTime(currentPrice.getDate())
                .qty(qty)
                .otid(OrderType.BUY.id())
                .build();

        if (accountService.withdraw(aid, (long) order.getQty() * order.getPrice())) {
            Optional<Position> position = positionRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepository.save(p);
            } else {
                Position p = new Position().toBuilder()
                        .account(accountRepository.getById(aid))
                        .qty(order.getQty())
                        .stockInfo(order.getStockInfo())
                        .avgEntryPrice(BigDecimal.valueOf(order.getPrice())).build();
                positionRepository.save(p);
            }

            oid = orderHistoryRepository.save(order).getOid();
        }

        log.debug(String.format("[OrderService] buy(aid: %d, ticker: %s, qty: %d) -> oid: %s", aid, ticker, qty, oid));

        return oid;
    }

    public Integer buy(Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Integer oid = null;
        if (qty <= 0) {
            throw new IllegalArgumentException("Buying quantity should be greater than 0.");
        }
        //TODO:: qty 가 0과 같거나 작을 때 에러 쓰로잉 필요
        StockInfo stockInfo = stockInfoRepository.getByTicker(ticker);
        Account account = accountRepository.getReferenceById(aid);
        Order order = Order.builder()
                .stockInfo(stockInfo)
                .filledTime(date)
                .account(account)
                .price(stockPriceService.getPrice(ticker, date).getClose())
                .qty(qty)
                .otid(OrderType.BUY.id())
                .build();

        if (accountService.withdraw(aid, (long) order.getQty() * order.getPrice())) {
            Optional<Position> position = positionRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepository.save(p);
            } else {
                Position p = new Position().toBuilder()
                        .account(accountRepository.getById(aid))
                        .qty(order.getQty())
                        .stockInfo(order.getStockInfo())
                        .avgEntryPrice(BigDecimal.valueOf(order.getPrice())).build();
                positionRepository.save(p);
            }

            oid = orderHistoryRepository.save(order).getOid();
        }
        log.debug(String.format("[OrderService] buy(aid: %d, ticker: %s, date: %s, qty: %d) -> oid: %s", aid, ticker, date, qty, oid));
        return oid;
    }

    public Integer sell(Integer aid, String ticker, Integer qty) {
        Integer oid = null;

        //check if position quantity is enough to sell
        StockPrice currentPrice = stockPriceService.getCurrentPrice(ticker);
        StockInfo stockInfo = stockInfoRepository.getByTicker(ticker);
        Account account = accountRepository.getReferenceById(aid);
        Optional<Position> optionalPosition = positionRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            if (position.getQty() >= qty) {
                Order order = Order.builder()
                        .stockInfo(stockInfo)
                        .filledTime(currentPrice.getDate())
                        .account(account)
                        .price(currentPrice.getClose())
                        .qty(qty)
                        .avgEntryPrice(position.getAvgEntryPrice())
                        .otid(OrderType.SELL.id())
                        .build();
                int remainingQty = position.getQty() - qty;
                if (remainingQty == 0) {
                    positionRepository.deleteByAccountAidAndStockInfoTicker(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepository.save(position);
                }
                Long profit = BigInteger.valueOf(currentPrice.getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                oid = orderHistoryRepository.save(order).getOid();
            }
        }
        log.debug(String.format("[OrderService] sell(aid: %d, ticker: %s, qty: %d) -> oid: %s", aid, ticker, qty, oid));
        return oid;
    }

    public Integer sell(Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Integer oid = null;

        //check if position quantity is enough to sell
        Optional<Position> optionalPosition = positionRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
        StockInfo stockInfo = stockInfoRepository.getByTicker(ticker);
        Account account = accountRepository.getReferenceById(aid);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            if (position.getQty() >= qty) {
                Order order = Order.builder()
                        .stockInfo(stockInfo)
                        .filledTime(date)
                        .account(account)
                        .price(stockPriceService.getPrice(ticker, date).getClose())
                        .qty(qty)
                        .avgEntryPrice(position.getAvgEntryPrice())
                        .otid(OrderType.SELL.id())
                        .build();
                int remainingQty = position.getQty() - qty;
                if (remainingQty == 0) {
                    positionRepository.deleteByAccountAidAndStockInfoTicker(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepository.save(position);
                }
                Long profit = BigInteger.valueOf(stockPriceService.getPrice(ticker, date).getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                oid = orderHistoryRepository.save(order).getOid();
            }
        }
        log.debug(String.format("[OrderService] sell(aid: %d, ticker: %s, date: %s, qty: %d) -> oid: %s", aid, ticker, date, qty, oid));
        return oid;
    }

    public List<OrderResponse> getOrderHistory(Integer aid) {
        List<OrderResponse> orderResponses = orderHistoryRepository.findByAccountAid(aid)
                .stream()
                .map((o) -> OrderResponse.from(o))
                .toList();
        log.debug(String.format("[OrderService] getOrderHistory(aid: %d) -> orders: %s", aid, orderResponses));
        return orderResponses;
    }

    public List<Order> getOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
        log.debug(String.format("[OrderService] getOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<Order> getBuyOrderHistory(Integer aid) {
        List<Order> orders = orderHistoryRepository.findByAccountAidAndOtid(aid, OrderType.BUY.id());
        log.debug(String.format("[OrderService] getBuyOrderHistory(aid: %d) -> orders: %s", aid, orders));
        return orders;
    }

    public List<Order> getBuyOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.findByAccountAidAndStockInfoTickerAndOtid(aid, ticker, OrderType.BUY.id());
        log.debug(String.format("[OrderService] getBuyOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<Order> getSellOrderHistory(Integer aid) {
        List<Order> orders = orderHistoryRepository.findByAccountAidAndOtid(aid, OrderType.SELL.id());
        log.debug(String.format("[OrderService] getSellOrderHistory(aid: %d) -> orders: %s", aid, orders));
        return orders;
    }

    public List<Order> getSellOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.findByAccountAidAndStockInfoTickerAndOtid(aid, ticker, OrderType.SELL.id());
        log.debug(String.format("[OrderService] getSellOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<PositionResponse> getPositions(Integer aid) {
        List<PositionResponse> positionResponses = positionRepository.findByAccountAid(aid)
                .stream()
                .map((p) -> PositionResponse.from(p))
                .toList();
        log.debug(String.format("[OrderService] getPositions(aid: %d) -> positions: %s", aid, positionResponses));
        return positionResponses;
    }

    public Optional<Position> getPosition(Integer aid, String ticker) {
        Optional<Position> position = positionRepository.findByAccountAidAndStockInfoTicker(aid, ticker);
        log.debug(String.format("[OrderService] getPosition(aid: %d, ticker: %s) -> positions: %s", aid, ticker, position));
        return position;
    }
}
