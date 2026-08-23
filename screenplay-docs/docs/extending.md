# Extending primitives

Built-in steps live in `screenplay-common`. Mods and libraries can add more
without forking Screenplay.

## Implement `ScenarioPrimitive`

```java
package com.example.mymod.screenplay;

import com.adamkali.screenplay.primitive.ScenarioPrimitive;
import com.adamkali.screenplay.primitive.ScenarioPrimitiveContext;

import java.util.Map;

public final class MyPrimitive implements ScenarioPrimitive {
    @Override
    public String name() {
        return "myStep";
    }

    @Override
    public void validate(Map<String, Object> args) {
        // Reject unknown / missing arguments before the client runs
    }

    @Override
    public boolean execute(ScenarioPrimitiveContext context, Map<String, Object> args) {
        // Return true when the step is complete; false to retry next tick
        return true;
    }
}
```

## Register via ServiceLoader

Add a provider file on the **client** classpath:

`META-INF/services/com.adamkali.screenplay.primitive.ScenarioPrimitive`

```text
com.example.mymod.screenplay.MyPrimitive
```

Screenplay loads built-ins first, then `ServiceLoader` extras. Duplicate names
from different classes fail at registry load.

## Guidance

- Prefer shared primitives upstream in `screenplay-common` when the step is
  generally useful.
- Keep mod-only steps in your mod and register them with ServiceLoader.
- Validate arguments strictly so malformed YAML fails before the client boots.
- Steps should be idempotent across ticks: return `false` while waiting, `true`
  when done, and throw for hard failures.
