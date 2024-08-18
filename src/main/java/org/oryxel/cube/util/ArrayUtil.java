package org.oryxel.cube.util;

import com.viaversion.viaversion.libs.gson.JsonArray;

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

    public static long pack(double[] array) {
        return ((long) ((int)array[0]) << 32) | ((long) ((int)array[1]) << 16) | (long) ((int)array[2]);
    }

    public static double[] build(double d, double d1) {
        return new double[] { d, d1 };
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

    public static double largest(double[] d) {
        double largest = d[0];

        for (int i = 0; i < d.length; i++) {
            if (d[i] > largest)
                largest = d[i];
        }

        return largest;
    }

    public static double smallest(double[] d) {
        double smallest = d[0];

        for (int i = 0; i < d.length; i++) {
            if (d[i] < smallest)
                smallest = d[i];
        }

        return smallest;
    }

    public static double[] multiply(double[] d, double v[]) {
        double[] cloned = clone(d);

        for (int i = 0; i < cloned.length; i++) {
            cloned[i] = cloned[i] * v[i];
        }

        return cloned;
    }

    public static double[] multiply(double[] d, double v) {
        double[] cloned = clone(d);

        for (int i = 0; i < cloned.length; i++) {
            cloned[i] = cloned[i] * v;
        }

        return cloned;
    }


    public static double[] divide(double[] d, double v) {
        double[] cloned = clone(d);

        for (int i = 0; i < cloned.length; i++) {
            cloned[i] = cloned[i] / v;
        }

        return cloned;
    }

    public static double[] divide(double[] d, double v[]) {
        double[] cloned = clone(d);

        for (int i = 0; i < cloned.length; i++) {
            cloned[i] = cloned[i] / v[i];
        }

        return cloned;
    }

    public static double[] max(double[] d, double max) {
        double[] cloned = clone(d);

        for (int i = 0; i < cloned.length; i++) {
            cloned[i] = cloned[i] > max ? max : cloned[i];
        }

        return cloned;
    }

    public static boolean isSmaller(double[] array, double[] array1) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] > array1[i])
                return false;
        }

        return true;
    }

    public static boolean isBigger(double[] array, double[] array1) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] < array1[i])
                return false;
        }

        return true;
    }

    public static double[] clamp(double[] d, double max, double min) {
        for (int i = 0; i < d.length; i++) {
            d[i] = MathUtil.clamp(d[i], min, max);
        }

        return d;
    }

    public static double[] getAsArray(JsonArray array) {
        if (array == null)
            return new double[] { 0D, 0D, 0D };

        return new double[] { array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble() };
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

    public static boolean isCloseEnough(double[] array, double v, int exception) {
        for (int i = 0; i < array.length; i++) {
            if (i == exception)
                continue;

            double distance = Math.abs(array[i] - v);
            if (distance > 4)
                return false;
        }

        return true;
    }

    public static boolean isAllCloseEnough(double[] array, double v) {
        return isAllCloseEnough(array, 12, v);
    }

    public static boolean isAllCloseEnough(double[] array, double close, double v) {
        for (double a : array) {
            double distance = Math.abs(a - v);
            if (distance > close)
                return false;
        }

        return true;
    }

    public static boolean isAllCloseEnough(double[] array, double[] array1, double close) {
        for (int i = 0; i < array.length; i++) {
            double distance = Math.abs(array[i] - array1[i]);
            if (distance > close)
                return false;
        }

        return true;
    }

}
