package org.oryxel.cube.model.bedrock.model;

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
public class Cube {
    private final double[] origin;
    private final double[] size;
    private final double[] pivot;
    private final double[] rotation;
    private double inflate = 0;
    private boolean mirror;

    private String parent = "";

    public Cube(double[] origin, double[] size, double[] pivot, double[] rotation) {
        this.origin = origin;
        this.size = size;
        this.pivot = pivot;
        this.rotation = rotation;
    }

    public static class PerFaceCube extends Cube {
        private final Map<Direction, double[]> uvMap = new HashMap<>();

        public PerFaceCube(double[] origin, double[] size, double[] pivot, double[] rotation) {
            super(origin, size, pivot, rotation);

            this.uvMap.put(Direction.NORTH, new double[] { 0, 0, 0, 0 });
            this.uvMap.put(Direction.WEST, new double[] { 0, 0, 0, 0 });
            this.uvMap.put(Direction.EAST, new double[] { 0, 0, 0, 0 });
            this.uvMap.put(Direction.UP, new double[] { 0, 0, 0, 0 });
            this.uvMap.put(Direction.DOWN, new double[] { 0, 0, 0, 0 });
        }

        public Map<Direction, double[]> uvMap() {
            return uvMap;
        }
    }

    public static class BoxCube extends Cube {
        private final double[] uvOffset;

        public BoxCube(double[] origin, double[] size, double[] pivot, double[] rotation, double[] uvOffset) {
            super(origin, size, pivot, rotation);
            this.uvOffset = uvOffset;
        }

        public double[] uvOffset() {
            return uvOffset;
        }
    }

    public double[] rotation() {
        return rotation;
    }

    public double[] origin() {
        return origin;
    }

    public double[] size() {
        return size;
    }

    public double[] pivot() {
        return pivot;
    }

    public double inflate() {
        return inflate;
    }

    public void inflate(double inflate) {
        this.inflate = inflate;
    }

    public boolean mirror() {
        return mirror;
    }

    public void mirror(boolean mirror) {
        this.mirror = mirror;
    }

    public String parent() {
        return parent;
    }

    public void parent(String parent) {
        this.parent = parent;
    }

}