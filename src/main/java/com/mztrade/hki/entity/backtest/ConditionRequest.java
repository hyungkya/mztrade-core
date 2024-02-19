package com.mztrade.hki.entity.backtest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConditionRequest {
    private String baseIndicator;
    private String targetIndicator;
    private String constantBound;
    private String compareType;
    private String frequency;

    public String getBaseIndicator() {
        return baseIndicator;
    }

    public ConditionRequest setBaseIndicator(String baseIndicator) {
        this.baseIndicator = baseIndicator;
        return this;
    }

    public String getTargetIndicator() {
        return targetIndicator;
    }

    public ConditionRequest setTargetIndicator(String targetIndicator) {
        this.targetIndicator = targetIndicator;
        return this;
    }

    public String getConstantBound() {
        return constantBound;
    }

    public ConditionRequest setConstantBound(String constantBound) {
        this.constantBound = constantBound;
        return this;
    }

    public String getCompareType() {
        return compareType;
    }

    public ConditionRequest setCompareType(String compareType) {
        this.compareType = compareType;
        return this;
    }

    public String getFrequency() {
        return frequency;
    }

    public ConditionRequest setFrequency(String frequency) {
        this.frequency = frequency;
        return this;
    }

    public Indicator parseBaseIndicator() {
        List<String> s = Stream.of(this.baseIndicator.trim().split(",")).collect(Collectors.toList());
        String type = s.get(0);
        List<Float> params = s.subList(1, s.size()).stream().map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());
        return new Indicator(type, params);
    }

    public Indicator parseTargetIndicator() {
        List<String> s = Stream.of(this.targetIndicator.trim().split(",")).collect(Collectors.toList());
        String type = s.get(0);
        List<Float> params = s.subList(1, s.size()).stream().map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());
        return new Indicator(type, params);
    }

    public float parseConstantBound() {
        return Float.parseFloat(this.constantBound);
    }

    public List<Integer> parseFrequency() {
        return Stream.of(this.frequency.trim().split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ConditionRequest{" +
                "baseIndicator='" + baseIndicator + '\'' +
                ", targetIndicator='" + targetIndicator + '\'' +
                ", constantBound='" + constantBound + '\'' +
                ", compareType='" + compareType + '\'' +
                ", frequency='" + frequency + '\'' +
                '}';
    }
}
