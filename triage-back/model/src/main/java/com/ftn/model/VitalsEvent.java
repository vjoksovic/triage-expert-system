package com.ftn.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Role.Type;
import org.kie.api.definition.type.Timestamp;

@Role(Type.EVENT)
@Timestamp("measuredAtEpochMillis")
@Expires("15m")
public class VitalsEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private double temperature;
    private int systolicBloodPressure;
    private int diastolicBloodPressure;
    private int pulse;
    private int spo2;
    private LocalDateTime measuredAt;
    private long measuredAtEpochMillis;

    public VitalsEvent() {
    }

    public VitalsEvent(double temperature, int systolicBloodPressure, int diastolicBloodPressure, int pulse, int spo2,
            LocalDateTime measuredAt) {
        this.temperature = temperature;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
        this.pulse = pulse;
        this.spo2 = spo2;
        setMeasuredAt(measuredAt);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getSystolicBloodPressure() {
        return systolicBloodPressure;
    }

    public void setSystolicBloodPressure(int systolicBloodPressure) {
        this.systolicBloodPressure = systolicBloodPressure;
    }

    public int getDiastolicBloodPressure() {
        return diastolicBloodPressure;
    }

    public void setDiastolicBloodPressure(int diastolicBloodPressure) {
        this.diastolicBloodPressure = diastolicBloodPressure;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public int getSpo2() {
        return spo2;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
        this.measuredAtEpochMillis = toEpochMillis(measuredAt);
    }

    public long getMeasuredAtEpochMillis() {
        return measuredAtEpochMillis;
    }

    public void setMeasuredAtEpochMillis(long measuredAtEpochMillis) {
        this.measuredAtEpochMillis = measuredAtEpochMillis;
    }

    private static long toEpochMillis(LocalDateTime measuredAt) {
        if (measuredAt == null) {
            return System.currentTimeMillis();
        }
        return measuredAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    public int hashCode() {
        return Objects.hash(diastolicBloodPressure, measuredAtEpochMillis, pulse, spo2, systolicBloodPressure,
                temperature);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VitalsEvent other = (VitalsEvent) obj;
        return Double.doubleToLongBits(temperature) == Double.doubleToLongBits(other.temperature)
                && systolicBloodPressure == other.systolicBloodPressure
                && diastolicBloodPressure == other.diastolicBloodPressure
                && pulse == other.pulse
                && spo2 == other.spo2
                && measuredAtEpochMillis == other.measuredAtEpochMillis;
    }
}
