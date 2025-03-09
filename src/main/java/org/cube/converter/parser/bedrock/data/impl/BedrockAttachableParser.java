package org.cube.converter.parser.bedrock.data.impl;

import com.google.gson.JsonObject;
import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.parser.bedrock.data.BedrockDataParser;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2025-2026 Oryxel and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class BedrockAttachableParser {
    public static BedrockAttachableData parse(final String json) {
        return BedrockDataParser.parseAttachable(json);
    }

    public static BedrockAttachableData parse(final JsonObject json) {
        return BedrockDataParser.parseAttachable(json.toString());
    }
}
