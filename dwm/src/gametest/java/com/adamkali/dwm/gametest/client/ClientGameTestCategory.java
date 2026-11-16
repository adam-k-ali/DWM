package com.adamkali.dwm.gametest.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares which {@link GameTestCategory categories} a client GameTest belongs to. Required on every test. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClientGameTestCategory {
    GameTestCategory[] value();
}
