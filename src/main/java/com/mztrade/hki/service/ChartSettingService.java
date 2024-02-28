package com.mztrade.hki.service;

import com.mztrade.hki.entity.ChartSetting;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.repository.BacktestHistoryRepository;
import com.mztrade.hki.repository.ChartSettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChartSettingService {

    private ChartSettingRepository chartSettingRepository;

    @Autowired
    public ChartSettingService(ChartSettingRepository chartSettingRepository){
        this.chartSettingRepository = chartSettingRepository;
    }

    public boolean save(ChartSetting chartSetting) {
        boolean isSuccess = chartSettingRepository.save(chartSetting);
        log.debug(String.format("[ChartSettingService] save(ChartSetting: %s) -> isSuccess: %b", chartSetting, isSuccess));
        return isSuccess;
    }

    public ChartSetting get(int uid) {
        ChartSetting chartSetting = chartSettingRepository.get(uid);
        log.debug(String.format("[ChartSettingService] get(int: %d) -> chartSetting: %s", uid, chartSetting));
        return chartSetting;
    }
}
