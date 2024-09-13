package org.oryxel.cube.parser.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.bedrock.model.Bone;
import org.oryxel.cube.model.bedrock.model.Cube;
import org.oryxel.cube.util.ArrayUtil;
import org.oryxel.cube.util.Direction;
import org.oryxel.cube.util.GsonUtil;

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
public class BedrockGeometrySerializer {

    public static List<BedrockGeometry> deserialize(String json) {
        return deserialize(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static List<BedrockGeometry> deserialize(JsonObject json) {
        final List<BedrockGeometry> geometries = new ArrayList<>();

        if (json.has("minecraft:geometry")) {
            JsonElement element = json.get("minecraft:geometry");
            if (!element.isJsonArray())
                return null;

            JsonArray array = element.getAsJsonArray();
            for (JsonElement element1 : array) {
                if (!element1.isJsonObject()) continue;
                JsonObject object = element1.getAsJsonObject();

                BedrockGeometry geometry = getEntityGeometry(object, "minecraft:geometry", "texture_width", "texture_height");
                if (geometry == null) continue;
                geometries.add(geometry);
            }
        }

        for (String elementName : json.keySet()) {
            JsonElement element = json.get(elementName);
            if (!element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            if (!object.has("texturewidth")) continue;
            BedrockGeometry geometry = getEntityGeometry(object, elementName, "texturewidth", "textureheight");
            if (geometry == null) continue;
            geometries.add(geometry);
        }

        return geometries;
    }

    private static BedrockGeometry getEntityGeometry(JsonObject geometry, String elementName, String textureWidthName, String textureHeightName) {
        JsonObject description = geometry.has("description") ? geometry.getAsJsonObject("description") : geometry;
        String identifier = description.has("identifier") ? description.get("identifier").getAsString() : elementName;

        if (!description.has(textureHeightName) || !description.has(textureWidthName))
            return null;

        int textureWidth = description.get(textureWidthName).getAsInt();
        int textureHeight = description.get(textureHeightName).getAsInt();

        if (!geometry.has("bones"))
            return null;

        final List<Bone> bones = new ArrayList<>();
        for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
            JsonObject boneObject = boneElement.getAsJsonObject();
            String name = boneObject.get("name").getAsString();

            double[] pivot = ArrayUtil.toArray(boneObject.getAsJsonArray("pivot"));
            double[] boneRotation = ArrayUtil.toArray(boneObject.getAsJsonArray("rotation"));

            Bone bone = new Bone(name, pivot, boneRotation);
            pivot[0] = -pivot[0];

            if (boneObject.has("parent"))
                bone.parent(boneObject.get("parent").getAsString());

            if (!boneObject.has("cubes")) {
                bones.add(bone);
                continue;
            }

            JsonArray cubeElements = boneObject.getAsJsonArray("cubes");
            for (JsonElement cubeElement : cubeElements) {
                JsonObject cubeObject = cubeElement.getAsJsonObject();
                double[] origin = ArrayUtil.toArray(cubeObject.getAsJsonArray("origin"));
                double[] size = ArrayUtil.toArray(cubeObject.getAsJsonArray("size"));
                double[] cubePivot = ArrayUtil.toArray(cubeObject.getAsJsonArray("pivot"));
                double[] rotation = ArrayUtil.toArray(cubeObject.getAsJsonArray("rotation"));
                cubePivot[0] = -cubePivot[0];

                Cube cube;
                if (cubeObject.get("uv") instanceof JsonArray) {
                    JsonArray array = cubeObject.getAsJsonArray("uv");
                    double[] uvArray = new double[] { array.get(0).getAsDouble(), array.get(1).getAsDouble() };
                    cube = new Cube.BoxCube(origin, size, cubePivot, rotation, uvArray);
                } else {
                    cube = new Cube.PerFaceCube(origin, size, cubePivot, rotation);
                    JsonObject uv = cubeObject.getAsJsonObject("uv");
                    putIfExist(Direction.NORTH, uv, (Cube.PerFaceCube) cube);
                    putIfExist(Direction.EAST, uv, (Cube.PerFaceCube) cube);
                    putIfExist(Direction.SOUTH, uv, (Cube.PerFaceCube) cube);
                    putIfExist(Direction.WEST, uv, (Cube.PerFaceCube) cube);
                    putIfExist(Direction.UP, uv, (Cube.PerFaceCube) cube);
                    putIfExist(Direction.DOWN, uv, (Cube.PerFaceCube) cube);
                }

                if (boneObject.has("parent"))
                    cube.parent(bone.parent());

                if (cubeObject.has("inflate"))
                    cube.inflate(cubeObject.get("inflate").getAsDouble());

                if (cubeObject.has("mirror"))
                    cube.mirror(cubeObject.get("mirror").getAsBoolean());

                bone.cubes().add(cube);
            }

            bones.add(bone);
        }

        BedrockGeometry bedrockGeometry = new BedrockGeometry(identifier, textureWidth, textureHeight);
        bedrockGeometry.bones().addAll(bones);

        return bedrockGeometry;
    }

    private static void putIfExist(Direction direction, JsonObject object, Cube.PerFaceCube cube) {
        String name = direction.name().toLowerCase();
        if (object.has(name)) {
            cube.uvMap().put(direction, getUvDirection(object.getAsJsonObject(name)));
        }
    }

    private static double[] getUvDirection(JsonObject object) {
        if (object == null)
            return new double[] { 0D, 0D, 0D };

        JsonArray arrayUv = object.getAsJsonArray("uv");
        JsonArray arrayUvSize = object.getAsJsonArray("uv_size");

        return new double[] { arrayUv.get(0).getAsDouble(), arrayUv.get(1).getAsDouble(), arrayUvSize.get(0).getAsDouble(), arrayUvSize.get(1).getAsDouble() };
    }

}
