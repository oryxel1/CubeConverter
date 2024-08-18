package org.oryxel.cube.model.java.other;

import org.oryxel.cube.util.Direction;

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

    private final Map<Direction, double[]> uvMap = new HashMap<>();

    public Element(String name, float angle, float rawAngle, String axis, double[] origin, double[] size, boolean mirror) {
        this.name = name;
        this.angle = angle;
        this.rawAngle = rawAngle;
        this.axis = axis;
        this.origin = origin;
        this.size = size;
        this.mirror = mirror;
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

    public Map<Direction, double[]> uvMap() {
        return uvMap;
    }

}
