package org.cube.converter.parser.bedrock.data.impl;

import com.google.gson.JsonObject;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.parser.bedrock.data.BedrockDataParser;
public class BedrockAttachableParser {
    public static BedrockAttachableData parse(final String json) {
        return BedrockDataParser.parseAttachable(json);
    }

    public static BedrockAttachableData parse(final JsonObject json) {
        return BedrockDataParser.parseAttachable(json.toString());
    }
}
