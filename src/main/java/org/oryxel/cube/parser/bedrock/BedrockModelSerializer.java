package org.oryxel.cube.parser.bedrock;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import org.oryxel.cube.model.bedrock.BedrockModelData;
import org.oryxel.cube.model.bedrock.BedrockRenderController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2023-2024 Oryxel and contributors
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
public class BedrockModelSerializer {

    public static BedrockModelData deserialize(String json) {
        return deserialize(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static BedrockModelData deserialize(JsonObject json) {
        if (!json.has("minecraft:client_entity"))
            return null;

        JsonObject clientEntity = json.getAsJsonObject("minecraft:client_entity");
        JsonObject description = clientEntity.getAsJsonObject("description");
        String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        String material = getFirstValue(description.getAsJsonObject("materials")).getAsString();
        Map<String, String> texture = objectToMap(description.getAsJsonObject("textures"));
        Map<String, String> geometry = objectToMap(description.getAsJsonObject("geometry"));
        List<String> controllers = description.has("render_controllers") ?
                objectToString(description.getAsJsonArray("render_controllers")) : new ArrayList<>();

        BedrockModelData model = new BedrockModelData(identifier, material, controllers, texture, geometry);
        return model;
    }

    private static List<String> objectToString(JsonArray array) {
        List<String> strings = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonObject())
                continue; // TODO: actually proper implement.
            else strings.add(element.getAsString());
        }

        return strings;
    }

    private static Map<String, String> objectToMap(JsonObject object) {
        Map<String, String> map = new HashMap<>();
        for (String name : object.keySet()) {
            JsonElement element = object.get(name);

            map.put(name, element.getAsString());
        }

        return map;
    }

    private static JsonElement getFirstValue(JsonObject object) {
        for (Map.Entry<String, JsonElement> object1 : object.entrySet()) {
            return object1.getValue();
        }

        return null;
    }

}
