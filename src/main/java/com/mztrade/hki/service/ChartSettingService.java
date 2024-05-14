package com.mztrade.hki.service;

import com.mztrade.hki.entity.ChartSetting;
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
        return isSuccess;
    }

    public ChartSetting get(int uid) {
        ChartSetting chartSetting = chartSettingRepository.get(uid);
        return chartSetting;
    }
}
