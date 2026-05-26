package com.ftn.service.engine;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

public class RuleTraceAgendaEventListener extends DefaultAgendaEventListener {
    private final List<String> firedRules = new ArrayList<>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        firedRules.add(event.getMatch().getRule().getName());
    }

    public List<String> getFiredRules() {
        return firedRules;
    }
}
