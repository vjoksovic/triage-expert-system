package com.ftn.service.dto;

import java.util.ArrayList;
import java.util.List;

public class DepartmentLoadResponseDto {
    private int p1Count;
    private int overloadThreshold;
    private boolean overloaded;
    private List<DepartmentCaseDto> activeP1Cases = new ArrayList<>();

    public int getP1Count() {
        return p1Count;
    }

    public void setP1Count(int p1Count) {
        this.p1Count = p1Count;
    }

    public int getOverloadThreshold() {
        return overloadThreshold;
    }

    public void setOverloadThreshold(int overloadThreshold) {
        this.overloadThreshold = overloadThreshold;
    }

    public boolean isOverloaded() {
        return overloaded;
    }

    public void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    public List<DepartmentCaseDto> getActiveP1Cases() {
        return activeP1Cases;
    }

    public void setActiveP1Cases(List<DepartmentCaseDto> activeP1Cases) {
        this.activeP1Cases = activeP1Cases;
    }
}
