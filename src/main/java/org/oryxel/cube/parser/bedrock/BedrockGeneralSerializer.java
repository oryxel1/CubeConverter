package org.oryxel.cube.parser.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.oryxel.cube.model.bedrock.BedrockGeneralData;
import org.oryxel.cube.util.GsonUtil;

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
public class BedrockGeneralSerializer {

    public static BedrockGeneralData deserialize(String json) {
        JsonElement element = GsonUtil.getGson().fromJson(json.trim(), JsonElement.class);
        if (!element.isJsonObject()) // Well this can happen sometimes a json can only have ["en_US"], or something like that, I have no idea.
            return null;

        return deserialize(element.getAsJsonObject());
    }

    public static BedrockGeneralData deserialize(JsonObject json) {
        if (!json.has("minecraft:client_entity") && !json.has("minecraft:attachable"))
            return null;

        JsonObject object = null;
        if (json.has("minecraft:client_entity")) {
            object = json.getAsJsonObject("minecraft:client_entity");
        } else if (json.has("minecraft:attachable")) {
            object = json.getAsJsonObject("minecraft:attachable");
        }
        System.out.println(json);

        JsonObject description = object.getAsJsonObject("description");
        String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        Map<String, String> texture = objectToMap(description.getAsJsonObject("textures"));
        Map<String, String> geometry = objectToMap(description.getAsJsonObject("geometry"));
        List<String> controllers = description.has("render_controllers") ?
                renderController(description.getAsJsonArray("render_controllers")) : new ArrayList<>();
        List<String> variables = new ArrayList<>();
        if (description.has("scripts")) {
            JsonObject scripts = description.getAsJsonObject("scripts");
            if (scripts.has("initialize"))
                variables = objectToString(scripts.getAsJsonArray("initialize"));
        }

        BedrockGeneralData model = new BedrockGeneralData(identifier, controllers, texture, geometry, variables);

        return model;
    }

    private static List<String> objectToString(JsonArray array) {
        List<String> strings = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonObject())
                continue;
            else strings.add(element.getAsString());
        }

        return strings;
    }

    private static List<String> renderController(JsonArray array) {
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

}
