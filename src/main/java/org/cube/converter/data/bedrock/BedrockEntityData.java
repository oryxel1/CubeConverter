package org.cube.converter.data.bedrock;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
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
    private final List<AnimationController> animationControllers;

    public BedrockEntityData(String identifier, Scripts scripts, List<RenderController> controllers,
                             Map<String, String> materials, Map<String, String> animations,
                             Map<String, String> textures, Map<String, String> geometries) {
        this(identifier, scripts, controllers, materials, animations, textures, geometries, List.of());
    }

    public record AnimationController(String alias, String identifier) {
        public static List<AnimationController> parse(final JsonArray array) {
            if (array == null) {
                return List.of();
            }
            final List<AnimationController> controllers = new ArrayList<>();
            for (final JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }
                for (final var entry : element.getAsJsonObject().entrySet()) {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                        controllers.add(new AnimationController(entry.getKey(), entry.getValue().getAsString()));
                    }
                }
            }
            return List.copyOf(controllers);
        }
    }

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

    public record Scripts(List<String> initialize, List<String> pre_animation, Scale scale, List<Animate> animates,
                          List<String> parentSetup) {
        public Scripts {
            parentSetup = List.copyOf(parentSetup);
        }

        public Scripts(List<String> initialize, List<String> pre_animation, Scale scale, List<Animate> animates) {
            this(initialize, pre_animation, scale, animates, List.of());
        }

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

            return new Scripts(initialize, pre_animation, scale, animates, parentSetup(object.get("parent_setup")));
        }

        private static List<String> parentSetup(final JsonElement element) {
            if (element == null) {
                return List.of();
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return List.of(element.getAsString());
            }
            if (!element.isJsonArray()) {
                return List.of();
            }
            final List<String> expressions = new ArrayList<>();
            for (final JsonElement expression : element.getAsJsonArray()) {
                if (expression.isJsonPrimitive() && expression.getAsJsonPrimitive().isString()) {
                    expressions.add(expression.getAsString());
                }
            }
            return List.copyOf(expressions);
        }

        public static Scripts emptyScript() {
            return new Scripts(List.of(), List.of(), new Scale("1", "1", "1", "1"), List.of());
        }
    }
}
