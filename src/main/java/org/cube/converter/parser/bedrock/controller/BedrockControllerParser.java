package org.cube.converter.parser.bedrock.controller;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.util.GsonUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.GsonUtil.objectToMap;

public class BedrockControllerParser {
    public static List<BedrockRenderController> parse(final String json) {
        return parse(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static List<BedrockRenderController> parse(final JsonObject json) {
        if (!json.has("render_controllers")) {
            return new ArrayList<>();
        }

        final List<BedrockRenderController> list = new ArrayList<>();
        final JsonObject controllers = json.getAsJsonObject("render_controllers");
        for (final String identifier : controllers.keySet()) {
            if (!controllers.get(identifier).isJsonObject() || !identifier.startsWith("controller.render")) {
                continue;
            }

            final JsonObject object = controllers.getAsJsonObject(identifier);

            List<String> textureExpressions = new ArrayList<>();
            if (object.has("textures")) {
                textureExpressions = arrayToList(object.getAsJsonArray("textures"));
            }

            String geometryExpression = "";
            if (object.has("geometry")) {
                JsonElement element = object.get("geometry");
                if (element.isJsonPrimitive()) {
                    geometryExpression = element.getAsString();
                }
            }

            final Map<String, String> materialsMap = new LinkedHashMap<>();
            if (object.has("materials")) {
                for (final JsonElement element : object.getAsJsonArray("materials")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    materialsMap.putAll(objectToMap(element.getAsJsonObject()));
                }
            }

            final boolean ignoreLighting = object.has("ignore_lighting") && object.get("ignore_lighting").getAsBoolean();
            final String lightColorMultiplier = object.has("light_color_multiplier") ? object.get("light_color_multiplier").getAsString() : "1.0";
            final BedrockRenderController.UvAnimation uvAnimation = uvAnimation(object.get("uv_anim"));
            final List<BedrockRenderController.BoneVisibility> partVisibility = partVisibility(object.get("part_visibility"));
            final JsonObject arrays = object.getAsJsonObject("arrays");
            if (arrays == null) {
                list.add(new BedrockRenderController(identifier, materialsMap, geometryExpression, textureExpressions, List.of(), List.of(), List.of(), ignoreLighting, lightColorMultiplier, uvAnimation, partVisibility));
                continue;
            }

            final List<BedrockRenderController.Array> textures = BedrockRenderController.Array.parse(arrays.getAsJsonObject("textures"));
            final List<BedrockRenderController.Array> geometries = BedrockRenderController.Array.parse(arrays.getAsJsonObject("geometries"));
            final List<BedrockRenderController.Array> materials = BedrockRenderController.Array.parse(arrays.getAsJsonObject("materials"));

            list.add(new BedrockRenderController(identifier, materialsMap, geometryExpression, textureExpressions, materials, textures, geometries, ignoreLighting, lightColorMultiplier, uvAnimation, partVisibility));
        }

        return list;
    }

    /** Parses ordered visibility declarations without selecting a renderer or variable scope. */
    public static List<BedrockRenderController.BoneVisibility> partVisibility(final JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }
        final List<BedrockRenderController.BoneVisibility> rules = new ArrayList<>();
        for (final JsonElement rule : element.getAsJsonArray()) {
            if (!rule.isJsonObject()) {
                continue;
            }
            for (final var entry : rule.getAsJsonObject().entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    rules.add(new BedrockRenderController.BoneVisibility(entry.getKey(), entry.getValue().getAsString()));
                }
            }
        }
        return List.copyOf(rules);
    }

    private static BedrockRenderController.UvAnimation uvAnimation(final JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return BedrockRenderController.UvAnimation.IDENTITY;
        }
        final JsonObject fields = element.getAsJsonObject();
        return new BedrockRenderController.UvAnimation(
            uvComponent(fields.get("offset"), 0, "0"), uvComponent(fields.get("offset"), 1, "0"),
            uvComponent(fields.get("scale"), 0, "1"), uvComponent(fields.get("scale"), 1, "1"));
    }

    private static String uvComponent(final JsonElement element, final int axis, final String fallback) {
        if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() != 2) {
            return fallback;
        }
        final JsonElement component = element.getAsJsonArray().get(axis);
        if (!component.isJsonPrimitive()) {
            return fallback;
        }
        final var value = component.getAsJsonPrimitive();
        return value.isString() || value.isNumber() ? value.getAsString() : fallback;
    }

    private static List<String> arrayToList(JsonArray array) {
        List<String> list = new ArrayList<>();

        for (JsonElement element : array) {
            list.add(element.getAsString());
        }

        return list;
    }
}
