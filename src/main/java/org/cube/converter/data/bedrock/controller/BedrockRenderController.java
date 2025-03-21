package org.cube.converter.data.bedrock.controller;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.GsonUtil.*;

// TODO: hurt color, overlay color, etc....
public record BedrockRenderController(String identifier, Map<String, String> materialsMap,
                                      String geometryExpression, List<String> textureExpressions,
                                      List<Array> materials, List<Array> textures, List<Array> geometries) {
    public record Array(String name, List<String> values) {
        public static List<Array> parse(final JsonObject object) {
            if (object == null) {
                return List.of();
            }

            final List<Array> list = new ArrayList<>();
            for (final String arrayKey : object.keySet()) {
                final JsonElement element = object.get(arrayKey);
                if (!element.isJsonArray()) {
                    continue;
                }

                list.add(new BedrockRenderController.Array(arrayKey, arrayToList(element.getAsJsonArray())));
            }

            return list;
        }
    }
}
