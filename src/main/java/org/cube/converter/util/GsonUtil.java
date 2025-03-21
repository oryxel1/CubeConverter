package org.cube.converter.util;

import com.google.gson.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GsonUtil {
    private GsonUtil() {}
    @Getter
    private final static Gson gson = new GsonBuilder().create();

    public static List<String> arrayToList(final JsonArray array) {
        if (array == null) {
            return List.of();
        }

        final List<String> strings = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                strings.add(element.getAsString());
            }
        }

        return strings;
    }

    public static Map<String, String> objectToMap(final JsonObject object) {
        if (object == null) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>();
        for (String name : object.keySet()) {
            JsonElement element = object.get(name);
            map.put(name, element.getAsString());
        }

        return map;
    }

    public static String getOrDefault(final JsonElement element, final String defaultValue) {
        return element == null ? defaultValue : element.getAsString();
    }
}
