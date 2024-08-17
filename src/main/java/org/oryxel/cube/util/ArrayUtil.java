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
public class ArrayUtil {

    public static double[] build(double d, double d1) {
        return new double[] { d, d1 };
    }

    public static double[] clone(double[] d) {
        return new double[] { d[0], d[1], d[2] };
    }

    public static double[] cloneW2Index(double[] d) {
        return new double[] { d[0], d[1] };
    }

    public static double[] clamp(double[] d, double max, double min) {
        for (int i = 0; i < d.length; i++) {
            d[i] = MathUtil.clamp(d[i], min, max);
        }

        return d;
    }

    public static double[] addOffsetToArray(double[] array, double offset) {
        for (var i = 0; i < 3; i++) {
            array[i] = array[i] + offset;
        }

        return array;
    }

    public static double[] getArrayWithOffset(double[] array) {
        return new double[] { array[0] + 8, array[1], array[2] + 8 };
    }

    public static double[] combineArray(double[] array, double[] array1) {
        return new double[] { array[0] + array1[0], array[1] + array1[1], array[2] + array1[2] };
    }

}
