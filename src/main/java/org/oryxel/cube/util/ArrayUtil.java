package org.oryxel.cube.util;

import com.google.gson.JsonArray;

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
public class ArrayUtil {

    public static double[] getOverlap(double[] box) {
        double[] overlap = new double[3];
        double[] cloned = ArrayUtil.clone(box);
        cloned[0] = MathUtil.clamp(cloned[0], -16, 32);
        cloned[1] = MathUtil.clamp(cloned[1], -16, 32);
        cloned[2] = MathUtil.clamp(cloned[2], -16, 32);

        for (int i = 0; i < box.length; i++) {
            overlap[i] = Math.abs(box[i] - cloned[i]);
        }

        return overlap;
    }

    public static double[] build(double d, double d1) {
        return new double[] { d, d1 };
    }

    public static double[] fixRotation(double[] d) {
        return new double[] { -d[0], -d[1], d[2] };
    }

    public static double[] clone(double[] d) {
        return new double[] { d[0], d[1], d[2] };
    }

    public static double[] cloneW2Index(double[] d) {
        return new double[] { d[0], d[1] };
    }

    public static double[] size(double[] to, double[] from) {
        double x = to[0] - from[0], y = to[1] - from[1], z = to[2] - from[2];
        return new double[] { x, y, z };
    }

    public static double[] sizeAbs(double[] to, double[] from) {
        double x = Math.abs(to[0] - from[0]), y = Math.abs(to[1] - from[1]), z = Math.abs(to[2] - from[2]);
        return new double[] { x, y, z };
    }

    public static double[] getAsArray(JsonArray array) {
        if (array == null)
            return new double[] { 0D, 0D, 0D };

        return new double[] { array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble() };
    }

    public static double[] getArrayWithOffset(double[] array) {
        return new double[] { array[0] + 8, array[1], array[2] + 8 };
    }

    public static double[] combineArray(double[] array, double[] array1) {
        return new double[] { array[0] + array1[0], array[1] + array1[1], array[2] + array1[2] };
    }

    public static boolean isAllEmpty(double[] array) {
        for (double d : array) {
            if (Math.abs(d) != 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isOneNotEmpty(double[] array) {
        int notEmpties = 0;
        for (double d : array) {
            if (Math.abs(d) != 0) {
                notEmpties++;
            }
        }

        return notEmpties == 1 || notEmpties == 0;
    }

}
