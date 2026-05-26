package com.ftn.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private int age;
    private AgeCategory ageCategory;
    private Set<ChronicCondition> chronicConditions = new HashSet<>();

    public Patient() {
    }

    public Patient(String fullName, int age, Set<ChronicCondition> chronicConditions) {
        this.fullName = fullName;
        this.age = age;
        this.chronicConditions = chronicConditions;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public AgeCategory getAgeCategory() {
        return ageCategory;
    }

    public void setAgeCategory(AgeCategory ageCategory) {
        this.ageCategory = ageCategory;
    }

    public Set<ChronicCondition> getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(Set<ChronicCondition> chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, ageCategory, chronicConditions, fullName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Patient other = (Patient) obj;
        return age == other.age
                && Objects.equals(fullName, other.fullName)
                && ageCategory == other.ageCategory
                && Objects.equals(chronicConditions, other.chronicConditions);
    }

    @Override
    public String toString() {
        return "Patient [fullName=" + fullName + ", age=" + age + ", ageCategory=" + ageCategory
                + ", chronicConditions=" + chronicConditions + "]";
    }
}
