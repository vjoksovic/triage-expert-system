package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

public class Diagnosis implements Serializable {
    private static final long serialVersionUID = 1L;

    private DiagnosisType type;
    private String explanation;

    public Diagnosis() {
    }

    public Diagnosis(DiagnosisType type, String explanation) {
        this.type = type;
        this.explanation = explanation;
    }

    public DiagnosisType getType() {
        return type;
    }

    public void setType(DiagnosisType type) {
        this.type = type;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(explanation, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Diagnosis other = (Diagnosis) obj;
        return type == other.type && Objects.equals(explanation, other.explanation);
    }

    @Override
    public String toString() {
        return "Diagnosis [type=" + type + ", explanation=" + explanation + "]";
    }
}
