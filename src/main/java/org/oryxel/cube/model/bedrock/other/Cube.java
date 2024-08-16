package org.oryxel.cube.model.bedrock.other;

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
    private double inflate = Double.MAX_VALUE;
    private boolean mirror;

    public Cube(double[] origin, double[] size, double[] pivot, double[] rotation) {
        this.origin = origin;
        this.size = size;
        this.pivot = pivot;
        this.rotation = rotation;
    }

    public class PerFaceCube extends Cube {
        private final Map<Direction, double[]> uvMap = new HashMap<>();

        public PerFaceCube(double[] origin, double[] size, double[] pivot, double[] rotation) {
            super(origin, size, pivot, rotation);
        }

        public Map<Direction, double[]> uvMap() {
            return uvMap;
        }
    }

    public class BoxCube extends Cube {
        private final double[] uvOffset;

        public BoxCube(double[] origin, double[] size, double[] pivot, double[] rotation, double[] uvOffset) {
            super(origin, size, pivot, rotation);
            this.uvOffset = uvOffset;
        }

        public double[] uvOffset() {
            return uvOffset;
        }
    }

}