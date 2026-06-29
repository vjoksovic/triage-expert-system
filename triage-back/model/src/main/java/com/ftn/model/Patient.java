package com.ftn.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private int age;
    private Integer ageInMonths;
    private boolean preterm;
    private AgeGroup ageGroup;
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

    public Integer getAgeInMonths() {
        return ageInMonths;
    }

    public void setAgeInMonths(Integer ageInMonths) {
        this.ageInMonths = ageInMonths;
    }

    public boolean isPreterm() {
        return preterm;
    }

    public void setPreterm(boolean preterm) {
        this.preterm = preterm;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public Set<ChronicCondition> getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(Set<ChronicCondition> chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, ageGroup, ageInMonths, chronicConditions, fullName, preterm);
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
                && preterm == other.preterm
                && Objects.equals(fullName, other.fullName)
                && ageGroup == other.ageGroup
                && Objects.equals(ageInMonths, other.ageInMonths)
                && Objects.equals(chronicConditions, other.chronicConditions);
    }

    @Override
    public String toString() {
        return "Patient [fullName=" + fullName + ", age=" + age + ", ageGroup=" + ageGroup
                + ", chronicConditions=" + chronicConditions + "]";
    }
}
