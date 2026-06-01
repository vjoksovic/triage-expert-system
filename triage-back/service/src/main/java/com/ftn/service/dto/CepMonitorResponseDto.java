package com.ftn.service.dto;

import java.util.ArrayList;
import java.util.List;

public class CepMonitorResponseDto {
    private List<CepAlarmDto> alarms = new ArrayList<>();
    private List<String> activatedRules = new ArrayList<>();

    public List<CepAlarmDto> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<CepAlarmDto> alarms) {
        this.alarms = alarms;
    }

    public List<String> getActivatedRules() {
        return activatedRules;
    }

    public void setActivatedRules(List<String> activatedRules) {
        this.activatedRules = activatedRules;
    }
}
