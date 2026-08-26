package com.adamkali.screenplay;

import java.util.List;

public record SuitePlan(
        String id,
        String name,
        List<ScenarioPlan.Step> beforeAll,
        List<ScenarioPlan.Step> beforeEach,
        List<ScenarioPlan.Step> afterEach,
        List<ScenarioPlan.Step> afterAll,
        List<ScenarioPlan> tests
) {
    public SuitePlan {
        beforeAll = List.copyOf(beforeAll);
        beforeEach = List.copyOf(beforeEach);
        afterEach = List.copyOf(afterEach);
        afterAll = List.copyOf(afterAll);
        tests = List.copyOf(tests);
    }
}
