package org.oryxel.cube.util;

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
// Credit goes to BlockBench. https://github.com/JannisX11/blockbench
public class RotationUtil {

    public static double[] rotate(double[] from, double[] pivot, double[] rotation) {
        double radX = Math.toRadians(rotation[0]), radY = Math.toRadians(rotation[1]), radZ = Math.toRadians(rotation[2]);
        double pivotX = from[0] - pivot[0], pivotY = from[1] - pivot[1], pivotZ = from[2] - pivot[2];

        double y1 = pivotY * Math.cos(radX) - pivotZ * Math.sin(radX), z1 = pivotY * Math.sin(radX) + pivotZ * Math.cos(radX);
        double x1 = pivotX * Math.cos(radY) + z1 * Math.sin(radY);
        double x2 = x1 * Math.cos(radZ) - y1 * Math.sin(radZ), y2 = x1 * Math.sin(radZ) + y1 * Math.cos(radZ);
        z1 = -pivotX * Math.sin(radY) + z1 * Math.cos(radY);

        return new double[] { x2 + pivot[0], y2 + pivot[1], z1 + pivot[2] };
    }

}
