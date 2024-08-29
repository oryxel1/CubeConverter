package org.oryxel.cube.model.java.other;

import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.bedrock.other.Cube;
import org.oryxel.cube.util.Direction;
import org.oryxel.cube.util.UVUtil;

import java.util.HashMap;
import java.util.Map;

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
public class Element {

    private final String name;
    private double[] from, to;
    private final float angle, rawAngle;
    private final String axis;
    private double[] origin, size;
    private final boolean mirror;

    private String parent;

    private final Map<Direction, double[]> uvMap = new HashMap<>();

    public Element(BedrockGeometry geometry, Cube cube, String name, float angle, float rawAngle, String axis, double[] origin, double[] from, double[] to) {
        this.name = name;
        this.angle = angle;
        this.rawAngle = rawAngle;
        this.axis = axis;
        this.origin = origin;
        this.size = cube.size();
        this.mirror = cube.mirror();
        this.from = from;
        this.to = to;

        autoPortUv(geometry, cube);
    }

    private void autoPortUv(BedrockGeometry geometry, Cube cube) {
        Map<Direction, double[]> uv = new HashMap<>();
        if (cube instanceof Cube.PerFaceCube perFace && !perFace.uvMap().isEmpty()) {
            uv = UVUtil.portUv(perFace.uvMap(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
        } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
            uv = UVUtil.portUv(mirror, boxCube.uvOffset(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight());
        }

        this.uvMap.putAll(uv);
    }

    public String name() {
        return name;
    }

    public double[] from() {
        return from;
    }

    public void from(double[] from) {
        this.from = from;
    }

    public double[] to() {
        return to;
    }

    public void to(double[] to) {
        this.to = to;
    }

    public float angle() {
        return angle;
    }

    public float rawAngle() {
        return rawAngle;
    }

    public String axis() {
        return axis;
    }

    public double[] origin() {
        return origin;
    }

    public void origin(double[] origin) {
        this.origin = origin;
    }

    public double[] size() {
        return size;
    }

    public boolean mirror() {
        return mirror;
    }

    public String parent() {
        return parent;
    }

    public void parent(String parent) {
        this.parent = parent;
    }

    public Map<Direction, double[]> uvMap() {
        return uvMap;
    }

}
