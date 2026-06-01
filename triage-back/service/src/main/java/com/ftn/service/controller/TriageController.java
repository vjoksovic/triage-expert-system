package com.ftn.service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.CepMonitorRequestDto;
import com.ftn.service.dto.CepMonitorResponseDto;
import com.ftn.service.dto.TriageRequestDto;
import com.ftn.service.dto.TriageResponseDto;
import com.ftn.service.service.CepMonitoringService;
import com.ftn.service.service.TriageEngineService;

@RestController
@RequestMapping("/api/triage")
@CrossOrigin(origins = "http://localhost:4200")
public class TriageController {
    private final TriageEngineService triageEngineService;
    private final CepMonitoringService cepMonitoringService;

    public TriageController(TriageEngineService triageEngineService, CepMonitoringService cepMonitoringService) {
        this.triageEngineService = triageEngineService;
        this.cepMonitoringService = cepMonitoringService;
    }

    @PostMapping("/evaluate")
    public TriageResponseDto evaluate(@RequestBody TriageRequestDto request) {
        return triageEngineService.evaluate(request);
    }

    @PostMapping("/cep/monitor")
    public CepMonitorResponseDto monitorVitalsStream(@RequestBody CepMonitorRequestDto request) {
        return cepMonitoringService.monitorSpo2Trend(request);
    }
}
