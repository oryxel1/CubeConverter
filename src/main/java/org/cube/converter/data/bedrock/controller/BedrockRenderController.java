package org.cube.converter.data.bedrock.controller;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.FastJson2Util.*;

// TODO: hurt color, overlay color, etc....
public record BedrockRenderController(String identifier, Map<String, String> materialsMap,
                                      String geometryExpression, List<String> textureExpressions,
                                      List<Array> materials, List<Array> textures, List<Array> geometries) {
    public record Array(String name, List<String> values) {
        public static List<Array> parse(final JSONObject object) {
            if (object == null) {
                return List.of();
            }

            final List<Array> list = new ArrayList<>();
            for (final String arrayKey : object.keySet()) {
                final Object element = object.get(arrayKey);
                if (!(element instanceof JSONArray array)) {
                    continue;
                }

                list.add(new BedrockRenderController.Array(arrayKey, arrayToList(array)));
            }

            return list;
        }
    }
}
