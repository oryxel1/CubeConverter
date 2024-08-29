package org.oryxel.cube.parser.bedrock;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import org.oryxel.cube.model.bedrock.BedrockRenderController;

import java.util.ArrayList;
import java.util.List;

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
public class BedrockControllerSerializer {

    public static BedrockRenderController deserialize(String json) {
        return deserialize(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static BedrockRenderController deserialize(JsonObject json) {
        if (!json.has("render_controllers")) {
            return null;
        }

        JsonObject controllers = json.getAsJsonObject("render_controllers");
        JsonObject object = null;
        for (String name : controllers.keySet()) {
            if (!controllers.get(name).isJsonObject())
                continue;

            if (name.startsWith("controller.render")) {
                object = controllers.getAsJsonObject(name);
                break;
            }
        }

        if (object == null) {
            return null;
        }

        List<String> textures = new ArrayList<>(), geometries = new ArrayList<>();

        JsonObject arrays = object.getAsJsonObject("arrays");
        if (arrays == null)
            return null;

        if (arrays.has("textures")) {
            JsonArray texturesObject = null;

            if (!arrays.get("textures").isJsonArray()) {
                JsonObject arraysTextures = arrays.getAsJsonObject("textures");
                for (String name : arraysTextures.keySet()) {
                    if (!arraysTextures.get(name).isJsonArray())
                        continue;

                    if (name.startsWith("Array.")) {
                        texturesObject = arraysTextures.getAsJsonArray(name);
                        break;
                    }
                }

                if (texturesObject != null) {
                    for (JsonElement texture : texturesObject) {
                        textures.add(texture.getAsString().replace("Texture.", ""));
                    }
                }
            }
        }

        if (arrays.has("geometries")) {
            if (!arrays.get("geometries").isJsonArray()) {
                JsonArray geoArray = null;
                JsonObject geoTextures = arrays.getAsJsonObject("geometries");
                for (String name : geoTextures.keySet()) {
                    if (!geoTextures.get(name).isJsonArray())
                        continue;

                    if (name.startsWith("Array.")) {
                        geoArray = geoTextures.getAsJsonArray(name.replace("Geometry.", ""));
                        break;
                    }
                }

                if (geoArray != null) {
                    for (JsonElement texture : geoArray) {
                        geometries.add(texture.getAsString());
                    }
                }
            }
        }

        return new BedrockRenderController(textures, geometries);
    }

}
