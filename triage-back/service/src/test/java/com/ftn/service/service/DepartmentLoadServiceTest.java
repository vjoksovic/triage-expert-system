package com.ftn.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ftn.model.Priority;
import com.ftn.model.Ward;
import com.ftn.service.dto.TriageRequestDto;

@SpringBootTest
class DepartmentLoadServiceTest {

    @Autowired
    private TriageEngineService triageEngineService;

    @Autowired
    private DepartmentLoadService departmentLoadService;

    @BeforeEach
    void resetDepartment() {
        departmentLoadService.getDepartmentLoad().getActiveP1Cases()
                .forEach(caseEntry -> departmentLoadService.discharge(caseEntry.getCaseId()));
    }

    @Test
    void doesNotDoubleCountSamePatientName() {
        for (int index = 1; index <= 5; index++) {
            triageEngineService.evaluate(p1Request("case-" + index, "John Smith"));
        }

        assertEquals(1, departmentLoadService.getP1Count());

        var response = triageEngineService.evaluate(p1Request("case-6", "Jane Doe"));

        assertEquals(Ward.JIL.name(), response.getWard());
        assertEquals(2, departmentLoadService.getP1Count());
    }

    @Test
    void redirectsSixthP1PatientWhenDepartmentAlreadyHasFive() {
        for (int index = 1; index <= 5; index++) {
            triageEngineService.evaluate(p1Request("case-" + index, "Patient " + index));
        }

        assertEquals(5, departmentLoadService.getP1Count());

        var response = triageEngineService.evaluate(p1Request("case-6", "Patient 6"));

        assertEquals("P1", response.getPriority());
        assertEquals(Ward.GENERAL_ER.name(), response.getWard());
        assertTrue(response.isRedirectedToSecondary());
        assertEquals(Ward.JIL.name(), response.getOriginalWard());
        assertEquals(6, response.getDepartmentP1Count());
        assertTrue(response.isDepartmentOverloaded());
    }

    private TriageRequestDto p1Request(String caseId, String fullName) {
        TriageRequestDto request = new TriageRequestDto();
        request.setCaseId(caseId);
        request.setFullName(fullName);
        request.setAge(68);
        request.setTemperature(39.2);
        request.setSystolicBloodPressure(88);
        request.setDiastolicBloodPressure(54);
        request.setPulse(128);
        request.setSpo2(91);
        request.getChronicConditions().add("DIABETES");
        request.getSymptoms().add("CONFUSION");
        return request;
    }
}
