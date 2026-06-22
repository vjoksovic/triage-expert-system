package com.ftn.service.dto;

import java.util.ArrayList;
import java.util.List;

public class BackwardChainingNodeDto {
    private String query;
    private String label;
    private boolean result;
    private String explanation;
    private List<BackwardChainingNodeDto> children = new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<BackwardChainingNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<BackwardChainingNodeDto> children) {
        this.children = children;
    }
}
