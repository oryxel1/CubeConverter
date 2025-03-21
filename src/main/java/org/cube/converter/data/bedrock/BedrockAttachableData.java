package org.cube.converter.data.bedrock;

import java.util.List;
import java.util.Map;

public class BedrockAttachableData extends BedrockEntityData {
    public BedrockAttachableData(String identifier, Scripts scripts, List<RenderController> controllers, Map<String, String> materials, Map<String, String> animations, Map<String, String> textures, Map<String, String> geometries) {
        super(identifier, scripts, controllers, materials, animations, textures, geometries);
    }
}
