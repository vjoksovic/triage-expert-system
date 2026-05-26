package com.ftn.service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.service.dto.TriageRequestDto;
import com.ftn.service.dto.TriageResponseDto;
import com.ftn.service.service.TriageEngineService;

@RestController
@RequestMapping("/api/triage")
@CrossOrigin(origins = "http://localhost:4200")
public class TriageController {
    private final TriageEngineService triageEngineService;

    public TriageController(TriageEngineService triageEngineService) {
        this.triageEngineService = triageEngineService;
    }

    @PostMapping("/evaluate")
    public TriageResponseDto evaluate(@RequestBody TriageRequestDto request) {
        return triageEngineService.evaluate(request);
    }
}
