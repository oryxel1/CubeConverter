package org.cube.converter.util.legacy;

import org.cube.converter.model.element.Cube;
import org.cube.converter.util.math.MathUtil;
import org.cube.converter.util.element.Direction;
import org.cube.converter.util.element.Position3V;

import java.util.HashMap;
import java.util.Map;

// Credit goes to BlockBench. https://github.com/JannisX11/blockbench
public class RotationUtil {
    public static void doHackyRotationIfPossiblePost1_21_60(final Cube cube) {

    }

    public static void doHackyRotationIfPossiblePre1_21_60(final Cube cube) {
        final Position3V rotation = cube.getRotation();
        // Since there only 1 angle now, this should be correct.
        float angle = rotation.getX() + rotation.getY() + rotation.getZ();
        if (!MathUtil.canDoHacky(angle)) {
            return;
        }
        angle = MathUtil.toHackyAngle(angle);

        final int axis = cube.getAxisIndex();
        if (Math.abs(angle) == 180) { // Hardcoded
            if (angle == -180) {
                roll(cube, -90, axis);
                roll(cube, -90, axis);
            } else {
                roll(cube, 90, axis);
                roll(cube, 90, axis);
            }

            return;
        }

        if (!MathUtil.isValidJavaAngle(90 - Math.abs(angle)) || angle == 0) {
            return;
        }

        if (Math.abs(angle) == 90) {
            roll(cube, angle, axis);
            cube.getRotation().set(Position3V.zero());
            return;
        }

        if (angle < 0) {
            angle = 90 + angle;
            roll(cube, -90, axis);
        } else {
            angle = angle - 90;
            roll(cube, 90, axis);
        }

        cube.getRotation().set(axis == 0 ? angle : 0, axis == 1 ? angle : 0, axis == 2 ? angle : 0);
    }

    public static void roll(final Cube element, float angle, int axis) {
        if (Math.abs(angle) != 90) {
            return;
        }

        roll(element, axis, angle == (axis == 1 ? -90 : 90) ? 1 : 3, element.getPivot().toArray());
    }

    private static void roll(final Cube element, int axis, int steps, float[] origin) {
        origin = ArrayUtil.clone(origin);
        origin = ArrayUtil.javaOffsetArray(origin);

        float[] from = element.getPosition().asJavaPosition(element.getSize()).toArray();
        float[] to = ArrayUtil.add(from, element.getSize().toArray());

        while (steps > 0) {
            steps--;
            //Swap coordinate thingy
            float[] cloneTo = ArrayUtil.clone(to);
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

            float[] size = ArrayUtil.size(to, from);
            element.getSize().set(new Position3V(size));
            element.getPosition().set(new Position3V(from).asBedrockPosition(element.getSize()));

            switch (axis) {
                case 0 -> {
                    element.getUvMap().rotate(Direction.WEST, 1);
                    element.getUvMap().rotate(Direction.EAST, 3);
                    element.getUvMap().rotate(Direction.NORTH, 2);
                    element.getUvMap().rotate(Direction.DOWN, 2);
                }

                case 1 -> {
                    element.getUvMap().rotate(Direction.UP, 1);
                    element.getUvMap().rotate(Direction.DOWN, 3);
                }

                case 2 -> {
                    element.getUvMap().rotate(Direction.NORTH, 1);
                    element.getUvMap().rotate(Direction.SOUTH, 3);
                    element.getUvMap().rotate(Direction.UP, 3);
                    element.getUvMap().rotate(Direction.EAST, 3);
                    element.getUvMap().rotate(Direction.WEST, 3);
                    element.getUvMap().rotate(Direction.DOWN, 3);
                }
            }
        }
    }

    private static void rotateCoord(float[] array, int axis, float[] origin) {
        Float a = null;
        int b = -1;

        for (int i = 0; i < array.length; i++) {
            float s = array[i];
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