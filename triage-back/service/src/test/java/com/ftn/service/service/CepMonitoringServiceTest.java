package com.ftn.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ftn.service.dto.CepAlarmDto;
import com.ftn.service.dto.CepMonitorRequestDto;
import com.ftn.service.dto.CepMonitorResponseDto;
import com.ftn.service.dto.VitalsReadingDto;

@SpringBootTest
class CepMonitoringServiceTest {

    @Autowired
    private KieContainer kieContainer;

    @Test
    void triggersAlarmWhenSpo2DropsMoreThanFivePercentWithinTenMinutes() {
        CepMonitoringService service = new CepMonitoringService(kieContainer);
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        CepMonitorRequestDto request = new CepMonitorRequestDto();
        request.setVitalsReadings(Arrays.asList(
                reading(base, 98),
                reading(base.plusMinutes(5), 96),
                reading(base.plusMinutes(9), 91)));

        CepMonitorResponseDto response = service.monitorSpo2Trend(request);

        List<CepAlarmDto> alarms = response.getAlarms();
        assertEquals(1, alarms.size());
        assertEquals("SpO2 Rapid Drop", alarms.get(0).getRuleName());
        assertEquals("P1", alarms.get(0).getPriority());
        assertEquals("Sudden drop in oxygen saturation - assess airway and breathing.",
                alarms.get(0).getMessage());
        assertEquals(1, response.getActivatedRules().size());
        assertTrue(response.getActivatedRules().get(0).contains("rapid SpO2 drop"));
    }

    @Test
    void doesNotTriggerAlarmForSmallSpo2Drop() {
        CepMonitoringService service = new CepMonitoringService(kieContainer);
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        CepMonitorRequestDto request = new CepMonitorRequestDto();
        request.setVitalsReadings(Arrays.asList(
                reading(base, 98),
                reading(base.plusMinutes(5), 96),
                reading(base.plusMinutes(9), 94)));

        CepMonitorResponseDto response = service.monitorSpo2Trend(request);

        assertTrue(response.getAlarms().isEmpty());
    }

    private static VitalsReadingDto reading(LocalDateTime measuredAt, int spo2) {
        VitalsReadingDto dto = new VitalsReadingDto();
        dto.setTemperature(36.8);
        dto.setSystolicBloodPressure(120);
        dto.setDiastolicBloodPressure(80);
        dto.setPulse(80);
        dto.setSpo2(spo2);
        dto.setMeasuredAt(measuredAt);
        return dto;
    }
}
