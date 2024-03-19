package com.mztrade.hki.entity.backtest;

import com.mztrade.hki.entity.StockPrice;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@Getter
@Builder
public class Indicator {
    private String type;
    private List<Float> params;

    public Indicator(String type, List<Float> params) {
        this.type = type;
        this.params = params;
    }

    public double calculate(List<StockPrice> stockPrices) {
        if (type.toUpperCase().matches("NONE")) {
            return 0;
        }
        if (type.toUpperCase().matches("PRICE_CLOSE")) {
            return stockPrices.getLast().getClose();
        }
        if (type.toUpperCase().matches("RSI")) {
            return calculateRSI(stockPrices, params);
        }
        if (type.toUpperCase().matches("SMA")) {
            return calculateSMA(stockPrices, params);
        }
        if (type.toUpperCase().matches("EMA")) {
            return calculateEMA(stockPrices, params);
        }
        if (type.toUpperCase().matches("MACD")) {
            return calculateMACD(stockPrices, params);
        }
        if (type.toUpperCase().matches("MACD_SIGNAL")) {
            return calculateMACDSignal(stockPrices, params);
        }
        if (type.toUpperCase().matches("SF")) {
            return calculateStochasticFast(stockPrices, params);
        }
        if (type.toUpperCase().matches("SS")) {
            return calculateStochasticSlow(stockPrices, params);
        }
        if (type.toUpperCase().matches("CCI")) {
            return calculateCommodityChannelIndex(stockPrices, params);
        }
        if (type.toUpperCase().matches("BBL")) {
            return calculateBBLow(stockPrices, params);
        }
        if (type.toUpperCase().matches("BBH")) {
            return calculateBBHigh(stockPrices, params);
        }
        throw new IllegalArgumentException("No such indicator exists");
    }

    public double calculateSMA(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("SMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (stockPrices.size() < period) return Double.NaN;
        stockPrices = stockPrices.subList(stockPrices.size() - period, stockPrices.size());
        return stockPrices.stream().mapToInt(b -> b.getClose()).average().getAsDouble();
    }

    public double calculateEMA(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("EMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (stockPrices.size() < period + 1) return Double.NaN;
        double m = (2.0 / (period + 1));
        stockPrices = stockPrices.subList(stockPrices.size() - period - 1, stockPrices.size());
        List<Double> emas = new ArrayList<>();
        emas.add((double) stockPrices.getFirst().getClose());
        for (StockPrice b : stockPrices) {
            emas.add(((1 - m) * emas.getLast()) + (m * b.getClose()));
        }
        return emas.getLast();
    }

    public double calculateMACD(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() <= 1) throw new IllegalArgumentException("MACD needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        if (stockPrices.size() < params.get(1).intValue()) return Double.NaN;
        double shortEMA = calculateEMA(stockPrices, List.of(params.get(0)));
        double longEMA = calculateEMA(stockPrices, List.of(params.get(1)));
        return shortEMA - longEMA;
    }

    public double calculateMACDSignal(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() <= 2) throw new IllegalArgumentException("MACD Signal needs 3 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        if (stockPrices.size() < params.get(1).intValue()) return Double.NaN;
        List<Double> macdSignal = new ArrayList<>();
        for (int i = params.get(2).intValue(); i > 0; i--) {
            macdSignal.add(
                    calculateMACD(
                            stockPrices.subList(0, stockPrices.size() - i),
                            List.of(params.get(0), params.get(1))
                    ));
        }
        double k = 2 / (params.get(2) + 1);
        return macdSignal.stream()
                .reduce(0.0, (curr, next) -> (1 - k) * curr + k * next);
    }

    public double calculateRSI(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("RSI needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (stockPrices.size() < period) return Double.NaN;
        stockPrices = stockPrices.subList(stockPrices.size() - period, stockPrices.size());
        List<Integer> ups = new ArrayList<>();
        List<Integer> downs = new ArrayList<>();

        OptionalDouble au;
        OptionalDouble ad;

        for (int i = 0; i < stockPrices.size() - 2; i++) {
            int diff = stockPrices.get(i + 1).getClose() - stockPrices.get(i).getClose();
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

    public double calculateStochasticFast(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Fast needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        if (stockPrices.size() < n + m) return Double.NaN;
        stockPrices = stockPrices.subList(stockPrices.size() - (n + m), stockPrices.size());
        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            int highestPrice = stockPrices.subList(i, i + n).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
            int lowestPrice = stockPrices.subList(i, i + n).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
            double percentK = ((double) (stockPrices.get(i + n - 1).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
            percentKs.add(percentK);
        }
        return percentKs.stream().mapToDouble(i -> i).average().orElse(-1);
    }

    public double calculateStochasticSlow(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException("Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        int m = params.get(1).intValue();
        int t = params.get(2).intValue();
        if (stockPrices.size() < n + m + t) return Double.NaN;
        stockPrices = stockPrices.subList(stockPrices.size() - (n + m + t), stockPrices.size());
        List<Double> percentDs = new ArrayList<>();
        for (int j = 0; j < t; j++) {
            List<Double> percentKs = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int highestPrice = stockPrices.subList(i + j, (i + n) + j).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = stockPrices.subList(i + j, (i + n) + j).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (stockPrices.get((i + n - 1) + j).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
            percentDs.add(percentKs.stream().mapToDouble(i -> i).average().orElseThrow());
        }
        return percentDs.stream().mapToDouble(i -> i).average().orElse(-1);
    }

    public double calculateCommodityChannelIndex(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() == 0)
            throw new IllegalArgumentException(
                    "Stochastic Slow needs 3 period parameter but 0 given");
        if (!(params.get(0) > 0))
            throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int n = params.get(0).intValue();
        if (stockPrices.size() < n * 2 - 1)
            return Double.NaN;
        stockPrices = stockPrices.subList(stockPrices.size() - (n * 2 - 1), stockPrices.size());
        List<Double> rawD = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            double m = stockPrices.subList(j, j + n)
                    .stream()
                    .mapToDouble(b -> (b.getClose() + b.getHigh() + b.getLow()) / 3.0)
                    .average()
                    .orElseThrow();
            double M = (stockPrices.get(j + n - 1).getClose() + stockPrices.get(j + n - 1).getHigh() + stockPrices.get(j + n - 1).getLow()) / 3.0;
            rawD.add(M - m);
        }
        return rawD.getLast() / (rawD.stream().mapToDouble(Math::abs).average().orElseThrow() * 0.015);
    }

    public double calculateBBLow(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB Low needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (stockPrices.size() < params.get(0).intValue()) return Double.NaN;
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();
        stockPrices = stockPrices.subList(stockPrices.size() - period, stockPrices.size());
        double avg = stockPrices.stream().mapToInt(StockPrice::getClose).average().orElseThrow();
        double squareSum = stockPrices.stream().mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2)).sum();
        double std = Math.sqrt(squareSum / (period - 1));
        return avg - (std * exp);
    }

    public double calculateBBHigh(List<StockPrice> stockPrices, List<Float> params) {
        if (params.size() < 2) throw new IllegalArgumentException("BB Low needs 2 parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (stockPrices.size() < params.get(0).intValue()) return Double.NaN;
        int period = params.get(0).intValue();
        int exp = params.get(1).intValue();
        stockPrices = stockPrices.subList(stockPrices.size() - period, stockPrices.size());
        double avg = stockPrices.stream().mapToInt(StockPrice::getClose).average().orElseThrow();
        double squareSum = stockPrices.stream().mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2)).sum();
        double std = Math.sqrt(squareSum / (period - 1));
        return avg + (std * exp);
    }

    private void _validateParameters(List<Float> params, int requiredQty) {
        if (params.size() != requiredQty) {
            throw new IllegalArgumentException(
                    String.format(
                            "Required parameters are %d but %d were given.",
                            requiredQty,
                            params.size())
            );
        }
    }

    private void _validatePeriods(int s, int l) {
        if (s >= l) {
            throw new IllegalArgumentException(
                    "Short period parameter should be less than long period parameter."
            );
        }
    }
}
