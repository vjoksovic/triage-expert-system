package com.ftn.service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.CepMonitorRequestDto;
import com.ftn.service.dto.CepMonitorResponseDto;
import com.ftn.service.dto.DepartmentLoadResponseDto;
import com.ftn.service.dto.SepsisQueryResponseDto;
import com.ftn.service.dto.TriageRequestDto;
import com.ftn.service.dto.TriageResponseDto;
import com.ftn.service.service.BackwardChainingService;
import com.ftn.service.service.CepMonitoringService;
import com.ftn.service.service.DepartmentLoadService;
import com.ftn.service.service.TriageEngineService;

@RestController
@RequestMapping("/api/triage")
@CrossOrigin(origins = "http://localhost:4200")
public class TriageController {
    private final TriageEngineService triageEngineService;
    private final CepMonitoringService cepMonitoringService;
    private final BackwardChainingService backwardChainingService;
    private final DepartmentLoadService departmentLoadService;

    public TriageController(
            TriageEngineService triageEngineService,
            CepMonitoringService cepMonitoringService,
            BackwardChainingService backwardChainingService,
            DepartmentLoadService departmentLoadService) {
        this.triageEngineService = triageEngineService;
        this.cepMonitoringService = cepMonitoringService;
        this.backwardChainingService = backwardChainingService;
        this.departmentLoadService = departmentLoadService;
    }

    @PostMapping("/evaluate")
    public TriageResponseDto evaluate(@RequestBody TriageRequestDto request) {
        return triageEngineService.evaluate(request);
    }

    @PostMapping("/backward/sepsis")
    public SepsisQueryResponseDto querySepsisSuspected(@RequestBody TriageRequestDto request) {
        return backwardChainingService.querySepsisSuspected(request);
    }

    @PostMapping("/cep/monitor")
    public CepMonitorResponseDto monitorVitalsStream(@RequestBody CepMonitorRequestDto request) {
        return cepMonitoringService.monitorVitalsStream(request);
    }

    @GetMapping("/department/load")
    public DepartmentLoadResponseDto getDepartmentLoad() {
        return departmentLoadService.getDepartmentLoad();
    }

    @DeleteMapping("/department/cases/{caseId}")
    public DepartmentLoadResponseDto dischargeCase(@PathVariable String caseId) {
        departmentLoadService.discharge(caseId);
        return departmentLoadService.getDepartmentLoad();
    }
}
