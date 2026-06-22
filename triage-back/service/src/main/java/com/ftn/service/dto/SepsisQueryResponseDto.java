package com.ftn.service.dto;

public class SepsisQueryResponseDto {
    private String question;
    private boolean suspected;
    private String summary;
    private BackwardChainingNodeDto reasoningTree;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isSuspected() {
        return suspected;
    }

    public void setSuspected(boolean suspected) {
        this.suspected = suspected;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public BackwardChainingNodeDto getReasoningTree() {
        return reasoningTree;
    }

    public void setReasoningTree(BackwardChainingNodeDto reasoningTree) {
        this.reasoningTree = reasoningTree;
    }
}
