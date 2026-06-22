package com.ftn.service.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    private static final String SEPSIS_QUERY = "isSepsaSuspected";
    private static final String SEPSIS_QUESTION =
            "Da li trenutni profil pacijenta sugerise pocetak sepse?";

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
            response.setReasoningTree(buildSepsisReasoningTree(session, patient, vitals));
            return response;
        } finally {
            if (session != null) {
                session.dispose();
            }
        }
    }

    private BackwardChainingNodeDto buildSepsisReasoningTree(KieSession session, Patient patient, Vitals vitals) {
        BackwardChainingNodeDto infectionRisk = buildInfectionRiskNode(session, patient, vitals);
        BackwardChainingNodeDto hemodynamicInstability = buildHemodynamicInstabilityNode(session, patient, vitals);
        boolean suspected = infectionRisk.isResult() && hemodynamicInstability.isResult();

        BackwardChainingNodeDto root = new BackwardChainingNodeDto();
        root.setQuery(SEPSIS_QUERY);
        root.setLabel("Sepsa suspektna");
        root.setResult(suspected);
        root.setExplanation(BackwardChainingFormatter.explainSepsisSuspected(suspected));
        root.setChildren(List.of(infectionRisk, hemodynamicInstability));
        return root;
    }

    private BackwardChainingNodeDto buildInfectionRiskNode(KieSession session, Patient patient, Vitals vitals) {
        BackwardChainingNodeDto fever = buildFeverNode(session, patient, vitals);
        BackwardChainingNodeDto confusion = buildConfusionNode(session);
        boolean infectionRisk = fever.isResult() || confusion.isResult();

        BackwardChainingNodeDto node = new BackwardChainingNodeDto();
        node.setQuery("hasInfectionRisk");
        node.setLabel("Rizik od infekcije");
        node.setResult(infectionRisk);
        node.setExplanation(BackwardChainingFormatter.explainInfectionRisk(infectionRisk));
        node.setChildren(List.of(fever, confusion));
        return node;
    }

    private BackwardChainingNodeDto buildHemodynamicInstabilityNode(
            KieSession session,
            Patient patient,
            Vitals vitals) {
        BackwardChainingNodeDto tachycardia = buildTachycardiaNode(session, patient, vitals);
        BackwardChainingNodeDto hypotension = buildHypotensionNode(session, patient, vitals);
        boolean hemodynamicInstability = tachycardia.isResult() && hypotension.isResult();

        BackwardChainingNodeDto node = new BackwardChainingNodeDto();
        node.setQuery("hasHemodynamicInstability");
        node.setLabel("Hemodinamska nestabilnost");
        node.setResult(hemodynamicInstability);
        node.setExplanation(BackwardChainingFormatter.explainHemodynamicInstability(hemodynamicInstability));
        node.setChildren(List.of(tachycardia, hypotension));
        return node;
    }

    private BackwardChainingNodeDto buildFeverNode(KieSession session, Patient patient, Vitals vitals) {
        boolean result = matchesQuery(session, "hasFever");
        return leafNode("hasFever", "Groznica", result,
                BackwardChainingFormatter.explainFever(patient, vitals, result));
    }

    private BackwardChainingNodeDto buildConfusionNode(KieSession session) {
        boolean result = matchesQuery(session, "hasConfusion");
        return leafNode("hasConfusion", "Konfuzija", result,
                BackwardChainingFormatter.explainConfusion(result));
    }

    private BackwardChainingNodeDto buildTachycardiaNode(KieSession session, Patient patient, Vitals vitals) {
        boolean result = matchesQuery(session, "hasTachycardia");
        return leafNode("hasTachycardia", "Tahikardija", result,
                BackwardChainingFormatter.explainTachycardia(patient, vitals, result));
    }

    private BackwardChainingNodeDto buildHypotensionNode(KieSession session, Patient patient, Vitals vitals) {
        boolean result = matchesQuery(session, "hasHypotension");
        return leafNode("hasHypotension", "Hipotenzija", result,
                BackwardChainingFormatter.explainHypotension(patient, vitals, result));
    }

    private BackwardChainingNodeDto leafNode(String queryName, String label, boolean result, String explanation) {
        BackwardChainingNodeDto node = new BackwardChainingNodeDto();
        node.setQuery(queryName);
        node.setLabel(label);
        node.setResult(result);
        node.setExplanation(explanation);
        return node;
    }

    private boolean matchesQuery(KieSession session, String queryName) {
        QueryResults results = session.getQueryResults(queryName);
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
