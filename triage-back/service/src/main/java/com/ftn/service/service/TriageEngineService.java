package com.ftn.service.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import com.ftn.model.ChronicCondition;
import com.ftn.model.DepartmentTriageCase;
import com.ftn.model.Diagnosis;
import com.ftn.model.Patient;
import com.ftn.model.Priority;
import com.ftn.model.Symptom;
import com.ftn.model.SymptomType;
import com.ftn.model.TriageResult;
import com.ftn.model.Vitals;
import com.ftn.model.Ward;
import com.ftn.service.dto.TriageRequestDto;
import com.ftn.service.dto.TriageResponseDto;
import com.ftn.service.engine.RuleInsightFormatter;
import com.ftn.service.engine.RuleTraceAgendaEventListener;

@Service
public class TriageEngineService {
    private static final String OVERLOAD_RULE = "Redirect new P1 patient when department overloaded";

    private final KieContainer kieContainer;
    private final DepartmentLoadService departmentLoadService;

    public TriageEngineService(KieContainer kieContainer, DepartmentLoadService departmentLoadService) {
        this.kieContainer = kieContainer;
        this.departmentLoadService = departmentLoadService;
    }

    public TriageResponseDto evaluate(TriageRequestDto request) {
        KieSession session = kieContainer.newKieSession("triageKSession");
        try {
            RuleTraceAgendaEventListener listener = new RuleTraceAgendaEventListener();
            session.addEventListener(listener);

            Patient patient = new Patient(
                    request.getFullName(),
                    request.getAge(),
                    mapChronicConditions(request.getChronicConditions()));
            patient.setAgeInMonths(request.getAgeInMonths());
            patient.setPreterm(request.isPreterm());
            Vitals vitals = new Vitals(
                    request.getTemperature(),
                    request.getSystolicBloodPressure(),
                    request.getDiastolicBloodPressure(),
                    request.getPulse(),
                    request.getSpo2(),
                    LocalDateTime.now());

            departmentLoadService.getActiveP1CasesExcluding(request.getCaseId(), request.getFullName())
                    .forEach(caseEntry -> session.insert(new DepartmentTriageCase(
                            caseEntry.getCaseId(),
                            caseEntry.getPatientName(),
                            caseEntry.getPriority(),
                            caseEntry.getWard())));

            session.insert(patient);
            session.insert(vitals);
            mapSymptoms(request.getSymptoms())
                    .forEach(session::insert);

            session.fireAllRules();

            TriageResponseDto response = new TriageResponseDto();
            response.setActivatedRules(RuleInsightFormatter.toInsights(
                    listener.getFiredRules(), patient, vitals));

            response.setSymptoms(session.getObjects(o -> o instanceof Symptom).stream()
                    .map(Symptom.class::cast)
                    .map(s -> s.getType().name())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()));

            response.setDiagnoses(session.getObjects(o -> o instanceof Diagnosis).stream()
                    .map(Diagnosis.class::cast)
                    .map(d -> d.getType().name())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()));

            session.getObjects(o -> o instanceof TriageResult).stream()
                    .map(TriageResult.class::cast)
                    .findFirst()
                    .ifPresent(triage -> {
                        boolean redirected = listener.getFiredRules().contains(OVERLOAD_RULE);
                        Ward finalWard = triage.getWard();
                        response.setPriority(triage.getPriority().name());
                        response.setWard(finalWard.name());
                        response.setWarnings(triage.getWarnings());
                        response.setRedirectedToSecondary(redirected);
                        if (redirected && triage.getRedirectedFromWard() != null) {
                            response.setOriginalWard(triage.getRedirectedFromWard().name());
                        }
                        departmentLoadService.registerOrUpdate(
                                request.getCaseId(),
                                request.getFullName(),
                                triage.getPriority(),
                                finalWard);
                    });

            response.setDepartmentP1Count(departmentLoadService.getP1Count());
            response.setDepartmentOverloadThreshold(DepartmentLoadService.OVERLOAD_THRESHOLD);
            response.setDepartmentOverloaded(departmentLoadService.isOverloaded());

            return response;
        } finally {
            session.dispose();
        }
    }

    private Set<ChronicCondition> mapChronicConditions(List<String> chronicConditions) {
        if (chronicConditions == null) {
            return Collections.emptySet();
        }
        return chronicConditions.stream()
                .map(this::normalize)
                .map(ChronicCondition::valueOf)
                .collect(Collectors.toSet());
    }

    private List<Symptom> mapSymptoms(List<String> symptoms) {
        if (symptoms == null) {
            return Collections.emptyList();
        }
        return symptoms.stream()
                .map(this::normalize)
                .map(SymptomType::valueOf)
                .map(Symptom::new)
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
