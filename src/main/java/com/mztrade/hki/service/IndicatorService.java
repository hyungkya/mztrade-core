package com.mztrade.hki.service;

import static java.lang.Math.abs;

import com.mztrade.hki.entity.StockPrice;
import com.mztrade.hki.entity.backtest.Indicator;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

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
        return indicator.calculate(stockPrices);
    }

/*    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();
        Indicator indicator = new Indicator(type, params);
        for (int i = 1; i <= bars.size(); i++) {
            result.put(bars.get(i - 1).getDate(), indicator.calculate(bars.subList(0, i)));
        }
        return result;
    }*/

    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, Indicator indicator) {
        Map<LocalDateTime, Double> result = new HashMap<>();
        if (indicator.getType().startsWith("SMA1")) {
            result = calculateSMAs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("SMA2")) {
            result = calculateSMAs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("SMA3")) {
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
            result = calculateCCIs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("BBH")) {
            result = calculateBBHighs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("BBL")) {
            result = calculateBBLows(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("PPP")) {
            result = calculatePPPs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("PPS1")) {
            result = calculatePPS1s(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("PPS2")) {
            result = calculatePPS2s(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("PPR1")) {
            result = calculatePPR1s(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("PPR2")) {
            result = calculatePPR2s(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("OBV")) {
            result = calculateOBVs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("OBV_SIGNAL")) {
            result = calculateOBVSignals(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("DMI_PDI")) {
            result = calculatePDIs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("DMI_MDI")) {
            result = calculateMDIs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("ADX")) {
            result = calculateADXs(ticker, startDate, endDate, indicator.getParams());
        } else if (indicator.getType().equals("MFI")) {
            result = calculateMFIs(ticker, startDate, endDate, indicator.getParams());
        }
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

    public Map<LocalDateTime, Double> calculateCCIs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1)
            throw new IllegalArgumentException(
                    "Commodity Channel Index needs 1 period parameter but 0 given");
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
                meanPricesMovingAverage.add(meanPrices.subList(i - n, i).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }
        List<Double> diffMovingAverage = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                diffMovingAverage.add(Double.NaN);
            } else {
                diffMovingAverage.add(meanPrices.get(i) - meanPricesMovingAverage.get(i));
            }
        }
        List<Double> absoluteDiffMovingAverage = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < (n * 2) - 1) {
                absoluteDiffMovingAverage.add(Double.NaN);
            } else {
                absoluteDiffMovingAverage.add(diffMovingAverage.subList(i - n, i).stream().mapToDouble(d -> abs(d)).average().orElse(Double.NaN));
            }
        }
        Map<LocalDateTime, Double> result = new HashMap<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < (n * 2) - 1) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), diffMovingAverage.get(i) / (absoluteDiffMovingAverage.get(i) * 0.015));
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

    public Map<LocalDateTime, Double> calculatePPPs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double pp = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), pp);
            }
            pp = (high + low + close) / 3;
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculatePPS1s(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double s1 = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), s1);
            }
            double pp = (high + low + close) / 3;
            s1 = pp * 2 - high;
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculatePPS2s(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double s2 = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), s2);
            }
            double pp = (high + low + close) / 3;
            s2 = pp - (high - low);
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculatePPR1s(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double r1 = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), r1);
            }
            double pp = (high + low + close) / 3;
            r1 = pp * 2 - low;
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculatePPR2s(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double r2 = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), r2);
            }
            double pp = (high + low + close) / 3;
            r2 = pp + (high - low);
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateOBVs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i == 0) {
                OBV = stockPrices.get(i).getVolume();
                result.put(stockPrices.get(i).getDate(), OBV);
            } else {
                if (stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
                    OBV += stockPrices.get(i).getVolume();
                } else if (stockPrices.get(i).getClose() < stockPrices.get(i - 1).getClose()) {
                    OBV -= stockPrices.get(i).getVolume();
                }
                result.put(stockPrices.get(i).getDate(), OBV);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateOBVSignals(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("OBV Signal needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;
        List<Double> OBVs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i == 0) {
                OBV = stockPrices.get(i).getVolume();
                OBVs.add(OBV);
            } else {
                if (stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
                    OBV += stockPrices.get(i).getVolume();
                } else if (stockPrices.get(i).getClose() < stockPrices.get(i - 1).getClose()) {
                    OBV -= stockPrices.get(i).getVolume();
                }
                OBVs.add(OBV);
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), OBVs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculatePDIs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> PDMs = new ArrayList<>();
        List<Integer> MDMs = new ArrayList<>();
        List<Integer> TRs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int highDiff = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getHigh(), 0);
            int lowDiff = Math.max(stockPrices.get(i - 1).getLow() - stockPrices.get(i).getLow(), 0);
            if (highDiff > lowDiff) {
                PDMs.add(highDiff);
                MDMs.add(0);
            } else if (lowDiff > highDiff) {
                PDMs.add(0);
                MDMs.add(lowDiff);
            } else {
                PDMs.add(0);
                MDMs.add(0);
            }

            int a = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow());
            int b = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose());
            int c = Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose());
            TRs.add(Math.max(Math.max(a, b), c));
        }
        List<Double> TR_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                TR_EMA.add(Double.NaN);
            } else if (i == period) {
                TR_EMA.add(TRs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                TR_EMA.add(((period - 1) * TR_EMA.getLast() + TRs.get(i)) / period);
            }
        }
        List<Double> PDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                PDM_EMA.add(Double.NaN);
            } else if (i == period) {
                PDM_EMA.add(PDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                PDM_EMA.add(((period - 1) * PDM_EMA.getLast() + PDMs.get(i)) / period);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double pdi = PDM_EMA.get(i - 1) / TR_EMA.get(i - 1);
                result.put(stockPrices.get(i).getDate(), pdi);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateMDIs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> PDMs = new ArrayList<>();
        List<Integer> MDMs = new ArrayList<>();
        List<Integer> TRs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int highDiff = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getHigh(), 0);
            int lowDiff = Math.max(stockPrices.get(i - 1).getLow() - stockPrices.get(i).getLow(), 0);
            if (highDiff > lowDiff) {
                PDMs.add(highDiff);
                MDMs.add(0);
            } else if (lowDiff > highDiff) {
                PDMs.add(0);
                MDMs.add(lowDiff);
            } else {
                PDMs.add(0);
                MDMs.add(0);
            }

            int a = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow());
            int b = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose());
            int c = Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose());
            int aa = Math.max(a, b);
            TRs.add(Math.max(aa, c));
        }
        List<Double> TR_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                TR_EMA.add(Double.NaN);
            } else if (i == period) {
                TR_EMA.add(TRs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                TR_EMA.add(((period - 1) * TR_EMA.getLast() + TRs.get(i)) / period);
            }
        }
        List<Double> MDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                MDM_EMA.add(Double.NaN);
            } else if (i == period) {
                MDM_EMA.add(MDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                MDM_EMA.add(((period - 1) * MDM_EMA.getLast() + MDMs.get(i)) / period);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double mdi = MDM_EMA.get(i - 1) / TR_EMA.get(i - 1);
                result.put(stockPrices.get(i).getDate(), mdi);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateADXs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("ADX needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> PDMs = new ArrayList<>();
        List<Integer> MDMs = new ArrayList<>();
        List<Integer> TRs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int highDiff = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getHigh(), 0);
            int lowDiff = Math.max(stockPrices.get(i - 1).getLow() - stockPrices.get(i).getLow(), 0);
            if (highDiff > lowDiff) {
                PDMs.add(highDiff);
                MDMs.add(0);
            } else if (lowDiff > highDiff) {
                PDMs.add(0);
                MDMs.add(lowDiff);
            } else {
                PDMs.add(0);
                MDMs.add(0);
            }

            int a = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow());
            int b = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose());
            int c = Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose());
            int aa = Math.max(a, b);
            TRs.add(Math.max(aa, c));
        }
        List<Double> TR_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                TR_EMA.add(Double.NaN);
            } else if (i == period) {
                TR_EMA.add(TRs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                TR_EMA.add(((period - 1) * TR_EMA.getLast() + TRs.get(i)) / period);
            }
        }
        List<Double> PDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                PDM_EMA.add(Double.NaN);
            } else if (i == period) {
                PDM_EMA.add(PDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                PDM_EMA.add(((period - 1) * PDM_EMA.getLast() + PDMs.get(i)) / period);
            }
        }
        List<Double> MDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                MDM_EMA.add(Double.NaN);
            } else if (i == period) {
                MDM_EMA.add(MDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                MDM_EMA.add(((period - 1) * MDM_EMA.getLast() + MDMs.get(i)) / period);
            }
        }
        List<Double> DX = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                DX.add(Double.NaN);
            } else if (i == period) {
                DX.add((Math.abs(PDM_EMA.get(i) - MDM_EMA.get(i)) / (PDM_EMA.get(i) + MDM_EMA.get(i))) * 100);
            } else {
                DX.add(((period - 1) * DX.getLast() + (Math.abs(PDM_EMA.get(i) - MDM_EMA.get(i)) / (PDM_EMA.get(i) + MDM_EMA.get(i))) * 100) / period);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i == 0) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), DX.get(i - 1));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public Map<LocalDateTime, Double> calculateMFIs(String ticker, LocalDateTime startDate, LocalDateTime endDate, List<Float> params) {
        if (params.size() < 1) throw new IllegalArgumentException("Pivot Points needs 1 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();

        List<StockPrice> stockPrices = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> pRMF = new ArrayList<>();
        List<Double> nRMF = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int previousTp = (stockPrices.get(i - 1).getHigh() + stockPrices.get(i - 1).getLow() + stockPrices.get(i - 1).getClose()) / 3;
            int tp = (stockPrices.get(i).getHigh() + stockPrices.get(i).getLow() + stockPrices.get(i).getClose()) / 3;
            double rmf = tp * stockPrices.get(i).getVolume();
            if (previousTp < tp) {
                pRMF.add(rmf);
                nRMF.add(0.0);
            } else {
                pRMF.add(0.0);
                nRMF.add(rmf);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double MFR = pRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum() / nRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum();
                result.put(stockPrices.get(i).getDate(), 100 - (100 / (1 + MFR)));
            }
        }
        assert result.size() == stockPrices.size();
        return result;
    }
}
