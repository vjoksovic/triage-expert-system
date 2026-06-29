package com.ftn.service.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ftn.model.Patient;
import com.ftn.model.Vitals;
import com.ftn.service.dto.TriageRequestDto;

@SpringBootTest
class BackwardChainingServiceTest {

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private BackwardChainingService backwardChainingService;

    @Test
    void proveQueryIsRegisteredInKieBase() {
        KieSession session = kieContainer.newKieSession("triageKSession");
        try {
            session.insert(new Patient("Test", 40, Collections.emptySet()));
            session.insert(new Vitals(39.0, 90, 60, 110, 98, LocalDateTime.now()));
            assertDoesNotThrow(() -> {
                QueryResults results = session.getQueryResults("prove", "hasFever");
                assertNotNull(results);
            });
        } finally {
            session.dispose();
        }
    }

    @Test
    void querySepsisSuspectedDoesNotThrow() {
        TriageRequestDto request = new TriageRequestDto();
        request.setFullName("Test Patient");
        request.setAge(40);
        request.setTemperature(39.5);
        request.setSystolicBloodPressure(85);
        request.setDiastolicBloodPressure(55);
        request.setPulse(120);
        request.setSpo2(96);

        assertDoesNotThrow(() -> backwardChainingService.querySepsisSuspected(request));
    }
}
