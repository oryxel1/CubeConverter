package org.cube.converter.parser.bedrock.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.util.GsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (!json.has("minecraft:attachable")) {
            return null;
        }

        final JsonObject description = json.getAsJsonObject("minecraft:attachable").getAsJsonObject("description");
        final String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        final Map<String, String> textures = convertJsonToMap(description.getAsJsonObject("textures"));
        final Map<String, String> geometries = convertJsonToMap(description.getAsJsonObject("geometry"));
        final List<BedrockEntityData.RenderController> controllers = description.has("render_controllers") ? parseRenderController(description.getAsJsonArray("render_controllers")) : new ArrayList<>();
        List<String> variables = new ArrayList<>();
        if (description.has("scripts")) {
            final JsonObject scripts = description.getAsJsonObject("scripts");
            if (scripts.has("initialize")) {
                variables = convertJsonToList(scripts.getAsJsonArray("initialize"));
            }
        }

        return new BedrockAttachableData(identifier, controllers, textures, geometries, variables);
    }

    private static BedrockEntityData parseEntity(final JsonObject json) {
        if (!json.has("minecraft:client_entity")) {
            return null;
        }

        final JsonObject description = json.getAsJsonObject("minecraft:client_entity").getAsJsonObject("description");
        final String identifier = description.getAsJsonPrimitive("identifier").getAsString();
        final Map<String, String> textures = convertJsonToMap(description.getAsJsonObject("textures"));
        final Map<String, String> geometries = convertJsonToMap(description.getAsJsonObject("geometry"));
        final List<BedrockEntityData.RenderController> controllers = description.has("render_controllers") ? parseRenderController(description.getAsJsonArray("render_controllers")) : new ArrayList<>();
        List<String> variables = new ArrayList<>();
        if (description.has("scripts")) {
            final JsonObject scripts = description.getAsJsonObject("scripts");
            if (scripts.has("initialize")) {
                variables = convertJsonToList(scripts.getAsJsonArray("initialize"));
            }
        }

        return new BedrockEntityData(identifier, controllers, textures, geometries, variables);
    }

    private static List<BedrockEntityData.RenderController> parseRenderController(final JsonArray array) {
        List<BedrockEntityData.RenderController> list = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                for (final String elementName : element.getAsJsonObject().keySet()) {
                    final JsonElement element1 = element.getAsJsonObject().get(elementName);
                    if (!element1.isJsonPrimitive()) {
                        continue;
                    }

                    list.add(new BedrockEntityData.RenderController(elementName, element1.getAsString()));
                }
            } else {
                list.add(new BedrockEntityData.RenderController(element.getAsString(), ""));
            }
        }

        return list;
    }

    private static List<String> convertJsonToList(final JsonArray array) {
        List<String> strings = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                strings.add(element.getAsString());
            }
        }

        return strings;
    }

    private static Map<String, String> convertJsonToMap(final JsonObject object) {
        Map<String, String> map = new HashMap<>();
        for (String name : object.keySet()) {
            JsonElement element = object.get(name);

            map.put(name, element.getAsString());
        }

        return map;
    }
}
