package org.cube.converter.parser.bedrock.data.impl;

import com.google.gson.JsonObject;
import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.parser.bedrock.data.BedrockDataParser;
public class BedrockEntityParser {
    public static BedrockEntityData parse(final String json) {
        return BedrockDataParser.parseEntity(json);
    }

    public static BedrockEntityData parse(final JsonObject json) {
        return BedrockDataParser.parseEntity(json.toString());
    }
}
