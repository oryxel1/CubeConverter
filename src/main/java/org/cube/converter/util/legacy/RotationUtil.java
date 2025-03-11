package org.cube.converter.util.legacy;

import org.cube.converter.converter.FormatConverter;
import org.cube.converter.model.element.Cube;
import org.cube.converter.util.math.MathUtil;
import org.cube.converter.util.element.Direction;
import org.cube.converter.util.element.Position3V;

import java.util.HashMap;

// Credit goes to BlockBench. https://github.com/JannisX11/blockbench
public class RotationUtil {
    public static void rotateIfPossible(final Cube cube) {
        double[] array = cube.getRotation().toArray();

        for (int axis = 0; axis < array.length; axis++) {
            double rotation = array[axis];
            if (axis != 2) {
                rotation = -rotation;
            }

            if (Math.abs(rotation) == 180) {
                if (rotation == -180) {
                    RotationUtil.rotate90Degrees(cube, -90, axis, false);
                    RotationUtil.rotate90Degrees(cube, -90, axis, false);
                } else {
                    RotationUtil.rotate90Degrees(cube, 90, axis, false);
                    RotationUtil.rotate90Degrees(cube, 90, axis, false);
                }
                continue;
            }

            if (Math.abs(rotation) != 90)
                continue;

            RotationUtil.rotate90Degrees(cube, rotation, axis, true);

            double[] rotAxes = ArrayUtil.clone(array);
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

            int index = FormatConverter.getAxis(rotAxes);
            cube.getRotation().set(index == 0 ? MathUtil.limitAngle(rotAxes[index]) : 0,
                    index == 1 ? MathUtil.limitAngle(rotAxes[index]) : 0,
                    index == 2 ? MathUtil.limitAngle(rotAxes[index]) : 0);

            if (index == axis) {
                cube.getRotation().set(0, 0, 0);
            }

            if (index != 2) {
                cube.getRotation().set(-cube.getRotation().getX(), -cube.getRotation().getY(), -cube.getRotation().getZ());
            }
        }

        // Update this
        array = cube.getRotation().toArray();

        int axis = cube.getAxis().equals("x") ? 0 : cube.getAxis().equals("y") ? 1 : 2;
        double actual = array[axis];
        if (axis != 2) actual = -actual;

        final double[] pivotArray = cube.getPivot().toArray();
        if (Math.abs(actual) == 135 && pivotArray[0] == 0 && pivotArray[1] == 0 && pivotArray[2] == 0) {
            final double newAngle = actual > 0 ? -45 : 45;
            cube.getRotation().set(axis == 0 ? newAngle : 0, axis == 1 ? newAngle : 0, axis == 2 ? newAngle : 0);
            return;
        }

        double hackyRotation = 90 - Math.abs(actual);
        boolean isHackyValid = MathUtil.isValidJavaAngle(hackyRotation) && !MathUtil.isValidJavaAngle(actual)
                && actual != 0D && hackyRotation != 0D;

        if (!isHackyValid) {
            return;
        }

        double mulValue = Math.abs(actual) / actual;
        hackyRotation = (Math.abs(hackyRotation) * mulValue);

        RotationUtil.rotate90Degrees(cube, 90 * mulValue, axis, true);

        final double newAngle = Math.abs(actual) > 90 || Math.abs(actual) == 112.5 ? hackyRotation : - hackyRotation;
        cube.getRotation().set(axis == 0 ? newAngle : 0, axis == 1 ? newAngle : 0, axis == 2 ? newAngle : 0);
    }

    public static void rotate90Degrees(final Cube element, double rotation, int axis, boolean updateUv) {
        final double[] pivotArray = element.getPivot().toArray();

        switch (axis) {
            case 0 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 0, 1, pivotArray, updateUv);
                else rotate90Degrees(element, 0, 3, pivotArray, updateUv);
            }

            case 1 -> {
                if (rotation == -90)
                    rotate90Degrees(element, 1, 1, pivotArray, updateUv);
                else rotate90Degrees(element, 1, 3, pivotArray, updateUv);
            }

            case 2 -> {
                if (rotation == 90)
                    rotate90Degrees(element, 2, 1, pivotArray, updateUv);
                else rotate90Degrees(element, 2, 3, pivotArray, updateUv);
            }
        }
    }

    private static void rotate90Degrees(final Cube element, int axis, int steps, double[] origin, boolean updateUv) {
        origin = ArrayUtil.clone(origin);
        origin = ArrayUtil.javaOffsetArray(origin);
        // origin[0] = -origin[0];

        double[] from = element.getPosition().asJavaPosition(element.getSize()).toArray();
        double[] to = ArrayUtil.add(from, element.getSize().toArray());

        while (steps > 0) {
            steps--;
            //Swap coordinate thingy
            double[] cloneTo = ArrayUtil.clone(to);
             switch(axis) {
                case 0, 1 -> {
                    to[2] = from[2];
                    from[2] = cloneTo[2];
                }
                case 2 -> {
                    to[1] = from[1];
                    from[1] = cloneTo[1];
                }
            }

            rotateCoord(from, axis, origin);
            rotateCoord(to, axis, origin);
            // element.origin(rotateCoord(element.origin(), axis, origin));

            double[] size = ArrayUtil.size(to, from);
            element.getSize().set(new Position3V(size));
            element.getPosition().set(new Position3V(from).asBedrockPosition(element.getSize()));

            if (!updateUv) {
                continue;
            }

            final HashMap<Direction, Double[]> old = new HashMap<>(element.getUvMap().getMap());

            switch (axis) {
                case 0 -> {
                    element.getUvMap().getMap().put(Direction.NORTH, old.get(Direction.DOWN));
                    element.getUvMap().getMap().put(Direction.DOWN, old.get(Direction.SOUTH));
                    element.getUvMap().getMap().put(Direction.SOUTH, old.get(Direction.UP));
                    element.getUvMap().getMap().put(Direction.UP, old.get(Direction.NORTH));
                }

                case 1 -> {
                    element.getUvMap().getMap().put(Direction.NORTH, old.get(Direction.WEST));
                    element.getUvMap().getMap().put(Direction.WEST, old.get(Direction.SOUTH));
                    element.getUvMap().getMap().put(Direction.SOUTH, old.get(Direction.EAST));
                    element.getUvMap().getMap().put(Direction.EAST, old.get(Direction.NORTH));
                }

                case 2 -> {
                    element.getUvMap().getMap().put(Direction.EAST, old.get(Direction.DOWN));
                    element.getUvMap().getMap().put(Direction.DOWN, old.get(Direction.WEST));
                    element.getUvMap().getMap().put(Direction.WEST, old.get(Direction.UP));
                    element.getUvMap().getMap().put(Direction.UP, old.get(Direction.EAST));
                }
            }
        }
    }

    private static void rotateCoord(double[] array, int axis, double[] origin) {
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

    }
}