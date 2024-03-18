package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.repository.OrderHistoryRepository;
import com.mztrade.hki.repository.PositionRepository;
import com.mztrade.hki.repository.PositionRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {
    private final OrderHistoryRepository orderHistoryRepository;
    private final PositionRepositoryImpl positionRepositoryImpl;
    private final AccountService accountService;
    private final StockPriceService stockPriceService;
    private final PositionRepository positionRepository;

    @Autowired
    public OrderService(OrderHistoryRepository orderHistoryRepository,
                        PositionRepositoryImpl positionRepositoryImpl, AccountService accountService,
                        StockPriceService stockPriceService, PositionRepository positionRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.positionRepositoryImpl = positionRepositoryImpl;
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
        this.positionRepository = positionRepository;
    }

    public Integer buy(Integer aid, String ticker, Integer qty) {
        Integer oid = null;
        Bar currentPrice = stockPriceService.getCurrentPrice(ticker);

        Order order = Order.builder()
                .ticker(ticker)
                .aid(aid)
                .price(currentPrice.getClose())
                .filledTime(currentPrice.getDate())
                .qty(qty)
                .otid(OrderType.BUY.id())
                .build();

        if (accountService.withdraw(aid, (long) order.getQty() * order.getPrice())) {
            Optional<Position> position = positionRepository.findByAidAndTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepositoryImpl.updatePosition(p);
            } else {
                Position p = new Position().toBuilder()
                        .aid(aid)
                        .qty(order.getQty())
                        .ticker(order.getTicker())
                        .avgEntryPrice(BigDecimal.valueOf(order.getPrice())).build();
                positionRepository.save(p);
            }

            oid = orderHistoryRepository.createOrderHistory(order);
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
        Order order = Order.builder()
                .ticker(ticker)
                .filledTime(date)
                .aid(aid)
                .price(stockPriceService.getPrice(ticker, date).getClose())
                .qty(qty)
                .otid(OrderType.BUY.id())
                .build();

        if (accountService.withdraw(aid, (long) order.getQty() * order.getPrice())) {
            Optional<Position> position = positionRepository.findByAidAndTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepositoryImpl.updatePosition(p);
            } else {
                Position p = new Position().toBuilder()
                        .aid(aid)
                        .qty(order.getQty())
                        .ticker(order.getTicker())
                        .avgEntryPrice(BigDecimal.valueOf(order.getPrice())).build();
                positionRepository.save(p);
            }

            oid = orderHistoryRepository.createOrderHistory(order);
        }
        log.debug(String.format("[OrderService] buy(aid: %d, ticker: %s, date: %s, qty: %d) -> oid: %s", aid, ticker, date, qty, oid));
        return oid;
    }

    public Integer sell(Integer aid, String ticker, Integer qty) {
        Integer oid = null;

        //check if position quantity is enough to sell
        Bar currentPrice = stockPriceService.getCurrentPrice(ticker);
        Optional<Position> optionalPosition = positionRepository.findByAidAndTicker(aid, ticker);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            if (position.getQty() >= qty) {
                Order order = Order.builder()
                        .ticker(ticker)
                        .filledTime(currentPrice.getDate())
                        .aid(aid)
                        .price(currentPrice.getClose())
                        .qty(qty)
                        .avgEntryPrice(position.getAvgEntryPrice())
                        .otid(OrderType.SELL.id())
                        .build();
                int remainingQty = position.getQty() - qty;
                if (remainingQty == 0) {
                    positionRepository.deleteByAidAndTicker(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepositoryImpl.updatePosition(position);
                }
                Long profit = BigInteger.valueOf(currentPrice.getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                oid = orderHistoryRepository.createOrderHistory(order);
            }
        }
        log.debug(String.format("[OrderService] sell(aid: %d, ticker: %s, qty: %d) -> oid: %s", aid, ticker, qty, oid));
        return oid;
    }

    public Integer sell(Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Integer oid = null;

        //check if position quantity is enough to sell
        Optional<Position> optionalPosition = positionRepository.findByAidAndTicker(aid, ticker);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            if (position.getQty() >= qty) {
                Order order = Order.builder()
                        .ticker(ticker)
                        .filledTime(date)
                        .aid(aid)
                        .price(stockPriceService.getPrice(ticker, date).getClose())
                        .qty(qty)
                        .avgEntryPrice(position.getAvgEntryPrice())
                        .otid(OrderType.SELL.id())
                        .build();
                int remainingQty = position.getQty() - qty;
                if (remainingQty == 0) {
                    positionRepository.deleteByAidAndTicker(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepositoryImpl.updatePosition(position);
                }
                Long profit = BigInteger.valueOf(stockPriceService.getPrice(ticker, date).getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                oid = orderHistoryRepository.createOrderHistory(order);
            }
        }
        log.debug(String.format("[OrderService] sell(aid: %d, ticker: %s, date: %s, qty: %d) -> oid: %s", aid, ticker, date, qty, oid));
        return oid;
    }

    public List<Order> getOrderHistory(Integer aid) {
        List<Order> orders = orderHistoryRepository.get(aid);
        log.debug(String.format("[OrderService] getOrderHistory(aid: %d) -> orders: %s", aid, orders));
        return orders;
    }

    public List<Order> getOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.get(aid, ticker);
        log.debug(String.format("[OrderService] getOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<Order> getBuyOrderHistory(Integer aid) {
        List<Order> orders = orderHistoryRepository.get(aid, OrderType.BUY.id());
        log.debug(String.format("[OrderService] getBuyOrderHistory(aid: %d) -> orders: %s", aid, orders));
        return orders;
    }

    public List<Order> getBuyOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.get(aid, ticker, OrderType.BUY.id());
        log.debug(String.format("[OrderService] getBuyOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<Order> getSellOrderHistory(Integer aid) {
        List<Order> orders = orderHistoryRepository.get(aid, OrderType.SELL.id());
        log.debug(String.format("[OrderService] getSellOrderHistory(aid: %d) -> orders: %s", aid, orders));
        return orders;
    }

    public List<Order> getSellOrderHistory(Integer aid, String ticker) {
        List<Order> orders = orderHistoryRepository.get(aid, ticker, OrderType.SELL.id());
        log.debug(String.format("[OrderService] getSellOrderHistory(aid: %d, ticker: %s) -> orders: %s", aid, ticker, orders));
        return orders;
    }
    public List<Position> getPositions(Integer aid) {
        List<Position> positions = positionRepository.findByAid(aid);
        log.debug(String.format("[OrderService] getPositions(aid: %d) -> positions: %s", aid, positions));
        return positions;
    }

    public Optional<Position> getPosition(Integer aid, String ticker) {
        Optional<Position> position = positionRepository.findByAidAndTicker(aid, ticker);
        log.debug(String.format("[OrderService] getPosition(aid: %d, ticker: %s) -> positions: %s", aid, ticker, position));
        return position;
    }
}
