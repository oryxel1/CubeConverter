package org.cube.converter.data.bedrock.controller;


import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.cube.converter.util.GsonUtil.*;

// TODO: hurt color, overlay color, etc....
public record BedrockRenderController(String identifier, List<MaterialAssignment> materialAssignments,
                                      String geometryExpression, List<String> textureExpressions,
                                      List<Array> materials, List<Array> textures, List<Array> geometries,
                                      boolean ignoreLighting, String lightColorMultiplierExpression, UvAnimation uvAnimation,
                                      List<BoneVisibility> partVisibility) {
    public BedrockRenderController {
        materialAssignments = List.copyOf(materialAssignments);
        partVisibility = List.copyOf(partVisibility);
    }

    public BedrockRenderController(String identifier, List<MaterialAssignment> materialAssignments,
                                   String geometryExpression, List<String> textureExpressions,
                                   List<Array> materials, List<Array> textures, List<Array> geometries,
                                   boolean ignoreLighting, String lightColorMultiplierExpression, UvAnimation uvAnimation) {
        this(identifier, materialAssignments, geometryExpression, textureExpressions, materials, textures, geometries,
            ignoreLighting, lightColorMultiplierExpression, uvAnimation, List.of());
    }

    public BedrockRenderController(String identifier, List<MaterialAssignment> materialAssignments,
                                   String geometryExpression, List<String> textureExpressions,
                                   List<Array> materials, List<Array> textures, List<Array> geometries) {
        this(identifier, materialAssignments, geometryExpression, textureExpressions, materials, textures, geometries,
            false, "1.0", UvAnimation.IDENTITY, List.of());
    }

    public record MaterialAssignment(String pattern, String expression) {
    }

    public record BoneVisibility(String pattern, String expression) {
    }

    public record UvAnimation(String offsetU, String offsetV, String scaleU, String scaleV) {
        public static final UvAnimation IDENTITY = new UvAnimation("0", "0", "1", "1");
    }

    public record Array(String name, List<String> values) {
        public static List<Array> parse(final JsonObject object) {
            if (object == null) {
                return List.of();
            }

            final List<Array> list = new ArrayList<>();
            for (final String arrayKey : object.keySet()) {
                final JsonElement element = object.get(arrayKey);
                if (!element.isJsonArray()) {
                    continue;
                }

                list.add(new BedrockRenderController.Array(arrayKey, arrayToList(element.getAsJsonArray())));
            }

            return list;
        }
    }
}
