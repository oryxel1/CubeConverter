package org.cube.converter.parser.bedrock.controller;

import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.parser.bedrock.data.impl.BedrockAttachableParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedrockAttachableRenderingTest {
    @Test
    void preservesVisibilityOverrideOrderIncludingRepeatedPatterns() {
        final var controller = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.worn":{
                "part_visibility":[{"*":false},{"Wing*":"query.is_alive","body":true},
                    null,{}, {"invalid":{},"Wing*":0},{"*":"!query.is_invisible"}],
                "arrays":{"textures":{"Array.texture":["Texture.default"]}}
            }}}
            """).get(0);

        assertEquals(List.of(
            new BedrockRenderController.BoneVisibility("*", "false"),
            new BedrockRenderController.BoneVisibility("Wing*", "query.is_alive"),
            new BedrockRenderController.BoneVisibility("body", "true"),
            new BedrockRenderController.BoneVisibility("Wing*", "0"),
            new BedrockRenderController.BoneVisibility("*", "!query.is_invisible")
        ), controller.partVisibility());
        assertEquals(1, controller.textures().size());
        assertThrows(UnsupportedOperationException.class, () -> controller.partVisibility().clear());
    }

    @Test
    void retainsVisibilityWithoutArraysAndDefaultsMalformedContainers() {
        final var visible = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.worn":{"part_visibility":[{"*":1}]}}}
            """).get(0);
        assertEquals(1, visible.partVisibility().size());
        final var absent = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.worn":{}}}
            """).get(0);
        final var malformed = BedrockControllerParser.parse("""
            {"render_controllers":{"controller.render.worn":{"part_visibility":{"*":true}}}}
            """).get(0);
        assertTrue(absent.partVisibility().isEmpty());
        assertTrue(malformed.partVisibility().isEmpty());
    }

    @Test
    void retainsScalarParentSetupSeparatelyFromOwnerAndAnimationScripts() {
        final var attachable = BedrockAttachableParser.parse("""
            {"minecraft:attachable":{"description":{"identifier":"test:worn",
                "scripts":{"parent_setup":"variable.hide_cape = 1;",
                    "initialize":["variable.phase = 0;"],
                    "pre_animation":["variable.phase = query.life_time;"],"animate":["idle"]}
            }}}
            """);
        assertEquals(List.of("variable.hide_cape = 1;"), attachable.getScripts().parentSetup());
        assertEquals(1, attachable.getScripts().initialize().size());
        assertEquals(1, attachable.getScripts().pre_animation().size());
        assertEquals(1, attachable.getScripts().animates().size());
    }

    @Test
    void retainsValidParentSetupNeighborsInOrderAndDefaultsAbsentScripts() {
        final var attachable = BedrockAttachableParser.parse("""
            {"minecraft:attachable":{"description":{"identifier":"test:worn",
                "scripts":{"parent_setup":["variable.first = 1;",null,4,{},
                    "variable.second = variable.first;"]}
            }}}
            """);
        assertEquals(List.of("variable.first = 1;", "variable.second = variable.first;"),
            attachable.getScripts().parentSetup());
        assertThrows(UnsupportedOperationException.class, () -> attachable.getScripts().parentSetup().clear());
        final var absent = BedrockAttachableParser.parse("""
            {"minecraft:attachable":{"description":{"identifier":"test:worn"}}}
            """);
        final var malformed = BedrockAttachableParser.parse("""
            {"minecraft:attachable":{"description":{"identifier":"test:worn", "scripts":{"parent_setup":false}}}}
            """);
        assertTrue(absent.getScripts().parentSetup().isEmpty());
        assertTrue(malformed.getScripts().parentSetup().isEmpty());
    }

    @Test
    void recordsDefensivelyCopyNewDeclarationsAndKeepExistingConstructors() {
        final var visibility = new ArrayList<>(List.of(new BedrockRenderController.BoneVisibility("*", "1")));
        final var controller = new BedrockRenderController("controller.render.worn", Map.of(), "", List.of(),
            List.of(), List.of(), List.of(), false, "1", BedrockRenderController.UvAnimation.IDENTITY, visibility);
        visibility.clear();
        assertEquals(1, controller.partVisibility().size());

        final var parentSetup = new ArrayList<>(List.of("variable.worn = 1;"));
        final var scale = new BedrockEntityData.Scripts.Scale("1", "1", "1", "1");
        final var scripts = new BedrockEntityData.Scripts(List.of(), List.of(), scale, List.of(), parentSetup);
        parentSetup.clear();
        assertEquals(1, scripts.parentSetup().size());

        assertTrue(new BedrockEntityData.Scripts(List.of(), List.of(), scale, List.of()).parentSetup().isEmpty());
        assertTrue(new BedrockRenderController("controller.render.worn", Map.of(), "", List.of(),
            List.of(), List.of(), List.of(), false, "1", BedrockRenderController.UvAnimation.IDENTITY)
            .partVisibility().isEmpty());

        final var legacyController = new BedrockRenderController("controller.render.worn", Map.of(), "", List.of(),
            List.of(), List.of(), List.of());
        assertFalse(legacyController.ignoreLighting());
        assertEquals(1F, Float.parseFloat(legacyController.lightColorMultiplierExpression()));
        assertEquals(BedrockRenderController.UvAnimation.IDENTITY, legacyController.uvAnimation());
        assertTrue(legacyController.partVisibility().isEmpty());
    }
}
