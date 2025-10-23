package org.cube.converter.util.legacy;

import org.cube.converter.converter.FormatConverter;
import org.cube.converter.model.element.Cube;
import org.cube.converter.util.math.MathUtil;
import org.cube.converter.util.element.Direction;
import org.cube.converter.util.element.Position3V;

import java.util.List;

// Credit goes to BlockBench. https://github.com/JannisX11/blockbench
public class RotationUtil {
    public static void rotateBy90r180DegreesIfPossible(final Cube cube) {
        if (cube.isThereOneAngleOnly()) {
            return;
        }

        final Position3V rotation = cube.getRotation();

        if (MathUtil.closeEnoughAbs(rotation.getX(), 90)) {
            rotation.setX(0);
            roll(cube, rotation.getX(), 0);
        } else if (MathUtil.closeEnoughAbs(rotation.getX(), 180)) {
            rotation.setX(0);
            roll(cube, Math.signum(rotation.getX()) * 90, 0);
            roll(cube, Math.signum(rotation.getX()) * 90, 0);
        }

        if (MathUtil.closeEnoughAbs(rotation.getY(), 90)) {
            rotation.setY(0);
            roll(cube, rotation.getY(), 1);
        } else if (MathUtil.closeEnoughAbs(rotation.getY(), 180)) {
            rotation.setY(0);
            roll(cube, Math.signum(rotation.getY()) * 90, 1);
            roll(cube, Math.signum(rotation.getY()) * 90, 1);
        }

        if (MathUtil.closeEnoughAbs(rotation.getZ(), 90)) {
            rotation.setZ(0);
            roll(cube, rotation.getZ(), 1);
        } else if (MathUtil.closeEnoughAbs(rotation.getZ(), 180)) {
            rotation.setZ(0);
            roll(cube, Math.signum(rotation.getZ()) * 90, 2);
            roll(cube, Math.signum(rotation.getZ()) * 90, 2);
        }
    }

    public static void priorityBestAngle(final Cube cube, boolean pre1_21_60) {
        float largestAxis = 0, axis = -1;
        final List<Float> axes = List.of(cube.getRotation().getX(), cube.getRotation().getY(), cube.getRotation().getZ());
        int index = 0;
        for (float angle : axes) {
            if (Math.abs(angle) > largestAxis && !(!MathUtil.canDoHacky(angle, pre1_21_60) || angle == 0)) {
                largestAxis = Math.abs(angle);
                axis = index;
            }

            index++;
        }

        if (axis != -1) {
            final Position3V rotation = cube.getRotation();
            rotation.setX(axis != 0 ? 0 : -rotation.getX());
            rotation.setY(axis != 1 ? 0 : -rotation.getY());
            rotation.setZ(axis != 2 ? 0 : rotation.getZ());
        } else {
            FormatConverter.convertTo1Axis(cube);
        }
    }

    public static void doHackyRotationIfPossible(final Cube cube, boolean pre1_21_60) {
        final Position3V rotation = cube.getRotation();
        // Since there only 1 angle now, this should be correct.
        float angle = rotation.getX() + rotation.getY() + rotation.getZ();
        if (!MathUtil.canDoHacky(angle, pre1_21_60) || angle == 0) {
            return;
        }
        if (pre1_21_60) {
            angle = MathUtil.toHackyAngle(angle);
        }

        final int axis = cube.getAxisIndex();

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