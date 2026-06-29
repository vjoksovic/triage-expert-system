package com.ftn.service.engine;

import java.util.List;
import java.util.stream.Collectors;

import com.ftn.model.AgeGroup;
import com.ftn.model.Patient;
import com.ftn.model.VitalSignThresholds;
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
            case "Classify preterm patient":
            case "Classify newborn patient":
            case "Classify infant patient":
            case "Classify toddler patient":
            case "Classify preschool patient":
            case "Classify school-age patient":
            case "Classify adolescent patient":
                return explainAgeClassification(patient);
            case "Detect fever":
                return explainFever(patient, vitals);
            case "Detect tachycardia in preterm infant":
            case "Detect tachycardia in newborn":
            case "Detect tachycardia in infant":
            case "Detect tachycardia in toddler":
            case "Detect tachycardia in preschool child":
            case "Detect tachycardia in school-age child":
            case "Detect tachycardia in adolescent":
                return explainTachycardia(patient, vitals);
            case "Detect hypotension":
                return explainHypotension(patient, vitals);
            case "Detect hypoxemia":
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
        AgeGroup group = VitalSignThresholds.resolveGroup(patient);
        return String.format(
                "Patient is classified as %s (age %d).",
                VitalSignThresholds.formatGroup(group),
                patient.getAge());
    }

    private static String explainFever(Patient patient, Vitals vitals) {
        AgeGroup group = VitalSignThresholds.resolveGroup(patient);
        double limit = VitalSignThresholds.feverThreshold(group);
        if (vitals != null && patient != null) {
            return String.format(
                    "Patient has fever: temperature %.1f C is above the %.1f C limit for %s.",
                    vitals.getTemperature(),
                    limit,
                    VitalSignThresholds.formatGroup(group));
        }
        return "Patient has fever: temperature is above the age-specific limit.";
    }

    private static String explainTachycardia(Patient patient, Vitals vitals) {
        AgeGroup group = VitalSignThresholds.resolveGroup(patient);
        int limit = VitalSignThresholds.tachycardiaThreshold(group);
        if (vitals != null && patient != null) {
            return String.format(
                    "Patient has tachycardia: pulse %d/min is above the %d/min limit for %s.",
                    vitals.getPulse(),
                    limit,
                    VitalSignThresholds.formatGroup(group));
        }
        return "Patient has tachycardia: pulse is above the age-specific limit.";
    }

    private static String explainHypoxemia(Patient patient, Vitals vitals) {
        AgeGroup group = VitalSignThresholds.resolveGroup(patient);
        int limit = VitalSignThresholds.hypoxemiaThreshold(group);
        if (vitals != null && patient != null) {
            return String.format(
                    "Patient has hypoxemia: SpO2 %d%% is below the %d%% limit for %s.",
                    vitals.getSpo2(),
                    limit,
                    VitalSignThresholds.formatGroup(group));
        }
        return "Patient has hypoxemia: SpO2 is below the age-specific limit.";
    }

    private static String explainHypotension(Patient patient, Vitals vitals) {
        AgeGroup group = VitalSignThresholds.resolveGroup(patient);
        int limit = VitalSignThresholds.hypotensionThreshold(group);
        if (vitals != null && patient != null) {
            return String.format(
                    "Patient has hypotension: systolic BP %d mmHg is below the %d mmHg limit for %s.",
                    vitals.getSystolicBloodPressure(),
                    limit,
                    VitalSignThresholds.formatGroup(group));
        }
        return "Patient has hypotension: systolic blood pressure is below the age-specific limit.";
    }
}
