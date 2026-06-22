package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

public class SepsisSuspectedAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean suspected;
    private String message;

    public SepsisSuspectedAnswer() {
    }

    public SepsisSuspectedAnswer(boolean suspected, String message) {
        this.suspected = suspected;
        this.message = message;
    }

    public boolean isSuspected() {
        return suspected;
    }

    public void setSuspected(boolean suspected) {
        this.suspected = suspected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, suspected);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SepsisSuspectedAnswer other = (SepsisSuspectedAnswer) obj;
        return suspected == other.suspected && Objects.equals(message, other.message);
    }

    @Override
    public String toString() {
        return "SepsisSuspectedAnswer [suspected=" + suspected + ", message=" + message + "]";
    }
}
