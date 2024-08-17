package org.oryxel.cube.parser;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import org.oryxel.cube.model.bedrock.EntityModelData;

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

    public static EntityModelData deserialize(String json) {
        return deserialize(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static EntityModelData deserialize(JsonObject json) {
        if (!json.has("minecraft:client_entity"))
            return null;

        JsonObject clientEntity = json.getAsJsonObject("minecraft:client_entity");
        JsonObject description = clientEntity.getAsJsonObject("description");
        String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        String material = getFirstValue(description.getAsJsonObject("materials")).getAsString();
        String texture = getFirstValue(description.getAsJsonObject("textures")).getAsString();
        String geometry = getFirstValue(description.getAsJsonObject("geometry")).getAsString();

        EntityModelData entityModelData = new EntityModelData(identifier, material, texture, geometry);
        return entityModelData;
    }

    private static JsonElement getFirstValue(JsonObject object) {
        for (Map.Entry<String, JsonElement> object1 : object.entrySet()) {
            return object1.getValue();
        }

        return null;
    }

}
