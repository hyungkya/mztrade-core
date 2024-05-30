package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Indicator {
    protected Algorithm algorithm;
    protected List<? extends Bar> bars;

    public Indicator setAlgorithm(String type, List<Float> params) {
        switch (type) {
            case "SMA1", "SMA3", "SMA2":
                algorithm = new SimpleMovingAverageAlgorithm(params);
                break;
            case "EMA":
                algorithm = new ExponentialMovingAverageAlgorithm(params);
                break;
            case "MACD":
                algorithm = new MovingAverageConvergenceDivergenceAlgorithm(params);
                break;
            case "MACD_SIGNAL":
                algorithm = new MovingAverageConvergenceDivergenceSignalAlgorithm(params);
                break;
            case "RSI":
                algorithm = new RelativeStrengthIndexAlgorithm(params);
                break;
            case "SF":
                algorithm = new StochasticFastAlgorithm(params);
                break;
            case "SS":
                algorithm = new StochasticSlowAlgorithm(params);
                break;
            case "CCI":
                algorithm = new CommodityChannelIndexAlgorithm(params);
                break;
            case "BBH":
                algorithm = new BollingerBandHighAlgorithm(params);
                break;
            case "BBL":
                algorithm = new BollingerBandLowAlgorithm(params);
                break;
            case "PPP":
                algorithm = new PivotPointPivotAlgorithm(params);
                break;
            case "PPS1":
                algorithm = new PivotPointSupport1Algorithm(params);
                break;
            case "PPS2":
                algorithm = new PivotPointSupport2Algorithm(params);
                break;
            case "PPR1":
                algorithm = new PivotPointResist1Algorithm(params);
                break;
            case "PPR2":
                algorithm = new PivotPointResist2Algorithm(params);
                break;
            case "OBV":
                algorithm = new OnBalanceVolumeAlgorithm(params);
                break;
            case "OBV_SIGNAL":
                algorithm = new OnBalanceVolumeSignalAlgorithm(params);
                break;
            case "DMI_PDI":
                algorithm = new DirectionalMovementIndexPlusAlgorithm(params);
                break;
            case "DMI_MDI":
                algorithm = new DirectionalMovementIndexMinusAlgorithm(params);
                break;
            case "ADX":
                algorithm = new AverageDirectionalIndexAlgorithm(params);
                break;
            case "MFI":
                algorithm = new MoneyFlowIndexAlgorithm(params);
                break;
        }
        return this;
    }

    public Indicator setBars(List<? extends Bar> bars) {
        this.bars = bars;
        return this;
    }

    public Map<LocalDateTime, Double> calculate() {
        return algorithm.calculate(bars);
    }

    public int requiredSize() {
        return algorithm.requiredSize();
    }
}
