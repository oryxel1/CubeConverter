package org.cube.converter.parser.bedrock.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.util.GsonUtil;

import java.util.List;
import java.util.Map;

import static org.cube.converter.util.GsonUtil.*;

@RequiredArgsConstructor
public class BedrockDataParser {
    public static BedrockEntityData parseEntity(final String json) {
        JsonElement element = GsonUtil.getGson().fromJson(json.trim(), JsonElement.class);
        if (!element.isJsonObject()) // Well this can happen sometimes a json can only have ["en_US"], or something like that, I have no idea.
            return null;

        return parseEntity(element.getAsJsonObject());
    }

    public static BedrockAttachableData parseAttachable(final String json) {
        JsonElement element = GsonUtil.getGson().fromJson(json.trim(), JsonElement.class);
        if (!element.isJsonObject()) // Well this can happen sometimes a json can only have ["en_US"], or something like that, I have no idea.
            return null;

        return parseAttachable(element.getAsJsonObject());
    }

    private static BedrockAttachableData parseAttachable(final JsonObject json) {
        return (BedrockAttachableData) parse(json, "minecraft:attachable", true);
    }

    private static BedrockEntityData parseEntity(final JsonObject json) {
        return parse(json, "minecraft:client_entity", false);
    }

    private static BedrockEntityData parse(final JsonObject json, final String identifierName, boolean attachable) {
        if (!json.has(identifierName)) {
            return null;
        }

        final JsonObject description = json.getAsJsonObject(identifierName).getAsJsonObject("description");
        final String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        final Map<String, String> materials = objectToMap(description.getAsJsonObject("materials"));
        final Map<String, String> textures = objectToMap(description.getAsJsonObject("textures"));
        final Map<String, String> geometries = objectToMap(description.getAsJsonObject("geometry"));
        final Map<String, String> animations = objectToMap(description.getAsJsonObject("animations"));
        final List<BedrockEntityData.RenderController> controllers = BedrockEntityData.RenderController.parse(description.getAsJsonArray("render_controllers"));
        final BedrockEntityData.Scripts scripts;
        if (description.has("scripts")) {
            scripts = BedrockEntityData.Scripts.parse(description.getAsJsonObject("scripts"));
        } else {
            scripts = BedrockEntityData.Scripts.emptyScript();
        }

        return attachable ? new BedrockAttachableData(identifier, scripts, controllers, materials, animations, textures, geometries) :
                new BedrockEntityData(identifier, scripts, controllers, materials, animations, textures, geometries);
    }
}
