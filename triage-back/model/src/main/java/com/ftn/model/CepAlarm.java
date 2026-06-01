package com.ftn.model;

import java.io.Serializable;
import java.util.Objects;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Role.Type;

@Role(Type.FACT)
public class CepAlarm implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ruleName;
    private Priority priority;
    private String message;

    public CepAlarm() {
    }

    public CepAlarm(String ruleName, Priority priority, String message) {
        this.ruleName = ruleName;
        this.priority = priority;
        this.message = message;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, priority, ruleName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CepAlarm other = (CepAlarm) obj;
        return Objects.equals(message, other.message)
                && priority == other.priority
                && Objects.equals(ruleName, other.ruleName);
    }

    @Override
    public String toString() {
        return "CepAlarm [ruleName=" + ruleName + ", priority=" + priority + ", message=" + message + "]";
    }
}
