package org.cube.converter.parser.bedrock.controller;

import org.junit.jupiter.api.Test;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BedrockControllerLightingTest {
    @Test
    void retainsLightingExpressionsAndMaterialOverrideOrderWithArrays() {
        final var controller = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.logo":{
                "geometry":"Geometry.default","textures":["Texture.default"],
                "materials":[{"*":"Material.default"},{"Light*":"Array.materials[query.variant]"}],
                "ignore_lighting":true,"light_color_multiplier":"query.variant * 0.5",
                "arrays":{"materials":{"Array.materials":["Material.normal","Material.glow"]}}
            }}}
            """).get(0);
        assertTrue(controller.ignoreLighting());
        assertEquals("query.variant * 0.5", controller.lightColorMultiplierExpression());
        assertEquals(List.of("*", "Light*"), List.copyOf(controller.materialsMap().keySet()));
        assertEquals(2, controller.materials().get(0).values().size());
    }

    @Test
    void defaultsAndNumericMultipliersDoNotRequireArrays() {
        final var defaults = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.default":{}}}
            """).get(0);
        assertFalse(defaults.ignoreLighting());
        assertEquals(1F, Float.parseFloat(defaults.lightColorMultiplierExpression()));
        final var dimmed = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.default":{"light_color_multiplier":0.4}}}
            """).get(0);
        assertEquals(0.4F, Float.parseFloat(dimmed.lightColorMultiplierExpression()));
    }
    @Test
    void retainsIndependentUvComponentsAndIdentityDefaults() {
        final var explicit = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.uv":{
                "uv_anim":{"offset":[-0.25,{}],"scale":[false,2.5]}
            }}}
            """).get(0).uvAnimation();
        assertEquals(-0.25F, Float.parseFloat(explicit.offsetU()));
        assertEquals(0F, Float.parseFloat(explicit.offsetV()));
        assertEquals(1F, Float.parseFloat(explicit.scaleU()));
        assertEquals(2.5F, Float.parseFloat(explicit.scaleV()));
        final var missing = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.default":{}}}
            """).get(0).uvAnimation();
        assertEquals(BedrockRenderController.UvAnimation.IDENTITY, missing);
        final var malformed = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.default":{
                "uv_anim":{"offset":[0.4],"scale":null}
            }}}
            """).get(0).uvAnimation();
        assertEquals(BedrockRenderController.UvAnimation.IDENTITY, malformed);
    }

}
