package com.ftn.service.dto;

import java.util.ArrayList;
import java.util.List;

public class TriageResponseDto {
    private List<String> activatedRules = new ArrayList<>();
    private List<String> symptoms = new ArrayList<>();
    private List<String> diagnoses = new ArrayList<>();
    private String priority;
    private String ward;
    private List<String> warnings = new ArrayList<>();

    public List<String> getActivatedRules() {
        return activatedRules;
    }

    public void setActivatedRules(List<String> activatedRules) {
        this.activatedRules = activatedRules;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<String> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
