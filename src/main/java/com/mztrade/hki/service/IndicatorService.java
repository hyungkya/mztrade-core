package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.backtest.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.time.temporal.ChronoUnit;

import static java.lang.Math.abs;

@Service
@Slf4j
public class IndicatorService {
    private final StockPriceService stockPriceService;

    @Autowired
    public IndicatorService(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    public double getIndicator(String ticker, LocalDateTime date, String type, List<Float> params) {
        int maxRange = 1;
        if (!params.isEmpty()) {
            for (Float value : params) {
                maxRange += value.intValue();
            }
        }
        System.out.println(params);
        System.out.println(maxRange);
        List<Bar> bars = new ArrayList<>();
        int maxFail = 10;
        while (bars.size() < maxRange && maxFail > 0) {
            try {
                bars.add(stockPriceService.getPrice(ticker, date));
                maxFail = 10;
            } catch(DataAccessException e) {
                maxFail--;
            }
            date = date.minus(1, ChronoUnit.DAYS);
        }
        Indicator indicator = new Indicator(type, params);
        double result = indicator.calculate(bars);

        log.debug(String.format("[IndicatorService] getIndicator(ticker: %s, date: %s, type: %s, params: %s) -> indicator:%s",
                ticker, date, type, params, result)
        );
        return indicator.calculate(bars);
    }

    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();
        Indicator indicator = new Indicator(type, params);
        for (int i = 1; i <= bars.size(); i++) {
            result.put(bars.get(i - 1).getDate(), indicator.calculate(bars.subList(0, i)));
        }
        log.debug(String.format("[IndicatorService] getIndicators(ticker: %s, startDate: %s, endDate: %s, type: %s, params: %s) -> indicator:%s",
                ticker, startDate, endDate, type, params, result)
        );
        return result;
    }

    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, Indicator indicator) {
        Map<LocalDateTime, Double> result = new HashMap<>();
        if (indicator.getType().equals("SMA")) {
            result = calculateSMAs(ticker, startDate, endDate, indicator.getParams());
        }
        else if (indicator.getType().equals("EMA")) {
            result = calculateEMAs(ticker, startDate, endDate, indicator.getParams());
        }
        else if (indicator.getType().equals("MACD")) {
            result = calculateMACDs(ticker, startDate, endDate, indicator.getParams());
        }
        log.debug(String.format("[IndicatorService] getIndicators(ticker: %s, startDate: %s, endDate: %s, indicator: %s) -> indicator:%s",
                ticker, startDate, endDate, indicator, result)
        );
        return result;
    }

    public Map<LocalDateTime, Double> calculateSMAs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("SMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                result.put(bars.get(i).getDate(), bars.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble());
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateEMAs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("EMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (period + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else if (i == period) {
                previousEMA = bars.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                result.put(bars.get(i).getDate(), previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                result.put(bars.get(i).getDate(), previousEMA);
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateMACDs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() <= 1) throw new IllegalArgumentException("MACD needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        int shortPeriod = params.get(0).intValue();
        int longPeriod = params.get(1).intValue();

        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> shortEMA = new ArrayList<>();
        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (shortPeriod + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < shortPeriod) {
                shortEMA.add(Double.NaN);
            } else if (i == shortPeriod) {
                previousEMA = bars.subList(i - shortPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                shortEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                shortEMA.add(previousEMA);
            }
        }

        List<Double> longEMA = new ArrayList<>();
        previousEMA = Double.NaN;
        smoothingConstant = 1 - (2.0 / (longPeriod + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < longPeriod) {
                longEMA.add(Double.NaN);
            } else if (i == longPeriod) {
                previousEMA = bars.subList(i - longPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                longEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                longEMA.add(previousEMA);
            }
        }

        for (int i = 0; i < bars.size(); i++) {
            if (i < longPeriod) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                result.put(bars.get(i).getDate(), longEMA.get(i) - shortEMA.get(i));
            }
        }
        assert result.size() == bars.size();
        return result;
    }
/*
    public double calculateMACDSignal(List<Bar> bars, List<Float> params) {
        if (params.size() <= 2) throw new IllegalArgumentException("MACD Signal needs 3 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        if (bars.size() < params.get(1).intValue()) return Double.NaN;
        List<Double> macdSignal = new ArrayList<>();
        for (int i = params.get(2).intValue(); i > 0; i--) {
            macdSignal.add(
                    calculateMACD(
                            bars.subList(0, bars.size() - i),
                            List.of(params.get(0), params.get(1))
                    ));
        }
        double k = 2 / (params.get(2) + 1);
        return macdSignal.stream()
                .reduce(0.0, (curr, next) -> (1 - k) * curr + k * next);
    }*/
    /*
    public double calculateRSI(List<Bar> bars, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("RSI needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (bars.size() < period) return Double.NaN;
        bars = bars.subList(bars.size() - period, bars.size());
        List<Integer> ups = new ArrayList<>();
        List<Integer> downs = new ArrayList<>();

        OptionalDouble au;
        OptionalDouble ad;

        for (int i = 0; i < bars.size() - 2; i++) {
            int diff = bars.get(i + 1).getClose() - bars.get(i).getClose();
            if (diff > 0) {
                ups.add(diff);
            }
            if (diff < 0) {
                downs.add(diff);
            }
        }

        au = ups.stream().mapToInt(i -> abs(i)).average();
        ad = downs.stream().mapToInt(i -> abs(i)).average();

        if (au.isEmpty()) return 0;
        if (ad.isEmpty()) return 100;

        return (au.getAsDouble() / (ad.getAsDouble() + au.getAsDouble())) * 100;
    }

    public double calculateStochasticFast(List<Bar> bars, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Fast needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        if (bars.size() < n + m) return Double.NaN;
        bars = bars.subList(bars.size() - (n + m), bars.size());
        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            int highestPrice = bars.subList(i, i + n).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
            int lowestPrice = bars.subList(i, i + n).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
            double percentK = ((double) (bars.get(i + n - 1).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
            percentKs.add(percentK);
        }
        return percentKs.stream().mapToDouble(i -> i).average().orElse(-1);
    }

    public double calculateStochasticSlow(List<Bar> bars, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        int t = params.get(2).intValue();
        if (bars.size() < n + m + t) return Double.NaN;
        bars = bars.subList(bars.size() - (n + m + t), bars.size());
        List<Double> percentDs = new ArrayList<>();
        for (int j = 0; j < t; j++) {
            List<Double> percentKs = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int highestPrice = bars.subList(i + j, (i + n) + j).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = bars.subList(i + j, (i + n) + j).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (bars.get((i + n - 1) + j).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
            percentDs.add(percentKs.stream().mapToDouble(i -> i).average().orElseThrow());
        }
        return percentDs.stream().mapToDouble(i -> i).average().orElse(-1);
    }

    public double calculateCommodityChannelIndex(List<Bar> bars, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException(
                    "Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0))
            throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        if (bars.size() < n * 2 - 1)
            return Double.NaN;
        bars = bars.subList(bars.size() - (n * 2 - 1), bars.size());
        List<Double> rawD = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            double m = bars.subList(j, j + n)
                    .stream()
                    .mapToDouble(b -> (b.getClose() + b.getHigh() + b.getLow()) / 3.0)
                    .average()
                    .orElseThrow();
            double M = (bars.get(j + n - 1).getClose() + bars.get(j + n - 1).getHigh() + bars.get(j + n - 1).getLow()) / 3.0;
            rawD.add(M - m);
        }
        return rawD.getLast() / (rawD.stream().mapToDouble(Math::abs).average().orElseThrow() * 0.015);
    }

    public double calculateBBLow(List<Bar> bars, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB Low needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (bars.size() < params.get(0).intValue()) return Double.NaN;
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();
        bars = bars.subList(bars.size() - period, bars.size());
        double avg = bars.stream().mapToInt(Bar::getClose).average().orElseThrow();
        double squareSum = bars.stream().mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2)).sum();
        double std = Math.sqrt(squareSum / (period - 1));
        return avg - (std * exp);
    }

    public double calculateBBHigh(List<Bar> bars, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB Low needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (bars.size() < params.get(0).intValue()) return Double.NaN;
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();
        bars = bars.subList(bars.size() - period, bars.size());
        double avg = bars.stream().mapToInt(Bar::getClose).average().orElseThrow();
        double squareSum = bars.stream().mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2)).sum();
        double std = Math.sqrt(squareSum / (period - 1));
        return avg + (std * exp);
    }*/
}
