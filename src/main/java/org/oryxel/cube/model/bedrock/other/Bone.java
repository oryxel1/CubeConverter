package org.oryxel.cube.model.bedrock.other;

import java.util.ArrayList;
import java.util.List;

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
public class Bone {
    private final String name;
    private final double[] pivot;
    private final List<Cube> cubes;

    public Bone(String name, double[] pivot) {
        this.name = name;
        this.pivot = pivot;

        this.cubes = new ArrayList<>();
    }

    public String name() {
        return name;
    }

    public double[] pivot() {
        return pivot;
    }

    public List<Cube> cubes() {
        return cubes;
    }
}