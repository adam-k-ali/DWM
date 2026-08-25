package com.adamkali.dwm.screenplay;

import com.adamkali.dwm.guide.FieldGuideScreens;
import com.adamkali.screenplay.ScenarioException;
import com.adamkali.screenplay.primitive.ScenarioPrimitive;
import com.adamkali.screenplay.primitive.ScenarioPrimitiveContext;

import java.util.Map;

public final class OpenFieldGuidePrimitive implements ScenarioPrimitive {
    @Override
    public String name() {
        return "openFieldGuide";
    }

    @Override
    public Map<String, Object> validate(Map<String, Object> arguments, String source) {
        if (arguments.isEmpty()) {
            return arguments;
        }
        if (arguments.size() != 1 || !(arguments.get("via") instanceof String via)) {
            throw new ScenarioException(source + ": openFieldGuide accepts optional via: direct|pause");
        }
        if (!"direct".equals(via) && !"pause".equals(via)) {
            throw new ScenarioException(source + ": openFieldGuide via must be 'direct' or 'pause'");
        }
        return arguments;
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context) {
        if (context.client().player == null) {
            return false;
        }
        Object via = context.arguments().get("via");
        if ("pause".equals(via)) {
            FieldGuideScreens.openViaPauseMenu(context.client());
        } else {
            FieldGuideScreens.openDirect(context.client());
        }
        return true;
    }
}
