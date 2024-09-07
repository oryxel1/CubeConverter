package org.oryxel.cube.util;

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

    public static void rotate90Degrees(Element element, double rotation, int axis) {
        switch (axis) {
            case 0 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 0, 1, element.origin());
                else rotate90Degrees(element, 0, 3, element.origin());
            }

            case 1 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 1, 1, element.origin());
                else rotate90Degrees(element, 1, 3, element.origin());
            }

            case 2 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 2, 1, element.origin());
                else rotate90Degrees(element, 2, 3, element.origin());
            }
        }
    }

    private static void rotate90Degrees(Element element, int axis, int steps, double[] origin) {
        origin = ArrayUtil.clone(origin);
        origin = ArrayUtil.getArrayWithOffset(origin);
        origin[0] = -origin[0];
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
