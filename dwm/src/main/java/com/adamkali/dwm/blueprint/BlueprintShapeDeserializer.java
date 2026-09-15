package com.adamkali.dwm.blueprint;

import com.adamkali.dwm.blueprint.model.BlueprintBlock;
import com.adamkali.dwm.blueprint.model.BlueprintLocation;
import com.adamkali.dwm.blueprint.model.BlueprintShape;
import com.adamkali.dwm.blueprint.model.CircleShape;
import com.adamkali.dwm.blueprint.model.DomeShape;
import com.adamkali.dwm.blueprint.model.PolygonShape;
import com.adamkali.dwm.blueprint.model.RectangleShape;
import com.adamkali.dwm.blueprint.model.SphereShape;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 * Deserializes a shape object using the {@code type} discriminator.
 */
final class BlueprintShapeDeserializer implements JsonDeserializer<BlueprintShape> {
    @Override
    public BlueprintShape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Shape must be a JSON object");
        }
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("type") || !obj.get("type").isJsonPrimitive()) {
            throw new JsonParseException("Shape missing required string field 'type'");
        }
        String type = obj.get("type").getAsString();
        BlueprintBlock block = context.deserialize(require(obj, "block"), BlueprintBlock.class);
        return switch (type) {
            case "circle" -> new CircleShape(
                    context.deserialize(require(obj, "origin"), BlueprintLocation.class),
                    block,
                    require(obj, "filled").getAsBoolean(),
                    require(obj, "radius").getAsDouble()
            );
            case "rectangle" -> new RectangleShape(
                    context.deserialize(require(obj, "from"), BlueprintLocation.class),
                    context.deserialize(require(obj, "to"), BlueprintLocation.class),
                    block,
                    require(obj, "fill").getAsBoolean()
            );
            case "sphere" -> new SphereShape(
                    context.deserialize(require(obj, "origin"), BlueprintLocation.class),
                    block,
                    require(obj, "fill").getAsBoolean(),
                    require(obj, "radius").getAsDouble()
            );
            case "polygon" -> {
                int nSides = require(obj, "n_sides").getAsInt();
                if (nSides < 5 || nSides > 12) {
                    throw new JsonParseException("n_sides must be between 5 and 12, got " + nSides);
                }
                yield new PolygonShape(
                        nSides,
                        context.deserialize(require(obj, "origin"), BlueprintLocation.class),
                        block,
                        require(obj, "fill").getAsBoolean(),
                        require(obj, "radius").getAsDouble()
                );
            }
            case "dome" -> new DomeShape(
                    context.deserialize(require(obj, "origin"), BlueprintLocation.class),
                    block,
                    require(obj, "fill").getAsBoolean(),
                    require(obj, "radius").getAsDouble()
            );
            default -> throw new JsonParseException("Unknown shape type: " + type);
        };
    }

    private static JsonElement require(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            throw new JsonParseException("Shape missing required field '" + key + "'");
        }
        return obj.get(key);
    }
}
