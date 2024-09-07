/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2023-2024 Oryxel and contributors
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
package org.oryxel.cube.model.bedrock.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BedrockEntityData {

    private final String identifier;
    private final List<String> controllers;
    private final Map<String, String> textures;
    private final Map<String, String> geometries;
    private final List<String> variables;

    public BedrockEntityData(String identifier, List<String> controllers, Map<String, String> textures, Map<String, String> geometries, List<String> variables) {
        this.identifier = identifier;
        this.controllers = controllers;
        this.textures = textures;
        this.geometries = geometries;
        this.variables = variables;
    }

    public String identifier() {
        return identifier;
    }

    public List<String> controllers() {
        return controllers;
    }

    public Map<String, String> textures() {
        return textures;
    }

    public Map<String, String> geometries() {
        return geometries;
    }

    public List<String> variables() {
        return variables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BedrockEntityData) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.controllers, that.controllers) &&
                Objects.equals(this.textures, that.textures) &&
                Objects.equals(this.geometries, that.geometries) &&
                Objects.equals(this.variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, controllers, textures, geometries, variables);
    }

    @Override
    public String toString() {
        return "BedrockEntityData[" +
                "identifier=" + identifier + ", " +
                "controllers=" + controllers + ", " +
                "textures=" + textures + ", " +
                "geometries=" + geometries + ", " +
                "variables=" + variables + ']';
    }

}
