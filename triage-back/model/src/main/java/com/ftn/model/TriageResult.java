package com.ftn.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TriageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Priority priority;
    private Ward ward;
    private List<String> warnings = new ArrayList<>();

    public TriageResult() {
    }

    public TriageResult(Priority priority, Ward ward) {
        this.priority = priority;
        this.ward = ward;
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

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, ward, warnings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TriageResult other = (TriageResult) obj;
        return priority == other.priority && ward == other.ward && Objects.equals(warnings, other.warnings);
    }

    @Override
    public String toString() {
        return "TriageResult [priority=" + priority + ", ward=" + ward + ", warnings=" + warnings + "]";
    }
}
