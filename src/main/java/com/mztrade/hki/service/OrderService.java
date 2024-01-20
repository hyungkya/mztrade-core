package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.repository.OrderHistoryRepository;
import com.mztrade.hki.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderHistoryRepository orderHistoryRepository;
    private final PositionRepository positionRepository;
    private final AccountService accountService;
    private final StockPriceService stockPriceService;

    @Autowired
    public OrderService(OrderHistoryRepository orderHistoryRepository,
                        PositionRepository positionRepository, AccountService accountService,
                        StockPriceService stockPriceService) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.positionRepository = positionRepository;
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
    }

    public Boolean buy(Integer aid, String ticker, Integer qty) {
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
            Optional<Position> position = positionRepository.getPositionByTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepository.updatePosition(p);
            } else {
                Position p = new Position()
                        .setAid(aid)
                        .setQty(order.getQty())
                        .setTicker(order.getTicker())
                        .setAvgEntryPrice(BigDecimal.valueOf(order.getPrice()));
                positionRepository.createPosition(p);
            }

            orderHistoryRepository.createOrderHistory(order);
            return true;
        } else {
            //Not Enough Balance.
            return false;
        }
    }

    public Boolean buy(Integer aid, String ticker, Instant date, Integer qty) {
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
            Optional<Position> position = positionRepository.getPositionByTicker(aid, ticker);
            if (position.isPresent()) {
                Position p = position.get();
                BigDecimal newAvgEntryPrice = p.getAvgEntryPrice().multiply(BigDecimal.valueOf(p.getQty()))
                        .add(BigDecimal.valueOf((long) order.getQty() * order.getPrice()))
                        .divide(BigDecimal.valueOf(order.getQty() + p.getQty()), 2, RoundingMode.HALF_UP);
                p.setQty(p.getQty() + order.getQty());
                p.setAvgEntryPrice(newAvgEntryPrice);
                positionRepository.updatePosition(p);
            } else {
                Position p = new Position()
                        .setAid(aid)
                        .setQty(order.getQty())
                        .setTicker(order.getTicker())
                        .setAvgEntryPrice(BigDecimal.valueOf(order.getPrice()));
                positionRepository.createPosition(p);
            }

            orderHistoryRepository.createOrderHistory(order);
            return true;
        } else {
            //Not Enough Balance.
            return false;
        }
    }

    public Boolean sell(Integer aid, String ticker, Integer qty) {
        //check if position quantity is enough to sell
        Bar currentPrice = stockPriceService.getCurrentPrice(ticker);
        Optional<Position> optionalPosition = positionRepository.getPositionByTicker(aid, ticker);
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
                    positionRepository.deletePosition(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepository.updatePosition(position);
                }
                Long profit = BigInteger.valueOf(currentPrice.getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                orderHistoryRepository.createOrderHistory(order);
                return true;
            }
        }
        return false;
    }

    public Boolean sell(Integer aid, String ticker, Instant date, Integer qty) {
        //check if position quantity is enough to sell
        Optional<Position> optionalPosition = positionRepository.getPositionByTicker(aid, ticker);
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
                    positionRepository.deletePosition(aid, ticker);
                } else {
                    position.setQty(remainingQty);
                    positionRepository.updatePosition(position);
                }
                Long profit = BigInteger.valueOf(stockPriceService.getPrice(ticker, date).getClose())
                        .multiply(BigInteger.valueOf(qty))
                        .longValue();
                accountService.deposit(aid, profit);
                orderHistoryRepository.createOrderHistory(order);
                return true;
            }
        }
        return false;
    }

    public List<Order> getOrderHistory(Integer aid) {
        return orderHistoryRepository.get(aid);
    }

    public List<Order> getOrderHistory(Integer aid, String ticker) {
        return orderHistoryRepository.get(aid, ticker);
    }
    public List<Order> getBuyOrderHistory(Integer aid) {
        return orderHistoryRepository.get(aid, OrderType.BUY.id());
    }

    public List<Order> getBuyOrderHistory(Integer aid, String ticker) {
        return orderHistoryRepository.get(aid, ticker, OrderType.BUY.id());
    }
    public List<Order> getSellOrderHistory(Integer aid) {
        return orderHistoryRepository.get(aid, OrderType.SELL.id());
    }

    public List<Order> getSellOrderHistory(Integer aid, String ticker) {
        return orderHistoryRepository.get(aid, ticker, OrderType.SELL.id());
    }
    public List<Position> getPositions(Integer aid) {
        return positionRepository.getAllPositions(aid);
    }

    public Optional<Position> getPosition(Integer aid, String ticker) {
        return positionRepository.getPositionByTicker(aid, ticker);
    }


}
