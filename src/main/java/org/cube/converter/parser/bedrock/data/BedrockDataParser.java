package org.cube.converter.parser.bedrock.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.data.bedrock.BedrockEntityData;

import java.util.List;
import java.util.Map;

import static org.cube.converter.util.FastJson2Util.*;

@RequiredArgsConstructor
public class BedrockDataParser {
    public static BedrockEntityData parseEntity(final String json) {
        Object element = JSON.parse(json.trim());
        if (!(element instanceof JSONObject object)) { // Well this can happen sometimes a json can only have ["en_US"], or something like that, I have no idea.
            return null;
        }

        return parseEntity(object);
    }

    public static BedrockAttachableData parseAttachable(final String json) {
        Object element = JSON.parse(json.trim());
        if (!(element instanceof JSONObject object)) { // Well this can happen sometimes a json can only have ["en_US"], or something like that, I have no idea.
            return null;
        }

        return parseAttachable(object);
    }

    private static BedrockAttachableData parseAttachable(final JSONObject json) {
        return (BedrockAttachableData) parse(json, "minecraft:attachable", true);
    }

    private static BedrockEntityData parseEntity(final JSONObject json) {
        return parse(json, "minecraft:client_entity", false);
    }

    private static BedrockEntityData parse(final JSONObject json, final String identifierName, boolean attachable) {
        if (!json.containsKey(identifierName)) {
            return null;
        }

        final JSONObject description = json.getJSONObject(identifierName).getJSONObject("description");
        final String identifier = description.getString("identifier");
        final Map<String, String> materials = objectToMap(description.getJSONObject("materials"));
        final Map<String, String> textures = objectToMap(description.getJSONObject("textures"));
        final Map<String, String> geometries = objectToMap(description.getJSONObject("geometry"));
        final Map<String, String> animations = objectToMap(description.getJSONObject("animations"));
        final List<BedrockEntityData.RenderController> controllers = BedrockEntityData.RenderController.parse(description.getJSONArray("render_controllers"));
        final BedrockEntityData.Scripts scripts;
        if (description.containsKey("scripts")) {
            scripts = BedrockEntityData.Scripts.parse(description.getJSONObject("scripts"));
        } else {
            scripts = BedrockEntityData.Scripts.emptyScript();
        }

        return attachable ? new BedrockAttachableData(identifier, scripts, controllers, materials, animations, textures, geometries) :
                new BedrockEntityData(identifier, scripts, controllers, materials, animations, textures, geometries);
    }
}
