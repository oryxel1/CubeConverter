package org.cube.converter.parser.bedrock.data.impl;

import com.alibaba.fastjson2.JSONObject;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.parser.bedrock.data.BedrockDataParser;
public class BedrockAttachableParser {
    public static BedrockAttachableData parse(final String json) {
        return BedrockDataParser.parseAttachable(json);
    }

    public static BedrockAttachableData parse(final JSONObject json) {
        return BedrockDataParser.parseAttachable(json.toString());
    }
}
