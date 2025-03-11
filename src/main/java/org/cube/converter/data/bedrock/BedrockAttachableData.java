package org.cube.converter.data.bedrock;

import java.util.List;
import java.util.Map;

public class BedrockAttachableData extends BedrockEntityData {
    public BedrockAttachableData(String identifier, List<RenderController> controllers, Map<String, String> textures, Map<String, String> geometries, List<String> variables) {
        super(identifier, controllers, textures, geometries, variables);
    }
}
