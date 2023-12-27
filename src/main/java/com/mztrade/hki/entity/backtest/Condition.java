package com.mztrade.hki.entity.backtest;
import com.mztrade.hki.entity.Bar;

import java.util.*;
import java.util.stream.Collectors;

public class Condition {
    private Indicator baseIndicator;
    private Indicator targetIndicator;
    private Float constantBound;
    private String compareType;
    private List<Integer> frequency;
    private Queue<Boolean> recentMatches;

    public Condition() {
        this.targetIndicator = new Indicator("NONE", Collections.emptyList());
        this.frequency = List.of(1, 1);
        this.recentMatches = new LinkedList<>();
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
        if (compareType.matches(">")) {
            if (baseIndicator.calculate(bars) > targetIndicator.calculate(bars) + constantBound) {
                recentMatches.add(true);
            } else {
                recentMatches.add(false);
            }
        }
        if (compareType.matches("<")) {
            if (baseIndicator.calculate(bars) < targetIndicator.calculate(bars) + constantBound) {
                recentMatches.add(true);
            } else {
                recentMatches.add(false);
            }
        }
        if (recentMatches.size() > frequency.get(0)) {
            recentMatches.remove();
        }
        int matchCount = recentMatches.stream().filter(m -> m == true).collect(Collectors.toList()).size();
        if (matchCount >= frequency.get(1)) {
            return true;
        }
        return false;
    }
}
