package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

public class DepartmentTriageCase implements Serializable {
    private static final long serialVersionUID = 1L;

    private String caseId;
    private String patientName;
    private Priority priority;
    private Ward ward;

    public DepartmentTriageCase() {
    }

    public DepartmentTriageCase(String caseId, String patientName, Priority priority, Ward ward) {
        this.caseId = caseId;
        this.patientName = patientName;
        this.priority = priority;
        this.ward = ward;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, patientName, priority, ward);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DepartmentTriageCase other = (DepartmentTriageCase) obj;
        return Objects.equals(caseId, other.caseId)
                && Objects.equals(patientName, other.patientName)
                && priority == other.priority
                && ward == other.ward;
    }
}
