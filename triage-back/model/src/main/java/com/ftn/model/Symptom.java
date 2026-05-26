package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

public class Symptom implements Serializable {
    private static final long serialVersionUID = 1L;

    private SymptomType type;

    public Symptom() {
    }

    public Symptom(SymptomType type) {
        this.type = type;
    }

    public SymptomType getType() {
        return type;
    }

    public void setType(SymptomType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Symptom other = (Symptom) obj;
        return type == other.type;
    }

    @Override
    public String toString() {
        return "Symptom [type=" + type + "]";
    }
}
