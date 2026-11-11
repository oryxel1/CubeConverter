package org.cube.converter.parser.bedrock.controller;

import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.parser.bedrock.data.impl.BedrockEntityParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedrockLegacyAnimationControllerTest {
    @Test
    void preservesLegacyDeclarationsInOrderIndependentlyOfModernScripts() {
        final var entity = BedrockEntityParser.parse("""
            {"format_version":"1.8.0","minecraft:client_entity":{"description":{
                "identifier":"test:mob","animations":{"face":"animation.test.face"},
                "animation_controllers":[{"first":"controller.animation.test.first"},
                    null,{"invalid":4,"second":"controller.animation.test.second"}],
                "scripts":{"animate":["face"]}
            }}}
            """);
        assertEquals(List.of(new BedrockEntityData.AnimationController("first", "controller.animation.test.first"),
            new BedrockEntityData.AnimationController("second", "controller.animation.test.second")), entity.getAnimationControllers());
        assertEquals(1, entity.getScripts().animates().size());
        assertThrows(UnsupportedOperationException.class, () -> entity.getAnimationControllers().clear());
    }

    @Test
    void defaultsWithoutInventingImplicitControllers() {
        final var entity = BedrockEntityParser.parse("""
            {"minecraft:client_entity":{"description":{"identifier":"test:mob"}}}
            """);
        assertTrue(entity.getAnimationControllers().isEmpty());
        assertTrue(entity.getScripts().animates().isEmpty());
    }
}
