package com.mztrade.hki.entity.backtest;
import com.mztrade.hki.entity.Bar;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Condition {
    private Indicator baseIndicator;
    private Indicator targetIndicator;
    private Float constantBound;
    private String compareType;
    private List<Integer> frequency;
    private Map<String, List<Boolean>> recentMatches;

    public Condition() {
        this.targetIndicator = new Indicator("NONE", Collections.emptyList());
        this.frequency = List.of(1, 1);
        this.recentMatches = new HashMap<>();
    }

    public Condition setBaseIndicator(Indicator baseIndicator) {
        this.baseIndicator = baseIndicator;
        return this;
    }

    public Condition setTargetIndicator(Indicator targetIndicator) {
        this.targetIndicator = targetIndicator;
        return this;
    }

    public Condition setConstantBound(Float constantBound) {
        this.constantBound = constantBound;
        return this;
    }

    public Condition setCompareType(String compareType) {
        this.compareType = compareType;
        return this;
    }

    public Condition setFrequency(List<Integer> frequency) {
        this.frequency = frequency;
        return this;
    }

    public boolean check(List<Bar> bars) {
        String ticker = bars.getFirst().getTicker();
        if (!recentMatches.containsKey(ticker)) {
            recentMatches.put(ticker, new ArrayList<>());
        }
        log.trace("baseIndicator: " + baseIndicator.calculate(bars) + " targetIndicator: " + targetIndicator.calculate(bars));
        if (compareType.matches(">") || compareType.matches(">>")) {
            if (baseIndicator.calculate(bars) > targetIndicator.calculate(bars) + constantBound) {
                recentMatches.get(ticker).add(true);
            } else {
                recentMatches.get(ticker).add(false);
            }
        }
        if (compareType.matches("<") || compareType.matches("<<")) {
            if (baseIndicator.calculate(bars) < targetIndicator.calculate(bars) + constantBound) {
                recentMatches.get(ticker).add(true);
            } else {
                recentMatches.get(ticker).add(false);
            }
        }
        if (recentMatches.get(ticker).size() > frequency.get(0)) {
            recentMatches.get(ticker).removeFirst();
        }

        if (recentMatches.get(ticker).size() != frequency.get(0)) {
            return false;
        } else {
            int falseTrueTransitionIndex = frequency.get(0) - frequency.get(1);
            for (int i = 0; i < frequency.get(0); i++) {
                if (i < falseTrueTransitionIndex && recentMatches.get(ticker).get(i) != false) {
                    return false;
                } else if (i >= falseTrueTransitionIndex && recentMatches.get(ticker).get(i) != true) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Condition{" +
                "baseIndicator=" + baseIndicator +
                ", targetIndicator=" + targetIndicator +
                ", constantBound=" + constantBound +
                ", compareType='" + compareType + '\'' +
                ", frequency=" + frequency +
                ", recentMatches=" + recentMatches +
                '}';
    }
}
