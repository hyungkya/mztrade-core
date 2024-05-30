package com.mztrade.hki.entity.backtest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.service.IndicatorService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter @Setter @ToString @Builder(toBuilder = true)
@AllArgsConstructor
@Slf4j
public class Condition {
    private String baseType;
    private String baseParam;
    private String targetType;
    private String targetParam;
    private String compareType;
    private List<Integer> compareParam;
    @JsonIgnore
    private Map<String, Map<LocalDateTime, Double>> bases;
    @JsonIgnore
    private Map<String, Map<LocalDateTime, Double>> targets;
    @JsonIgnore
    private Map<String, List<Boolean>> recentMatches;

    public void setup(List<? extends Bar> bars, IndicatorService indicatorService, String ticker) {
        if (bases == null) bases = new HashMap<>();
        if (targets == null) targets = new HashMap<>();

        if (baseType.equals("indicator")) {
            List<String> s = Stream.of(this.getBaseParam().trim().split(",")).collect(Collectors.toList());
            String type = s.get(0);
            List<Float> params = s.subList(1, s.size()).stream().map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());

            bases.put(ticker, indicatorService.getIndicators(
                    bars,
                    type,
                    params
            ));
        } else if (baseType.equals("price")) {
            Map<LocalDateTime, Double> temp = new HashMap<>();
            for (Bar bar : bars) {
                if (baseParam.equals("close")) {
                    temp.put(bar.getDate(), bar.getClose().doubleValue());
                } else if (baseParam.equals("open")) {
                    temp.put(bar.getDate(), bar.getOpen().doubleValue());
                } else if (baseParam.equals("high")) {
                    temp.put(bar.getDate(), bar.getHigh().doubleValue());
                } else if (baseParam.equals("low")) {
                    temp.put(bar.getDate(), bar.getLow().doubleValue());
                }
            }
            bases.put(ticker, temp);
        } else {
            Map<LocalDateTime, Double> temp = new HashMap<>();
            for (Bar bar : bars) {
                temp.put(bar.getDate(), Double.parseDouble(baseParam));
            }
            bases.put(ticker, temp);
        }
        if (targetType.equals("indicator")) {
            List<String> s = Stream.of(this.getTargetParam().trim().split(",")).collect(Collectors.toList());
            String type = s.get(0);
            List<Float> params = s.subList(1, s.size()).stream().map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());

            targets.put(ticker, indicatorService.getIndicators(
                    bars,
                    type,
                    params
            ));
        } else if (targetType.equals("price")) {
            Map<LocalDateTime, Double> temp = new HashMap<>();
            for (Bar bar : bars) {
                if (targetParam.equals("close")) {
                    temp.put(bar.getDate(), bar.getClose().doubleValue());
                } else if (targetParam.equals("open")) {
                    temp.put(bar.getDate(), bar.getOpen().doubleValue());
                } else if (targetParam.equals("high")) {
                    temp.put(bar.getDate(), bar.getHigh().doubleValue());
                } else if (targetParam.equals("low")) {
                    temp.put(bar.getDate(), bar.getLow().doubleValue());
                }
            }
            targets.put(ticker, temp);
        } else {
            Map<LocalDateTime, Double> temp = new HashMap<>();
            for (Bar bar : bars) {
                temp.put(bar.getDate(), Double.parseDouble(targetParam));
            }
            targets.put(ticker, temp);
        }
    }

    public boolean check(String ticker, LocalDateTime date) {
        if (recentMatches == null) recentMatches = new HashMap<>();
        if (!recentMatches.containsKey(ticker)) {
            recentMatches.put(ticker, new ArrayList<>());
        }

        if (compareType.matches(">") || compareType.matches(">>")) {
            if (bases.get(ticker).containsKey(date)
                    && targets.get(ticker).containsKey(date)
                    && bases.get(ticker).get(date) > targets.get(ticker).get(date)) {
                recentMatches.get(ticker).add(true);
            } else {
                recentMatches.get(ticker).add(false);
            }
        }
        if (compareType.matches("<") || compareType.matches("<<")) {
            if (bases.get(ticker).containsKey(date)
                    && targets.get(ticker).containsKey(date)
                    && bases.get(ticker).get(date) < targets.get(ticker).get(date)) {
                recentMatches.get(ticker).add(true);
            } else {
                recentMatches.get(ticker).add(false);
            }
        }
        if (recentMatches.get(ticker).size() > compareParam.get(0)) {
            recentMatches.get(ticker).removeFirst();
        }
        if (recentMatches.get(ticker).size() != compareParam.get(0)) {
            return false;
        } else {
            int falseTrueTransitionIndex = compareParam.get(0) - compareParam.get(1);
            for (int i = 0; i < compareParam.get(0); i++) {
                if (i < falseTrueTransitionIndex && recentMatches.get(ticker).get(i) != false) {
                    return false;
                } else if (i >= falseTrueTransitionIndex && recentMatches.get(ticker).get(i) != true) {
                    return false;
                }
            }
        }
        return true;
    }
}
