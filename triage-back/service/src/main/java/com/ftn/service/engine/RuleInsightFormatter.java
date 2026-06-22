package com.ftn.service.engine;

import java.util.List;
import java.util.stream.Collectors;

import com.ftn.model.AgeCategory;
import com.ftn.model.Patient;
import com.ftn.model.Vitals;

public final class RuleInsightFormatter {

    private RuleInsightFormatter() {
    }

    public static List<String> toInsights(List<String> firedRuleNames, Patient patient, Vitals vitals) {
        return firedRuleNames.stream()
                .map(ruleName -> explain(ruleName, patient, vitals))
                .collect(Collectors.toList());
    }

    public static String explain(String ruleName, Patient patient, Vitals vitals) {
        if (ruleName == null) {
            return "";
        }

        switch (ruleName) {
            case "Classify child patient":
            case "Classify adult patient":
            case "Classify senior patient":
                return explainAgeClassification(patient);
            case "Detect fever in child":
            case "Detect fever in adult":
            case "Detect fever in senior":
                return explainFever(patient, vitals);
            case "Detect tachycardia in child":
            case "Detect tachycardia in adult":
            case "Detect tachycardia in senior":
                return explainTachycardia(patient, vitals);
            case "Detect hypotension in child":
            case "Detect hypotension in adult":
            case "Detect hypotension in senior":
                return explainHypotension(patient, vitals);
            case "Detect hypoxemia in child":
            case "Detect hypoxemia in adult":
            case "Detect hypoxemia in senior":
                return explainHypoxemia(patient, vitals);
            case "Sepsis suspected in diabetic patient":
                return "Patient has suspected sepsis: fever and tachycardia with diabetes (priority P1, internal medicine).";
            case "SpO2 Rapid Drop":
                return "Patient has a rapid SpO2 drop: oxygen saturation fell more than 5% within 10 minutes, with compensatory tachycardia.";
            case "Acute Respiratory Crash Prediction - Pulse Collapse":
            case "Acute Respiratory Crash Prediction - Failed Compensation":
            case "Acute Respiratory Crash Prediction":
                return "Acute respiratory crash risk: SpO2 fell more than 5% within 10 minutes without adequate pulse compensation, or with concurrent pulse collapse.";
            default:
                return "Rule satisfied: " + ruleName;
        }
    }

    private static String explainAgeClassification(Patient patient) {
        if (patient == null) {
            return "Patient age group was classified.";
        }
        String group = formatAgeCategory(patient.getAgeCategory());
        return String.format("Patient is classified as %s (age %d).", group, patient.getAge());
    }

    private static String explainFever(Patient patient, Vitals vitals) {
        AgeCategory category = patient != null ? patient.getAgeCategory() : null;
        double limit = feverLimit(category);
        if (vitals != null && category != null) {
            return String.format(
                    "Patient has fever: temperature %.1f C is above the %.1f C limit for %s.",
                    vitals.getTemperature(),
                    limit,
                    formatAgeCategory(category));
        }
        return "Patient has fever: temperature is above the age-specific limit.";
    }

    private static String explainTachycardia(Patient patient, Vitals vitals) {
        AgeCategory category = patient != null ? patient.getAgeCategory() : null;
        int limit = tachycardiaLimit(category);
        if (vitals != null && category != null) {
            return String.format(
                    "Patient has tachycardia: pulse %d/min is above the %d/min limit for %s.",
                    vitals.getPulse(),
                    limit,
                    formatAgeCategory(category));
        }
        return "Patient has tachycardia: pulse is above the age-specific limit.";
    }

    private static String explainHypoxemia(Patient patient, Vitals vitals) {
        AgeCategory category = patient != null ? patient.getAgeCategory() : null;
        int limit = hypoxemiaLimit(category);
        if (vitals != null && category != null) {
            return String.format(
                    "Patient has hypoxemia: SpO2 %d%% is below the %d%% limit for %s.",
                    vitals.getSpo2(),
                    limit,
                    formatAgeCategory(category));
        }
        return "Patient has hypoxemia: SpO2 is below the age-specific limit.";
    }

    private static String explainHypotension(Patient patient, Vitals vitals) {
        AgeCategory category = patient != null ? patient.getAgeCategory() : null;
        int limit = hypotensionLimit(category);
        if (vitals != null && category != null) {
            return String.format(
                    "Patient has hypotension: systolic BP %d mmHg is below the %d mmHg limit for %s.",
                    vitals.getSystolicBloodPressure(),
                    limit,
                    formatAgeCategory(category));
        }
        return "Patient has hypotension: systolic blood pressure is below the age-specific limit.";
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

    private static int hypoxemiaLimit(AgeCategory category) {
        if (category == AgeCategory.CHILD) {
            return 95;
        }
        if (category == AgeCategory.SENIOR) {
            return 92;
        }
        return 94;
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
            return "their age group";
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
