package org.cube.converter.parser.bedrock.geometry;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.util.element.Position2V;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.element.UVMap;

import java.util.ArrayList;
import java.util.List;

// I am NOT cleaning this up lmao...
public class BedrockGeometryParser {
    public static List<BedrockGeometryModel> parse(String json) {
        return parse(JSON.parseObject(json.trim()));
    }

    public static List<BedrockGeometryModel> parse(JSONObject json) {
        final List<BedrockGeometryModel> geometries = new ArrayList<>();

        if (json.containsKey("minecraft:geometry")) {
            final Object element = json.get("minecraft:geometry");
            if (!(element instanceof JSONArray array)) {
                return null;
            }

            for (Object other : array) {
                if (!(other instanceof JSONObject object)) {
                    continue;
                }

                final BedrockGeometryModel geometry = getGeometry(object, "minecraft:geometry", "texture_width", "texture_height");
                if (geometry == null) {
                    continue;
                }
                geometries.add(geometry);
            }
        }

        for (String elementName : json.keySet()) {
            Object element = json.get(elementName);
            if (!(element instanceof JSONObject object)) {
                continue;
            }
            if (!object.containsKey("texturewidth")) {
                continue;
            }
            final BedrockGeometryModel geometry = getGeometry(object, elementName, "texturewidth", "textureheight");
            if (geometry == null) {
                continue;
            }
            geometries.add(geometry);
        }

        return geometries;
    }

    private static BedrockGeometryModel getGeometry(JSONObject geometry, String elementName, String textureWidthName, String textureHeightName) {
        JSONObject description = geometry.containsKey("description") ? geometry.getJSONObject("description") : geometry;
        String identifier = description.containsKey("identifier") ? description.getString("identifier") : elementName;

        if (!description.containsKey(textureHeightName) || !description.containsKey(textureWidthName)) {
            return null;
        }

        int textureWidth = description.getInteger(textureWidthName);
        int textureHeight = description.getInteger(textureHeightName);

        if (!geometry.containsKey("bones")) {
            return null;
        }

        final List<Parent> bones = new ArrayList<>();
        for (Object boneElement : geometry.getJSONArray("bones")) {
            final JSONObject boneObject = (JSONObject) boneElement;
            final String name = boneObject.getString("name");

            final Position3V parentPivot = new Position3V(boneObject.getJSONArray("pivot"));
            final Position3V boneRotation = new Position3V(boneObject.getJSONArray("rotation"));

            // parentPivot.setX(-parentPivot.getX());
            // boneRotation.setX(-boneRotation.getX());
            // boneRotation.setY(-boneRotation.getY());

            Parent bone = new Parent(name, parentPivot, boneRotation);

            if (boneObject.containsKey("parent")) {
                bone.setParent(boneObject.getString("parent"));
            }

            if (!boneObject.containsKey("cubes")) {
                bones.add(bone);
                continue;
            }

            int i = 0;
            final JSONArray cubeElements = boneObject.getJSONArray("cubes");
            for (Object cubeElement : cubeElements) {
                JSONObject cubeObject = (JSONObject) cubeElement;
                final Position3V position = new Position3V(cubeObject.getJSONArray("origin"));
                final Position3V size = new Position3V(cubeObject.getJSONArray("size"));
                final Position3V pivot = new Position3V(cubeObject.getJSONArray("pivot"));
                final Position3V rotation = new Position3V(cubeObject.getJSONArray("rotation"));
                // pivot.setX(-pivot.getX());

                // rotation.setX(-rotation.getX());
                // rotation.setY(-rotation.getY());

                boolean mirror = false;
                if (cubeObject.containsKey("mirror")) {
                    mirror = cubeObject.getBoolean("mirror");
                }

                Cube cube;
                if (cubeObject.get("uv") instanceof JSONArray array) {
                    Float[] offset = new Float[] { array.getFloat(0), array.getFloat(1) };
                    cube = new Cube(pivot, position, size, rotation, mirror, UVMap.fromBoxUV(size, offset, mirror));
                } else {
                    cube = new Cube(pivot, position, size, rotation, mirror, UVMap.fromPerfaceUV(cubeObject));
                }

                cube.setParent(bone.getParent());
                if (cubeObject.containsKey("inflate")) {
                    cube.setInflate(cubeObject.getFloat("inflate"));
                }

                bone.getCubes().put(i, cube);
                i++;
            }

            bones.add(bone);
        }

        final BedrockGeometryModel geometryModel = new BedrockGeometryModel(identifier, new Position2V(textureWidth, textureHeight));
        geometryModel.getParents().addAll(bones);
        return geometryModel;
    }
}