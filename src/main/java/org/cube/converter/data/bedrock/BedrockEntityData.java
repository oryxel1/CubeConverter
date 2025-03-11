package org.cube.converter.data.bedrock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@ToString
@Getter
public class BedrockEntityData {
    private final String identifier;
    private final List<RenderController> controllers;
    private final Map<String, String> textures;
    private final Map<String, String> geometries;
    private final List<String> variables;

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class RenderController {
        private final String identifier;
        private final String condition;
    }
}
