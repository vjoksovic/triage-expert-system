package com.ftn.service.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionClock;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.stereotype.Service;

import com.ftn.model.CepAlarm;
import com.ftn.model.Vitals;
import com.ftn.service.dto.CepAlarmDto;
import com.ftn.service.dto.CepMonitorRequestDto;
import com.ftn.service.dto.CepMonitorResponseDto;
import com.ftn.service.dto.VitalsReadingDto;
import com.ftn.service.engine.RuleInsightFormatter;
import com.ftn.service.engine.RuleTraceAgendaEventListener;

@Service
public class CepMonitoringService {
    private final KieContainer kieContainer;

    public CepMonitoringService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public CepMonitorResponseDto monitorSpo2Trend(CepMonitorRequestDto request) {
        List<VitalsReadingDto> readings = request.getVitalsReadings();
        if (readings == null || readings.isEmpty()) {
            return new CepMonitorResponseDto();
        }

        KieSession session = null;
        try {
            session = kieContainer.newKieSession("cepKSession");
            RuleTraceAgendaEventListener listener = new RuleTraceAgendaEventListener();
            session.addEventListener(listener);

            SessionClock clock = session.getSessionClock();
            if (!(clock instanceof SessionPseudoClock)) {
                throw new IllegalStateException("CEP session requires a pseudo clock for vitals stream replay.");
            }
            SessionPseudoClock pseudoClock = (SessionPseudoClock) clock;

            List<VitalsReadingDto> sortedReadings = readings.stream()
                    .sorted(Comparator.comparing(VitalsReadingDto::getMeasuredAt,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            for (VitalsReadingDto reading : sortedReadings) {
                advanceClock(pseudoClock, reading.getMeasuredAt());
                session.insert(toVitals(reading));
                session.fireAllRules();
            }

            CepMonitorResponseDto response = new CepMonitorResponseDto();
            response.setActivatedRules(RuleInsightFormatter.toInsights(
                    listener.getFiredRules(), null, null));
            response.setAlarms(session.getObjects(o -> o instanceof CepAlarm).stream()
                    .map(CepAlarm.class::cast)
                    .map(this::toDto)
                    .collect(Collectors.toList()));
            return response;
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    private void advanceClock(SessionPseudoClock pseudoClock, LocalDateTime measuredAt) {
        LocalDateTime timestamp = measuredAt != null ? measuredAt : LocalDateTime.now();
        long targetMillis = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long currentMillis = pseudoClock.getCurrentTime();
        if (targetMillis > currentMillis) {
            pseudoClock.advanceTime(targetMillis - currentMillis, TimeUnit.MILLISECONDS);
        }
    }

    private Vitals toVitals(VitalsReadingDto reading) {
        return new Vitals(
                reading.getTemperature(),
                reading.getSystolicBloodPressure(),
                reading.getDiastolicBloodPressure(),
                reading.getPulse(),
                reading.getSpo2(),
                reading.getMeasuredAt() != null ? reading.getMeasuredAt() : LocalDateTime.now());
    }

    private CepAlarmDto toDto(CepAlarm alarm) {
        CepAlarmDto dto = new CepAlarmDto();
        dto.setRuleName(alarm.getRuleName());
        dto.setPriority(alarm.getPriority().name());
        dto.setMessage(alarm.getMessage());
        return dto;
    }
}
