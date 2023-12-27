package com.mztrade.hki.entity.backtest;

import com.mztrade.hki.entity.Bar;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static java.lang.Math.abs;

public class Indicator {
    private String type;
    private List<Float> params;

    public Indicator(String type, List<Float> params) {
        this.type = type;
        this.params = params;
    }

    public double calculate(List<Bar> bars) {
        if (type.toUpperCase().matches("NONE")) {
            return 0;
        }
        if (type.toUpperCase().matches("PRICE_CLOSE")) {
            return bars.getLast().getClose();
        }
        if (type.toUpperCase().matches("RSI")) {
            return calculateRSI(bars, params);
        }
        if (type.toUpperCase().matches("SMA")) {
            return calculateSMA(bars, params);
        }
        if (type.toUpperCase().matches("EMA")) {
            return calculateEMA(bars, params);
        }
        if (type.toUpperCase().matches("MACD")) {
            return calculateMACD(bars, params);
        }
        if (type.toUpperCase().matches("MACD_SIGNAL")) {
            return calculateMACDSignal(bars, params);
        }
        throw new IllegalArgumentException("No such indicator exists");
    }

    public double calculateSMA(List<Bar> bars, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("SMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (bars.size() < period) return Double.NaN;
        bars = bars.subList(bars.size() - period, bars.size());
        System.out.println("SMA" + params.get(0) + ": " + bars.stream().mapToInt(b -> b.getClose()).average().getAsDouble());
        return bars.stream().mapToInt(b -> b.getClose()).average().getAsDouble();
    }

    public double calculateEMA(List<Bar> bars, List<Float> params) {
        if (params.size() == 0) throw new IllegalArgumentException("EMA needs 1 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        int period = params.get(0).intValue();
        if (bars.size() < period + 1) return Double.NaN;
        double m = (2.0 / (period + 1));
        bars = bars.subList(bars.size() - period - 1, bars.size());
        List<Double> emas = new ArrayList<>();
        emas.add((double) bars.getFirst().getClose());
        for (Bar b : bars) {
            emas.add(((1-m) * emas.getLast()) + (m * b.getClose()));
        }
        return emas.getLast();
    }

    public double calculateMACD(List<Bar> bars, List<Float> params) {
        if (params.size() <= 1) throw new IllegalArgumentException("MACD needs 2 period parameter but 0 given");
        if (!(params.get(0) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (!(params.get(1) > 0)) throw new IllegalArgumentException("Period parameter should be bigger than 0");
        if (params.get(0) >= params.get(1)) throw new IllegalArgumentException("MACD Exception 1");
        if (bars.size() < params.get(1).intValue()) return Double.NaN;
        double shortEMA = calculateEMA(bars, List.of(params.get(0)));
        double longEMA = calculateEMA(bars, List.of(params.get(1)));
        return shortEMA - longEMA;
    }

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
    }

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
            if (diff > 0) { ups.add(diff); }
            if (diff < 0) { downs.add(diff); }
        }

        au = ups.stream().mapToInt(i -> abs(i)).average();
        ad = downs.stream().mapToInt(i -> abs(i)).average();

        if (au.isEmpty()) return 0;
        if (ad.isEmpty()) return 100;

        return (au.getAsDouble() / (ad.getAsDouble() + au.getAsDouble())) * 100;
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
