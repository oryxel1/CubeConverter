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

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2025-2026 Oryxel and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

            final Map<String, List<String>> arrays = new HashMap<>();

            final JsonObject object = controllers.getAsJsonObject(identifier);
            final JsonObject arrayJson = object.getAsJsonObject("arrays");

            List<String> textureIndex = new ArrayList<>(), geometryIndex = new ArrayList<>();
            if (object.has("textures")) {
                textureIndex = getListFromJson(object.getAsJsonArray("textures"));
            }

            if (object.has("geometry")) {
                JsonElement element = object.get("geometry");
                if (element.isJsonPrimitive()) {
                    geometryIndex.add(element.getAsString());
                } else {
                    geometryIndex = getListFromJson(element.getAsJsonArray());
                }
            }

            if (arrayJson != null) {
                for (final String arrayObject : arrayJson.keySet()) {
                    final JsonElement element = arrayJson.get(arrayObject);
                    if (!element.isJsonObject()) {
                        continue;
                    }

                    for (final String elementObject : element.getAsJsonObject().keySet()) {
                        final List<String> arrayObjectList = new ArrayList<>();
                        final JsonElement element1 = element.getAsJsonObject().get(elementObject);
                        if (!element1.isJsonArray()) {
                            continue;
                        }

                        for (final JsonElement element2 : element1.getAsJsonArray()) {
                            arrayObjectList.add(element2.getAsString());
                        }

                        if (!arrayObjectList.isEmpty()) {
                            arrays.put(elementObject, arrayObjectList);
                        }
                    }
                }
            }

            list.add(new BedrockRenderController(identifier, textureIndex, geometryIndex, arrays));
        }

        return list;
    }

    private static List<String> getListFromJson(JsonArray array) {
        List<String> list = new ArrayList<>();

        for (JsonElement element : array) {
            list.add(element.getAsString());
        }

        return list;
    }
}
