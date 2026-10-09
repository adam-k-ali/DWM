package com.adamkali.dwm.model.json.anim;

import com.adamkali.dwm.MinecraftTestBootstrap;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionTest {
    @BeforeAll
    static void bootstrap() {
        MinecraftTestBootstrap.ensure();
    }

    private static float eval(String source, float... values) {
        return Expression.parse(source).compile(List.of("a", "b")).eval(values);
    }

    @Test
    void respectsPrecedenceAndParentheses() {
        assertEquals(7.0F, eval("1 + 2 * 3"));
        assertEquals(9.0F, eval("(1 + 2) * 3"));
        assertEquals(-5.0F, eval("1 - 2 * 3"));
        assertEquals(4.0F, eval("8 / 2 / 1"));
        assertEquals(1.0F, eval("8 / 4 / 2"));
    }

    @Test
    void unaryMinusBindsTighterThanBinary() {
        assertEquals(-6.0F, eval("-2 * 3"));
        assertEquals(5.0F, eval("1 - -4"));
        assertEquals(-3.0F, eval("-a", 3.0F, 0.0F));
    }

    @Test
    void readsVariablesByIndex() {
        assertEquals(11.0F, eval("a + b * 2", 3.0F, 4.0F));
    }

    @Test
    void constantsAndTrigMatchJavaFormulas() {
        assertEquals((float) Math.PI, eval("pi"));
        assertEquals((float) (180.0 / Math.PI), eval("deg"));
        float pos = 1.7F;
        float speed = 0.6F;
        float expected = Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed;
        assertEquals(expected, eval("cos(a * 0.6662 + pi) * 1.4 * b", pos, speed), 1.0e-6F);
    }

    @Test
    void helperFunctions() {
        assertEquals(3.0F, eval("abs(-3)"));
        assertEquals(1.0F, eval("min(1, 2)"));
        assertEquals(2.0F, eval("max(1, 2)"));
        assertEquals(5.0F, eval("clamp(9, 0, 5)"));
        assertEquals(0.0F, eval("clamp(-9, 0, 5)"));
        assertEquals(15.0F, eval("lerp(10, 20, 0.5)"));
    }

    @Test
    void reportsVariablesInFirstUseOrder() {
        assertEquals(List.of("b", "a"), List.copyOf(Expression.parse("b * a + b").variables()));
    }

    @Test
    void rejectsMalformedExpressions() {
        for (String bad : new String[]{"", "1 +", "(1", "1 )", "2 $ 3", "foo(1)", "cos()", "min(1)", "1..2"}) {
            assertThrows(IllegalArgumentException.class, () -> Expression.parse(bad), bad);
        }
    }

    @Test
    void unknownVariableNamesAvailableOnes() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Expression.parse("nope + 1").compile(List.of("a", "b")));
        assertTrue(error.getMessage().contains("nope") && error.getMessage().contains("[a, b]"));
    }

    @Test
    void codecRoundTripsSource() {
        Expression expression = Expression.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
                com.google.gson.JsonParser.parseString("\"a * 2\"")).getOrThrow();
        assertEquals("a * 2", expression.source());
        assertTrue(Expression.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
                com.google.gson.JsonParser.parseString("\"a *\"")).error().isPresent());
    }
}
