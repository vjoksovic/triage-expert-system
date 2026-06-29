package com.ftn.service.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.springframework.stereotype.Service;

import com.ftn.model.ChronicCondition;
import com.ftn.model.Patient;
import com.ftn.model.SepsisSuspectedAnswer;
import com.ftn.model.Symptom;
import com.ftn.model.SymptomType;
import com.ftn.model.Vitals;
import com.ftn.service.dto.BackwardChainingNodeDto;
import com.ftn.service.dto.SepsisQueryResponseDto;
import com.ftn.service.dto.TriageRequestDto;
import com.ftn.service.engine.BackwardChainingFormatter;

@Service
public class BackwardChainingService {
    private static final String SEPSIS_GOAL = "isSepsaSuspected";
    private static final String SEPSIS_QUERY = "isSepsaSuspected";
    private static final String PROVE_QUERY = "prove";
    private static final String SEPSIS_QUESTION =
            "Does the current patient profile suggest suspected sepsis?";

    private static final Map<String, List<String>> GOAL_CHILDREN = Map.of(
            SEPSIS_GOAL, List.of("hasInfectionRisk", "hasHemodynamicInstability"),
            "hasInfectionRisk", List.of("hasFever", "hasConfusion"),
            "hasHemodynamicInstability", List.of("hasTachycardia", "hasHypotension"));

    private static final Map<String, String> GOAL_LABELS = Map.of(
            SEPSIS_GOAL, "Suspected sepsis",
            "hasInfectionRisk", "Infection risk",
            "hasHemodynamicInstability", "Hemodynamic instability",
            "hasFever", "Fever",
            "hasConfusion", "Confusion",
            "hasTachycardia", "Tachycardia",
            "hasHypotension", "Hypotension");

    private final KieContainer kieContainer;

    public BackwardChainingService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public SepsisQueryResponseDto querySepsisSuspected(TriageRequestDto request) {
        KieSession session = null;
        try {
            session = kieContainer.newKieSession("triageKSession");

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

            session.insert(patient);
            session.insert(vitals);
            mapSymptoms(request.getSymptoms()).forEach(session::insert);

            session.fireAllRules(match -> match.getRule().getName().startsWith("Classify"));

            session.insert(SEPSIS_QUERY);
            session.getAgenda().getAgendaGroup("backward").setFocus();
            session.fireAllRules();

            SepsisSuspectedAnswer answer = session.getObjects(o -> o instanceof SepsisSuspectedAnswer).stream()
                    .map(SepsisSuspectedAnswer.class::cast)
                    .findFirst()
                    .orElseGet(() -> new SepsisSuspectedAnswer(false,
                            "Patient profile does not meet criteria for suspected sepsis."));

            SepsisQueryResponseDto response = new SepsisQueryResponseDto();
            response.setQuestion(SEPSIS_QUESTION);
            response.setSuspected(answer.isSuspected());
            response.setSummary(answer.getMessage());
            response.setReasoningTree(buildGoalNode(session, patient, vitals, SEPSIS_GOAL));
            return response;
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    private BackwardChainingNodeDto buildGoalNode(KieSession session, Patient patient, Vitals vitals, String goal) {
        boolean result = provesGoal(session, goal);
        List<String> childGoals = GOAL_CHILDREN.get(goal);

        BackwardChainingNodeDto node = new BackwardChainingNodeDto();
        node.setQuery(goal);
        node.setLabel(GOAL_LABELS.getOrDefault(goal, goal));
        node.setResult(result);
        node.setExplanation(explainGoal(goal, patient, vitals, result));

        if (childGoals != null) {
            node.setChildren(childGoals.stream()
                    .map(childGoal -> buildGoalNode(session, patient, vitals, childGoal))
                    .collect(Collectors.toList()));
        }

        return node;
    }

    private String explainGoal(String goal, Patient patient, Vitals vitals, boolean result) {
        switch (goal) {
            case "isSepsaSuspected":
                return BackwardChainingFormatter.explainSepsisSuspected(result);
            case "hasInfectionRisk":
                return BackwardChainingFormatter.explainInfectionRisk(result);
            case "hasHemodynamicInstability":
                return BackwardChainingFormatter.explainHemodynamicInstability(result);
            case "hasFever":
                return BackwardChainingFormatter.explainFever(patient, vitals, result);
            case "hasConfusion":
                return BackwardChainingFormatter.explainConfusion(result);
            case "hasTachycardia":
                return BackwardChainingFormatter.explainTachycardia(patient, vitals, result);
            case "hasHypotension":
                return BackwardChainingFormatter.explainHypotension(patient, vitals, result);
            default:
                return result ? "Goal satisfied: " + goal : "Goal not satisfied: " + goal;
        }
    }

    private boolean provesGoal(KieSession session, String goal) {
        QueryResults results = session.getQueryResults(PROVE_QUERY, goal);
        return results.iterator().hasNext();
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
