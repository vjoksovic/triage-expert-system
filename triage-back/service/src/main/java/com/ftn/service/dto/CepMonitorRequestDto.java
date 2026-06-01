package com.ftn.service.dto;

import java.util.ArrayList;
import java.util.List;

public class CepMonitorRequestDto {
    private List<VitalsReadingDto> vitalsReadings = new ArrayList<>();

    public List<VitalsReadingDto> getVitalsReadings() {
        return vitalsReadings;
    }

    public void setVitalsReadings(List<VitalsReadingDto> vitalsReadings) {
        this.vitalsReadings = vitalsReadings;
    }
}
