package org.cube.converter.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FastJson2Util {
    private FastJson2Util() {}

    public static List<String> arrayToList(final JSONArray array) {
        if (array == null) {
            return List.of();
        }

        final List<String> strings = new ArrayList<>();
        for (Object element : array) {
            if (!(element instanceof JSONObject)) {
                strings.add(element.toString());
            }
        }

        return strings;
    }

    public static Map<String, String> objectToMap(final JSONObject object) {
        if (object == null) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>();
        for (String name : object.keySet()) {
            map.put(name, object.getString(name));
        }

        return map;
    }

    public static String getOrDefault(final Object element, final String defaultValue) {
        return element == null ? defaultValue : element.toString();
    }
}
