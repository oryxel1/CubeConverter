package org.oryxel.cube.util;

import org.oryxel.cube.converter.FormatConverter;
import org.oryxel.cube.model.bedrock.model.Cube;
import org.oryxel.cube.model.java.other.Element;

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
// Credit goes to BlockBench. https://github.com/JannisX11/blockbench
public class RotationUtil {

    public static void rotateIfPossible(Element element, Cube cube) {
        for (int axis = 0; axis < cube.rotation().length; axis++) {
            double rotation = cube.rotation()[axis];
            if (axis != 2) rotation = -rotation;

            if (Math.abs(rotation) == 180) {
                if (rotation == -180) {
                    RotationUtil.rotate90Degrees(element, -90, axis, false);
                    RotationUtil.rotate90Degrees(element, -90, axis, false);
                } else {
                    RotationUtil.rotate90Degrees(element, 90, axis, false);
                    RotationUtil.rotate90Degrees(element, 90, axis, false);
                }

                continue;
            }

            if (Math.abs(rotation) != 90)
                continue;

            RotationUtil.rotate90Degrees(element, rotation, axis, true);

            double[] rotAxes = ArrayUtil.clone(cube.rotation());
            var i = 0;
            Double temp_rot = null;
            Integer temp_i = null;
            while (i < 3) {
                if (i != axis) {
                    if (temp_rot == null) {
                        temp_rot = rotAxes[i];
                        temp_i = i;
                    } else {
                        rotAxes[temp_i] = -rotAxes[i];
                        rotAxes[i] = temp_rot;
                    }
                }
                i++;
            }

            int axisIndex = FormatConverter.getAxis(rotAxes);

            element.axis(axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z");
            element.angle(MathUtil.clampToJavaAngle(rotAxes[axisIndex]));

            if (axisIndex == axis) {
                element.angle(0);
            }

            if (axisIndex != 2) {
                element.angle(-element.angle());
            }
        }

        int axis = element.axis().equals("x") ? 0 : element.axis().equals("y") ? 1 : 2;
        double actual = cube.rotation()[axis];
        if (axis != 2) actual = -actual;

        if (Math.abs(actual) == 135 && cube.pivot()[0] == 0 && cube.pivot()[1] == 0 && cube.pivot()[2] == 0) {
            element.angle(actual > 0 ? -45 : 45);
            return;
        }

        float hackyRotation = (float) (90 - Math.abs(actual));
        boolean isHackyValid = MathUtil.isValidJavaAngle(hackyRotation) && !MathUtil.isValidJavaAngle(actual)
                && actual != 0D && hackyRotation != 0D;

        if (!isHackyValid)
            return;

        double mulValue = Math.abs(actual) / actual;
        hackyRotation = (float) (Math.abs(hackyRotation) * mulValue);

        RotationUtil.rotate90Degrees(element, 90 * mulValue, axis, true);
        element.angle(Math.abs(actual) > 90 || Math.abs(actual) == 112.5 ? hackyRotation : - hackyRotation);
    }

    public static void rotate90Degrees(Element element, double rotation, int axis, boolean updateUv) {
        switch (axis) {
            case 0 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 0, 1, element.origin(), updateUv);
                else rotate90Degrees(element, 0, 3, element.origin(), updateUv);
            }

            case 1 -> {
                if (rotation == -90)
                    rotate90Degrees(element, 1, 1, element.origin(), updateUv);
                else rotate90Degrees(element, 1, 3, element.origin(), updateUv);
            }

            case 2 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 2, 1, element.origin(), updateUv);
                else rotate90Degrees(element, 2, 3, element.origin(), updateUv);
            }
        }
    }

    private static void rotate90Degrees(Element element, int axis, int steps, double[] origin, boolean updateUv) {
        origin = ArrayUtil.clone(origin);
        origin = ArrayUtil.javaOffsetArray(origin);
        // origin[0] = -origin[0];

        while (steps > 0) {
            steps--;
            //Swap coordinate thingy
            double[] cloneTo = ArrayUtil.clone(element.to());
             switch(axis) {
                case 0, 1 -> {
                    element.to()[2] = element.from()[2];
                    element.from()[2] = cloneTo[2];
                }
                case 2 -> {
                    element.to()[1] = element.from()[1];
                    element.from()[1] = cloneTo[1];
                }
            }

            element.from(rotateCoord(element.from(), axis, origin));
            element.to(rotateCoord(element.to(), axis, origin));
            // element.origin(rotateCoord(element.origin(), axis, origin));

            element.size(ArrayUtil.size(element.to(), element.from()));

            if (!updateUv) {
                continue;
            }

            Map<Direction, double[]> old = new HashMap<>(element.uvMap());

            switch (axis) {
                case 0 -> {
                    element.uvMap().put(Direction.NORTH, old.get(Direction.DOWN));
                    element.uvMap().put(Direction.DOWN, old.get(Direction.SOUTH));
                    element.uvMap().put(Direction.SOUTH, old.get(Direction.UP));
                    element.uvMap().put(Direction.UP, old.get(Direction.NORTH));
                }

                case 1 -> {
                    element.uvMap().put(Direction.NORTH, old.get(Direction.WEST));
                    element.uvMap().put(Direction.WEST, old.get(Direction.SOUTH));
                    element.uvMap().put(Direction.SOUTH, old.get(Direction.EAST));
                    element.uvMap().put(Direction.EAST, old.get(Direction.NORTH));
                }

                case 2 -> {
                    element.uvMap().put(Direction.EAST, old.get(Direction.DOWN));
                    element.uvMap().put(Direction.DOWN, old.get(Direction.WEST));
                    element.uvMap().put(Direction.WEST, old.get(Direction.UP));
                    element.uvMap().put(Direction.UP, old.get(Direction.EAST));
                }
            }
        }
    }

    private static double[] rotateCoord(double[] array, int axis, double[] origin) {
        Double a = null;
        int b = -1;

        for (int i = 0; i < array.length; i++) {
            double s = array[i];
            if (i != axis) {
                if (a == null) {
                    a = s - origin[i];
                    b = i;
                } else {
                    array[b] = s - origin[i];
                    array[b] = origin[b] - array[b];
                    array[i] = origin[i] + a;
                }
            }
        }

        return array;
    }

}