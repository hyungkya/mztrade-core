package com.mztrade.hki.service;

import com.mztrade.hki.entity.StockPrice;
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
        List<StockPrice> stockPrices = new ArrayList<>();
        int maxFail = 10;
        while (stockPrices.size() < maxRange && maxFail > 0) {
            try {
                stockPrices.add(stockPriceService.getPrice(ticker, date));
                maxFail = 10;
            } catch (DataAccessException e) {
                maxFail--;
            }
            date = date.minus(1, ChronoUnit.DAYS);
        }
        Indicator indicator = new Indicator(type, params);
        double result = indicator.calculate(stockPrices);

        log.debug(String.format("[IndicatorService] getIndicator(ticker: %s, date: %s, type: %s, params: %s) -> indicator:%s",
                ticker, date, type, params, result)
        );
        return indicator.calculate(stockPrices);
    }

/*    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
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
    }*/

    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, Indicator indicator) {
        Map<LocalDateTime, Double> result = new HashMap<>();
        if (indicator.getType().startsWith("SMA")) {
            result = calculateSMAs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("EMA")) {
            result = calculateEMAs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("MACD")) {
            result = calculateMACDs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("MACD_SIGNAL")) {
            result = calculateMACDSignals(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("RSI")) {
            result = calculateRSIs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("SF")) {
            result = calculateStochasticFasts(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("SS")) {
            result = calculateStochasticSlows(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("CCI")) {
            result = calculateCommodityChannelIndexs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("BBH")) {
            result = calculateBBHighs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("BBL")) {
            result = calculateBBLows(ticker, startDate, endDate, indicator.getParams());
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

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), stockPrices.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble());
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateEMAs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("EMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (period + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else if (i == period) {
                previousEMA = stockPrices.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                result.put(stockPrices.get(i).getDate(), previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                result.put(stockPrices.get(i).getDate(), previousEMA);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateMACDs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() <= 1) throw new IllegalArgumentException("MACD needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        int shortPeriod = params.get(0).intValue();
        int longPeriod = params.get(1).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> shortEMA = new ArrayList<>();
        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (shortPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < shortPeriod) {
                shortEMA.add(Double.NaN);
            } else if (i == shortPeriod) {
                previousEMA = stockPrices.subList(i - shortPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                shortEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                shortEMA.add(previousEMA);
            }
        }

        List<Double> longEMA = new ArrayList<>();
        previousEMA = Double.NaN;
        smoothingConstant = 1 - (2.0 / (longPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                longEMA.add(Double.NaN);
            } else if (i == longPeriod) {
                previousEMA = stockPrices.subList(i - longPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                longEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                longEMA.add(previousEMA);
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), longEMA.get(i) - shortEMA.get(i));
            }
        }
        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateMACDSignals(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() <= 1) throw new IllegalArgumentException("MACD needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        int shortPeriod = params.get(0).intValue();
        int longPeriod = params.get(1).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> shortEMA = new ArrayList<>();
        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (shortPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < shortPeriod) {
                shortEMA.add(Double.NaN);
            } else if (i == shortPeriod) {
                previousEMA = stockPrices.subList(i - shortPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                shortEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                shortEMA.add(previousEMA);
            }
        }

        List<Double> longEMA = new ArrayList<>();
        previousEMA = Double.NaN;
        smoothingConstant = 1 - (2.0 / (longPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                longEMA.add(Double.NaN);
            } else if (i == longPeriod) {
                previousEMA = stockPrices.subList(i - longPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                longEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                longEMA.add(previousEMA);
            }
        }

        List<Double> MACDs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                MACDs.add(Double.NaN);
            } else {
                MACDs.add(longEMA.get(i) - shortEMA.get(i));
            }
        }

        double k = 2 / (params.get(2) + 1);
        for (int i = 1; i < stockPrices.size(); i++) {
            result.put(stockPrices.get(i).getDate(), ((1 - k) * MACDs.get(i - 1)) + (k * MACDs.get(i)));
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateRSIs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("RSI needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> diffs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            diffs.add(stockPrices.get(i).getClose() - stockPrices.get(i - 1).getClose());
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double au = diffs.subList(i - period, i).stream().filter(e -> e > 0).mapToInt(Math::abs).average().orElse(0);
                double ad = diffs.subList(i - period, i).stream().filter(e -> e < 0).mapToInt(Math::abs).average().orElse(100);
                result.put(stockPrices.get(i).getDate(), (au / (ad + au)) * 100);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateStochasticFasts(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Fast needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");

        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        assert n > m;

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();


        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                percentKs.add(Double.NaN);
            } else {
                int highestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (stockPrices.get(i).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n + m) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), percentKs.subList(i + 1 - m, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }
        assert stockPrices.size() == result.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateStochasticSlows(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        int t = params.get(2).intValue();

        assert n > m;
        assert m >= t;

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                percentKs.add(Double.NaN);
            } else {
                int highestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (stockPrices.get(i).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
        }
        List<Double> percentDs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n + m) {
                percentDs.add(Double.NaN);
            } else {
                percentDs.add(percentKs.subList(i + 1 - m, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n + m + t) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), percentDs.subList(i + 1 - t, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }
        assert stockPrices.size() == result.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateCommodityChannelIndexs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException(
                    "Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0))
            throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        List<Double> meanPrices = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            meanPrices.add((stockPrices.get(i).getHigh() + stockPrices.get(i).getLow() + stockPrices.get(i).getClose()) / 3.0);
        }
        List<Double> meanPricesMovingAverage = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                meanPricesMovingAverage.add(Double.NaN);
            } else {
                meanPricesMovingAverage.add(meanPrices.subList(i + 1 - n, i + 1).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }
        List<Double> diffMovingAverage = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                diffMovingAverage.add(Double.NaN);
            } else {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    sum += meanPrices.get(i - j) - meanPricesMovingAverage.get(i - j);
                }
                diffMovingAverage.add(sum / n);
            }
        }
        Map<LocalDateTime, Double> result = new HashMap<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), (meanPrices.get(i) - meanPricesMovingAverage.get(i)) / (abs(diffMovingAverage.get(i)) * 0.015));
            }
        }
        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateBBLows(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB Low needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double avg = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToInt(StockPrice::getClose)
                        .average()
                        .orElseThrow();
                double squareSum = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2))
                        .sum();
                double std = Math.sqrt(squareSum / (period - 1));
                result.put(stockPrices.get(i).getDate(), avg - (std * exp));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateBBHighs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB High needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double avg = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToInt(StockPrice::getClose)
                        .average()
                        .orElseThrow();
                double squareSum = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2))
                        .sum();
                double std = Math.sqrt(squareSum / (period - 1));
                result.put(stockPrices.get(i).getDate(), avg + (std * exp));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }
}
