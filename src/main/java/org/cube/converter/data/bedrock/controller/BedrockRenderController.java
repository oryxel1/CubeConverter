package org.cube.converter.data.bedrock.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@ToString
@Getter
public final class BedrockRenderController {
    private final String identifier;
    private final String geometryPath;
    private final List<String> texturePaths;
    private final List<Array> textures, geometries;

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class Array {
        private final String name;
        private final List<String> values;
    }
}
