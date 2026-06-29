package com.ftn.model;

public final class VitalSignThresholds {

    private VitalSignThresholds() {
    }

    public static AgeGroup resolveGroup(Patient patient) {
        if (patient == null) {
            return AgeGroup.ADOLESCENT;
        }
        if (patient.getAgeGroup() != null) {
            return patient.getAgeGroup();
        }
        if (patient.isPreterm()) {
            return AgeGroup.PRETERM;
        }

        int age = patient.getAge();
        Integer ageInMonths = patient.getAgeInMonths();

        if (age == 0) {
            if (ageInMonths != null && ageInMonths < 1) {
                return AgeGroup.NEWBORN;
            }
            return AgeGroup.INFANT;
        }
        if (age >= 1 && age <= 2) {
            return AgeGroup.TODDLER;
        }
        if (age >= 3 && age <= 5) {
            return AgeGroup.PRESCHOOL;
        }
        if (age >= 6 && age <= 12) {
            return AgeGroup.SCHOOL_AGE;
        }
        return AgeGroup.ADOLESCENT;
    }

    public static int tachycardiaThreshold(AgeGroup group) {
        if (group == null) {
            return 100;
        }
        switch (group) {
            case PRETERM:
                return 180;
            case NEWBORN:
                return 160;
            case INFANT:
                return 140;
            case TODDLER:
                return 130;
            case PRESCHOOL:
                return 110;
            case SCHOOL_AGE:
            case ADOLESCENT:
                return 100;
            default:
                return 100;
        }
    }

    public static int tachycardiaThreshold(Patient patient) {
        return tachycardiaThreshold(resolveGroup(patient));
    }

    public static boolean isTachycardia(Patient patient, int pulse) {
        return pulse > tachycardiaThreshold(patient);
    }

    public static double feverThreshold(AgeGroup group) {
        return isPediatric(group) ? 37.5 : 38.0;
    }

    public static double feverThreshold(Patient patient) {
        return feverThreshold(resolveGroup(patient));
    }

    public static boolean isFever(Patient patient, double temperature) {
        return temperature > feverThreshold(patient);
    }

    public static int hypotensionThreshold(AgeGroup group) {
        return isPediatric(group) ? 90 : 100;
    }

    public static int hypotensionThreshold(Patient patient) {
        return hypotensionThreshold(resolveGroup(patient));
    }

    public static boolean isHypotension(Patient patient, int systolicBloodPressure) {
        return systolicBloodPressure < hypotensionThreshold(patient);
    }

    public static int hypoxemiaThreshold(AgeGroup group) {
        return isPediatric(group) ? 95 : 94;
    }

    public static int hypoxemiaThreshold(Patient patient) {
        return hypoxemiaThreshold(resolveGroup(patient));
    }

    public static boolean isHypoxemia(Patient patient, int spo2) {
        return spo2 < hypoxemiaThreshold(patient);
    }

    public static boolean isPediatric(AgeGroup group) {
        return group != null && group != AgeGroup.ADOLESCENT;
    }

    public static String formatGroup(AgeGroup group) {
        if (group == null) {
            return "the patient's age group";
        }
        switch (group) {
            case PRETERM:
                return "a preterm infant";
            case NEWBORN:
                return "a newborn (0–1 month)";
            case INFANT:
                return "an infant (1–12 months)";
            case TODDLER:
                return "a toddler (1–3 years)";
            case PRESCHOOL:
                return "a preschool child (3–5 years)";
            case SCHOOL_AGE:
                return "a school-age child (6–12 years)";
            case ADOLESCENT:
                return "an adolescent or adult (13+ years)";
            default:
                return "the patient's age group";
        }
    }
}
