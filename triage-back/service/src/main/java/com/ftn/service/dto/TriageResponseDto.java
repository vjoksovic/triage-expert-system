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
    private boolean redirectedToSecondary;
    private String originalWard;
    private int departmentP1Count;
    private int departmentOverloadThreshold;
    private boolean departmentOverloaded;

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

    public boolean isRedirectedToSecondary() {
        return redirectedToSecondary;
    }

    public void setRedirectedToSecondary(boolean redirectedToSecondary) {
        this.redirectedToSecondary = redirectedToSecondary;
    }

    public String getOriginalWard() {
        return originalWard;
    }

    public void setOriginalWard(String originalWard) {
        this.originalWard = originalWard;
    }

    public int getDepartmentP1Count() {
        return departmentP1Count;
    }

    public void setDepartmentP1Count(int departmentP1Count) {
        this.departmentP1Count = departmentP1Count;
    }

    public int getDepartmentOverloadThreshold() {
        return departmentOverloadThreshold;
    }

    public void setDepartmentOverloadThreshold(int departmentOverloadThreshold) {
        this.departmentOverloadThreshold = departmentOverloadThreshold;
    }

    public boolean isDepartmentOverloaded() {
        return departmentOverloaded;
    }

    public void setDepartmentOverloaded(boolean departmentOverloaded) {
        this.departmentOverloaded = departmentOverloaded;
    }
}
