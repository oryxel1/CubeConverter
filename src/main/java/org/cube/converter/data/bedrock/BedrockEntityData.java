package org.cube.converter.data.bedrock;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cube.converter.util.FastJson2Util.*;

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
        public static List<RenderController> parse(final JSONArray array) {
            if (array == null) {
                return List.of();
            }

            final List<RenderController> list = new ArrayList<>();
            for (Object element : array) {
                if (element instanceof JSONObject object) {
                    for (final String elementName : object.keySet()) {
                        final Object element1 = object.get(elementName);
                        if (!(element1 instanceof String string)) {
                            continue;
                        }

                        list.add(new RenderController(elementName, string));
                    }
                } else {
                    list.add(new RenderController(element.toString(), ""));
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
            private static List<Animate> parse(final JSONArray array) {
                if (array == null) {
                    return List.of();
                }

                final List<Animate> animates = new ArrayList<>();

                for (final Object element : array) {
                    if (element instanceof String s) {
                        animates.add(new Animate(s, ""));
                        continue;
                    }
                    if (!(element instanceof JSONObject object)) {
                        continue;
                    }

                    for (final String elementName : object.keySet()) {
                        animates.add(new Animate(elementName, object.getString(elementName)));
                    }
                }

                return animates;
            }
        }

        public static Scripts parse(final JSONObject object) {
            final List<String> initialize = arrayToList(object.getJSONArray("initialize"));
            final List<String> pre_animation = arrayToList(object.getJSONArray("pre_animation"));
            final Scale scale = new Scale(getOrDefault(object.get("scale"), "1"), getOrDefault(object.get("scaleX"), "1"), getOrDefault(object.get("scaleY"), "1"), getOrDefault(object.get("scaleZ"), "1"));
            final List<Animate> animates = Animate.parse(object.getJSONArray("animate"));

            return new Scripts(initialize, pre_animation, scale, animates);
        }

        public static Scripts emptyScript() {
            return new Scripts(List.of(), List.of(), new Scale("1", "1", "1", "1"), List.of());
        }
    }
}
