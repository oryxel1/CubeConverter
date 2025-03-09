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
