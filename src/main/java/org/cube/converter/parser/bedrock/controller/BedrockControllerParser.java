package org.cube.converter.parser.bedrock.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.FastJson2Util.objectToMap;

public class BedrockControllerParser {
    public static List<BedrockRenderController> parse(final String json) {
        return parse(JSON.parseObject(json.trim()));
    }

    public static List<BedrockRenderController> parse(final JSONObject json) {
        if (!json.containsKey("render_controllers")) {
            return new ArrayList<>();
        }

        final List<BedrockRenderController> list = new ArrayList<>();
        final JSONObject controllers = json.getJSONObject("render_controllers");
        for (final String identifier : controllers.keySet()) {
            if (!identifier.startsWith("controller.render")) {
                continue;
            }

            if (!(controllers.get(identifier) instanceof JSONObject object)) {
                continue;
            }

            List<String> textureExpressions = new ArrayList<>();
            if (object.containsKey("textures")) {
                textureExpressions = arrayToList(object.getJSONArray("textures"));
            }

            String geometryExpression = "";
            if (object.containsKey("geometry")) {
                String element = object.getString("geometry");
                if (element != null) {
                    geometryExpression = element;
                }
            }

            final Map<String, String> materialsMap = new HashMap<>();
            if (object.containsKey("materials")) {
                for (final Object element : object.getJSONArray("materials")) {
                    if (!(element instanceof JSONObject jo)) {
                        continue;
                    }
                    materialsMap.putAll(objectToMap(jo));
                }
            }

            final JSONObject arrays = object.getJSONObject("arrays");
            if (arrays == null) {
                list.add(new BedrockRenderController(identifier, materialsMap, geometryExpression, textureExpressions, List.of(), List.of(), List.of()));
                continue;
            }

            final List<BedrockRenderController.Array> textures = BedrockRenderController.Array.parse(arrays.getJSONObject("textures"));
            final List<BedrockRenderController.Array> geometries = BedrockRenderController.Array.parse(arrays.getJSONObject("geometries"));
            final List<BedrockRenderController.Array> materials = BedrockRenderController.Array.parse(arrays.getJSONObject("materials"));

            list.add(new BedrockRenderController(identifier, materialsMap, geometryExpression, textureExpressions, materials, textures, geometries));
        }

        return list;
    }

    private static List<String> arrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();

        for (Object element : array) {
            list.add(element.toString());
        }

        return list;
    }
}
