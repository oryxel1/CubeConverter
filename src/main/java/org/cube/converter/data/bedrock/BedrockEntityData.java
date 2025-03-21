package org.cube.converter.data.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.GsonUtil.*;

@RequiredArgsConstructor
@ToString
@Getter
public class BedrockEntityData {
    private final String identifier;
    private final Scripts scripts;
    private final List<RenderController> controllers;
    private final Map<String, String> materials;
    private final Map<String, String> animations;
    private final Map<String, String> textures;
    private final Map<String, String> geometries;

    public record RenderController(String identifier, String condition) {
        public static List<RenderController> parse(final JsonArray array) {
            if (array == null) {
                return List.of();
            }

            final List<RenderController> list = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    for (final String elementName : element.getAsJsonObject().keySet()) {
                        final JsonElement element1 = element.getAsJsonObject().get(elementName);
                        if (!element1.isJsonPrimitive()) {
                            continue;
                        }

                        list.add(new RenderController(elementName, element1.getAsString()));
                    }
                } else {
                    list.add(new RenderController(element.getAsString(), ""));
                }
            }

            return list;
        }
    }

    public record Scripts(List<String> initialize, List<String> pre_animation, Scale scale, List<Animate> animates) {
        public record Scale(String scale, String scaleX, String scaleY, String scaleZ) {
        }

        // This name redirect to an identifier (animations)
        public record Animate(String name, String expression) {
            private static List<Animate> parse(final JsonArray array) {
                if (array == null) {
                    return List.of();
                }

                final List<Animate> animates = new ArrayList<>();

                for (final JsonElement element : array) {
                    if (!element.isJsonObject()) {
                        animates.add(new Animate(element.getAsString(), ""));
                        continue;
                    }

                    final JsonObject object = element.getAsJsonObject();
                    for (final String elementName : object.keySet()) {
                        animates.add(new Animate(elementName, object.get(elementName).getAsString()));
                    }
                }

                return animates;
            }
        }

        public static Scripts parse(final JsonObject object) {
            final List<String> initialize = arrayToList(object.getAsJsonArray("initialize"));
            final List<String> pre_animation = arrayToList(object.getAsJsonArray("pre_animation"));
            final Scale scale = new Scale(getOrDefault(object.get("scale"), "1"), getOrDefault(object.get("scaleX"), "1"), getOrDefault(object.get("scaleY"), "1"), getOrDefault(object.get("scaleZ"), "1"));
            final List<Animate> animates = Animate.parse(object.getAsJsonArray("animate"));

            return new Scripts(initialize, pre_animation, scale, animates);
        }

        public static Scripts emptyScript() {
            return new Scripts(List.of(), List.of(), new Scale("1", "1", "1", "1"), List.of());
        }
    }
}
