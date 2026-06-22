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
                reading(base, 98, 78),
                reading(base.plusMinutes(5), 96, 92),
                reading(base.plusMinutes(9), 91, 105)));

        CepMonitorResponseDto response = service.monitorVitalsStream(request);

        List<CepAlarmDto> alarms = response.getAlarms();
        assertEquals(1, alarms.size());
        assertEquals("SpO2 Rapid Drop", alarms.get(0).getRuleName());
        assertEquals("P1", alarms.get(0).getPriority());
        assertEquals("Sudden drop in oxygen saturation with compensatory tachycardia - assess airway and breathing.",
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
                reading(base, 98, 80),
                reading(base.plusMinutes(5), 96, 80),
                reading(base.plusMinutes(9), 94, 80)));

        CepMonitorResponseDto response = service.monitorVitalsStream(request);

        assertTrue(response.getAlarms().isEmpty());
    }

    @Test
    void triggersAcuteRespiratoryCrashWhenSpo2DropsWithoutCompensatoryPulse() {
        CepMonitoringService service = new CepMonitoringService(kieContainer);
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        CepMonitorRequestDto request = new CepMonitorRequestDto();
        request.setVitalsReadings(Arrays.asList(
                reading(base, 98, 82),
                reading(base.plusMinutes(4), 95, 83),
                reading(base.plusMinutes(8), 91, 84)));

        CepMonitorResponseDto response = service.monitorVitalsStream(request);

        assertEquals(1, response.getAlarms().size());
        assertEquals("Acute Respiratory Crash Prediction", response.getAlarms().get(0).getRuleName());
        assertEquals("P1", response.getAlarms().get(0).getPriority());
        assertTrue(response.getAlarms().get(0).getMessage().contains("without compensatory tachycardia"));
        assertTrue(response.getActivatedRules().stream()
                .anyMatch(insight -> insight.toLowerCase().contains("acute respiratory crash")));
    }

    @Test
    void triggersAcuteRespiratoryCrashWhenSpo2AndPulseCollapseTogether() {
        CepMonitoringService service = new CepMonitoringService(kieContainer);
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        CepMonitorRequestDto request = new CepMonitorRequestDto();
        request.setVitalsReadings(Arrays.asList(
                reading(base, 97, 105),
                reading(base.plusMinutes(3), 94, 98),
                reading(base.plusMinutes(7), 90, 82)));

        CepMonitorResponseDto response = service.monitorVitalsStream(request);

        assertEquals(1, response.getAlarms().size());
        assertEquals("Acute Respiratory Crash Prediction", response.getAlarms().get(0).getRuleName());
        assertTrue(response.getAlarms().get(0).getMessage().contains("pulse collapse"));
    }

    @Test
    void doesNotTriggerAcuteRespiratoryCrashWhenPulseCompensates() {
        CepMonitoringService service = new CepMonitoringService(kieContainer);
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);

        CepMonitorRequestDto request = new CepMonitorRequestDto();
        request.setVitalsReadings(Arrays.asList(
                reading(base, 98, 78),
                reading(base.plusMinutes(4), 95, 92),
                reading(base.plusMinutes(8), 91, 108)));

        CepMonitorResponseDto response = service.monitorVitalsStream(request);

        assertEquals(1, response.getAlarms().size());
        assertEquals("SpO2 Rapid Drop", response.getAlarms().get(0).getRuleName());
        assertTrue(response.getAlarms().stream()
                .noneMatch(alarm -> "Acute Respiratory Crash Prediction".equals(alarm.getRuleName())));
    }

    private static VitalsReadingDto reading(LocalDateTime measuredAt, int spo2) {
        return reading(measuredAt, spo2, 80);
    }

    private static VitalsReadingDto reading(LocalDateTime measuredAt, int spo2, int pulse) {
        VitalsReadingDto dto = new VitalsReadingDto();
        dto.setTemperature(36.8);
        dto.setSystolicBloodPressure(120);
        dto.setDiastolicBloodPressure(80);
        dto.setPulse(pulse);
        dto.setSpo2(spo2);
        dto.setMeasuredAt(measuredAt);
        return dto;
    }
}
