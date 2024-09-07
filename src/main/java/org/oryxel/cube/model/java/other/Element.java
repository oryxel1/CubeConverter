package org.oryxel.cube.model.java.other;

import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.bedrock.model.Cube;
import org.oryxel.cube.util.*;

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
    private float angle;
    private final String axis;
    private double[] origin, size;
    private final boolean mirror;
    private final double inflate;

    private String parent;

    private final Map<Direction, double[]> uvMap = new HashMap<>();

    public Element(BedrockGeometry geometry, Cube cube, String name, float angle, String axis, double[] origin, double[] from, double[] to) {
        this.name = name;
        this.angle = angle;
        this.axis = axis;
        this.origin = origin;
        this.size = cube.size();
        this.mirror = cube.mirror();
        this.from = from;
        this.to = to;
        this.inflate = cube.inflate();

        autoPortUv(geometry, cube);
        rotateIfPossible(cube);
    }

    private void rotateIfPossible(Cube cube) {
        for (int axis = 0; axis < cube.rotation().length; axis++) {
            double rotation = cube.rotation()[axis];
            if (axis != 2) rotation = -rotation;
            if (Math.abs(rotation) != 90)
                continue;

            RotationUtil.rotate90Degrees(this, rotation, axis);
            this.angle = 0;
        }

        int axis = this.axis.equals("x") ? 0 : this.axis.equals("y") ? 1 : 2;
        double actual = cube.rotation()[axis];
        if (axis != 2) actual = -actual;

        if (Math.abs(actual) == 135 && cube.pivot()[0] == 0 && cube.pivot()[1] == 0 && cube.pivot()[2] == 0) {
            this.angle = actual > 0 ? -45 : 45;
            return;
        }

        float hackyRotation = (float) (90 - Math.abs(actual));
        boolean isHackyValid = MathUtil.isValidJavaAngle(hackyRotation) && !MathUtil.isValidJavaAngle(actual)
                && actual != 0D && hackyRotation != 0D;

        if (!isHackyValid)
            return;

        double mulValue = Math.abs(actual) / actual;
        hackyRotation = (float) (Math.abs(hackyRotation) * mulValue);

        RotationUtil.rotate90Degrees(this, 90 * mulValue, axis);
        this.angle = Math.abs(actual) > 90 || Math.abs(actual) == 112.5 ? hackyRotation : - hackyRotation;
    }

    private void autoPortUv(BedrockGeometry geometry, Cube cube) {
        Map<Direction, double[]> uv = new HashMap<>();
        boolean boxUv = false;
        if (cube instanceof Cube.PerFaceCube perFace && !perFace.uvMap().isEmpty()) {
            uv = perFace.uvMap();
        } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
            uv = UVUtil.rawPortUv(mirror, boxCube.uvOffset(), from, to);
            boxUv = true;
        }

        for (Map.Entry<Direction, double[]> entry : uv.entrySet()) {
            double sizeValue2 = boxUv ? entry.getValue()[2] - entry.getValue()[0] : entry.getValue()[2],
                    sizeValue3 = boxUv ? entry.getValue()[3] - entry.getValue()[1] : entry.getValue()[3];
            if (cube.rotation()[1] == 180 || cube.rotation()[0] == 180) {
                entry.getValue()[0] = entry.getValue()[0] + sizeValue2;
                entry.getValue()[2] = boxUv ? entry.getValue()[0] - sizeValue2 : -entry.getValue()[2];
            }

            if (cube.rotation()[0] == 180 || cube.rotation()[2] == 180) {
                entry.getValue()[1] = entry.getValue()[1] + sizeValue3;
                entry.getValue()[3] = boxUv ? entry.getValue()[1] - sizeValue3 : -entry.getValue()[3];
            }
        }

        uv = UVUtil.portUv(uv, geometry.textureWidth(), geometry.textureHeight(), boxUv);
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

    public String axis() {
        return axis;
    }

    public double[] origin() {
        return origin;
    }

    public void origin(double[] origin) {
        this.origin = origin;
    }

    public void size(double[] size) {
        this.size = size;
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

    public double inflate() {
        return inflate;
    }

    public void parent(String parent) {
        this.parent = parent;
    }

    public Map<Direction, double[]> uvMap() {
        return uvMap;
    }

}
