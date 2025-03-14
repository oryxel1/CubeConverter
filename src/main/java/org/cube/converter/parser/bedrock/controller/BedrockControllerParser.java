package org.cube.converter.parser.bedrock.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.util.GsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            List<String> texturePaths = new ArrayList<>();
            if (object.has("textures")) {
                texturePaths = arrayToList(object.getAsJsonArray("textures"));
            }

            String geometryPath = "";
            if (object.has("geometry")) {
                JsonElement element = object.get("geometry");
                if (element.isJsonPrimitive()) {
                    geometryPath = element.getAsString();
                }
            }

            final JsonObject arrays = object.getAsJsonObject("arrays");
            if (arrays == null) {
                list.add(new BedrockRenderController(identifier, geometryPath, texturePaths, List.of(), null));
                continue;
            }

            final List<BedrockRenderController.Array> textures = new ArrayList<>();

            if (arrays.has("textures")) {
                final JsonObject textureArray = arrays.getAsJsonObject("textures");
                for (final String textureArrayKey : textureArray.keySet()) {
                    final JsonElement textureElement = textureArray.get(textureArrayKey);
                    if (!textureElement.isJsonArray()) {
                        continue;
                    }

                    textures.add(new BedrockRenderController.Array(textureArrayKey, arrayToList(textureElement.getAsJsonArray())));
                }
            }

            final List<BedrockRenderController.Array> geometries = new ArrayList<>();

            if (arrays.has("geometries")) {
                final JsonObject geometriesArray = arrays.getAsJsonObject("geometries");

                for (final String geometryArrayKey : geometriesArray.keySet()) {
                    final JsonElement geometryElement = geometriesArray.get(geometryArrayKey);
                    if (!geometryElement.isJsonArray()) {
                        continue;
                    }

                    geometries.add(new BedrockRenderController.Array(geometryArrayKey, arrayToList(geometryElement.getAsJsonArray())));
                }
            }

            list.add(new BedrockRenderController(identifier, geometryPath, texturePaths, textures, geometries));
        }

        return list;
    }

    private static List<String> arrayToList(JsonArray array) {
        List<String> list = new ArrayList<>();

        for (JsonElement element : array) {
            list.add(element.getAsString());
        }

        return list;
    }
}
