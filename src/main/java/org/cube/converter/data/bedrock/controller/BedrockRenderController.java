package org.cube.converter.data.bedrock.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@ToString
public final class BedrockRenderController {
    private final String identifier;
    private final List<String> textureIndex;
    private final List<String> geometryIndex;
    private final Map<String, List<String>> arrays;
}
