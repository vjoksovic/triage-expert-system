package com.ftn.service.engine;

import com.ftn.model.AgeCategory;
import com.ftn.model.Patient;
import com.ftn.model.Vitals;

public final class BackwardChainingFormatter {

    private BackwardChainingFormatter() {
    }

    public static String explainFever(Patient patient, Vitals vitals, boolean result) {
        if (!result) {
            return "Temperature is within the age-specific limit — fever not detected.";
        }
        AgeCategory category = patient.getAgeCategory();
        double limit = feverLimit(category);
        return String.format(
                "Fever detected: temperature %.1f °C exceeds the %.1f °C threshold for %s.",
                vitals.getTemperature(),
                limit,
                formatAgeCategory(category));
    }

    public static String explainConfusion(boolean result) {
        if (!result) {
            return "Confusion symptom not reported.";
        }
        return "Confusion symptom is present — infection risk marker.";
    }

    public static String explainTachycardia(Patient patient, Vitals vitals, boolean result) {
        if (!result) {
            return "Pulse is within the age-specific limit — tachycardia not detected.";
        }
        AgeCategory category = patient.getAgeCategory();
        int limit = tachycardiaLimit(category);
        return String.format(
                "Tachycardia detected: pulse %d/min exceeds the %d/min threshold for %s.",
                vitals.getPulse(),
                limit,
                formatAgeCategory(category));
    }

    public static String explainHypotension(Patient patient, Vitals vitals, boolean result) {
        if (!result) {
            return "Systolic blood pressure is within the age-specific limit — hypotension not detected.";
        }
        AgeCategory category = patient.getAgeCategory();
        int limit = hypotensionLimit(category);
        return String.format(
                "Hypotension detected: systolic BP %d mmHg is below the %d mmHg threshold for %s.",
                vitals.getSystolicBloodPressure(),
                limit,
                formatAgeCategory(category));
    }

    public static String explainInfectionRisk(boolean result) {
        if (!result) {
            return "Neither fever nor confusion is present — infection risk not established.";
        }
        return "Infection risk established through fever and/or confusion markers.";
    }

    public static String explainHemodynamicInstability(boolean result) {
        if (!result) {
            return "Tachycardia and hypotension are not both present — hemodynamic instability not confirmed.";
        }
        return "Hemodynamic instability confirmed: both tachycardia and hypotension are present.";
    }

    public static String explainSepsisSuspected(boolean result) {
        if (!result) {
            return "Patient profile does not meet criteria for suspected sepsis.";
        }
        return "Patient profile meets criteria for suspected sepsis — infection risk with hemodynamic instability.";
    }

    private static double feverLimit(AgeCategory category) {
        if (category == AgeCategory.CHILD) {
            return 37.5;
        }
        if (category == AgeCategory.SENIOR) {
            return 37.8;
        }
        return 38.0;
    }

    private static int tachycardiaLimit(AgeCategory category) {
        if (category == AgeCategory.SENIOR) {
            return 90;
        }
        return 100;
    }

    private static int hypotensionLimit(AgeCategory category) {
        if (category == AgeCategory.CHILD) {
            return 90;
        }
        if (category == AgeCategory.SENIOR) {
            return 110;
        }
        return 100;
    }

    private static String formatAgeCategory(AgeCategory category) {
        if (category == null) {
            return "the patient's age group";
        }
        switch (category) {
            case CHILD:
                return "a child";
            case SENIOR:
                return "a senior";
            default:
                return "an adult";
        }
    }
}
