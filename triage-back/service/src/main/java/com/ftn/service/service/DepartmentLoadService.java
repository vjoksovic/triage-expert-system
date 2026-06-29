package com.ftn.service.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ftn.model.DepartmentTriageCase;
import com.ftn.model.Priority;
import com.ftn.model.Ward;
import com.ftn.service.dto.DepartmentCaseDto;
import com.ftn.service.dto.DepartmentLoadResponseDto;

@Service
public class DepartmentLoadService {
    public static final int OVERLOAD_THRESHOLD = 5;

    private final Map<String, DepartmentTriageCase> activeCases = new ConcurrentHashMap<>();

    public List<DepartmentTriageCase> getActiveP1CasesExcluding(String excludeCaseId, String currentPatientName) {
        String normalizedCurrentName = normalizeName(currentPatientName);
        return activeCases.values().stream()
                .filter(caseEntry -> caseEntry.getPriority() == Priority.P1)
                .filter(caseEntry -> excludeCaseId == null || !excludeCaseId.equals(caseEntry.getCaseId()))
                .filter(caseEntry -> !normalizedCurrentName.equals(normalizeName(caseEntry.getPatientName())))
                .collect(Collectors.toList());
    }

    public void registerOrUpdate(String caseId, String patientName, Priority priority, Ward ward) {
        if (caseId == null || caseId.isBlank()) {
            return;
        }
        if (priority != Priority.P1) {
            activeCases.remove(caseId);
            return;
        }
        String normalizedName = normalizeName(patientName);
        activeCases.entrySet().removeIf(entry ->
                normalizedName.equals(normalizeName(entry.getValue().getPatientName()))
                        && !caseId.equals(entry.getKey()));
        activeCases.put(caseId, new DepartmentTriageCase(caseId, patientName, priority, ward));
    }

    public void discharge(String caseId) {
        if (caseId != null) {
            activeCases.remove(caseId);
        }
    }

    public int getP1Count() {
        return (int) activeCases.values().stream()
                .filter(caseEntry -> caseEntry.getPriority() == Priority.P1)
                .map(caseEntry -> normalizeName(caseEntry.getPatientName()))
                .distinct()
                .count();
    }

    public boolean isOverloaded() {
        return getP1Count() > OVERLOAD_THRESHOLD;
    }

    public DepartmentLoadResponseDto getDepartmentLoad() {
        DepartmentLoadResponseDto response = new DepartmentLoadResponseDto();
        response.setP1Count(getP1Count());
        response.setOverloadThreshold(OVERLOAD_THRESHOLD);
        response.setOverloaded(isOverloaded());
        response.setActiveP1Cases(activeCases.values().stream()
                .filter(caseEntry -> caseEntry.getPriority() == Priority.P1)
                .sorted(Comparator.comparing(DepartmentTriageCase::getPatientName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .collect(Collectors.toList()));
        return response;
    }

    private static String normalizeName(String patientName) {
        if (patientName == null) {
            return "";
        }
        return patientName.trim().toLowerCase(Locale.ROOT);
    }

    private DepartmentCaseDto toDto(DepartmentTriageCase caseEntry) {
        DepartmentCaseDto dto = new DepartmentCaseDto();
        dto.setCaseId(caseEntry.getCaseId());
        dto.setPatientName(caseEntry.getPatientName());
        dto.setPriority(caseEntry.getPriority().name());
        dto.setWard(caseEntry.getWard().name());
        return dto;
    }
}
