package org.cube.converter.converter;

import org.cube.converter.converter.enums.RotationFixMode;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.util.MathUtil;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.legacy.RotationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormatConverter {
    public static JavaItemModel geometryToItemModel(final String texture, final BedrockGeometryModel geometry, final RotationFixMode fixMode) {
        final Position3V min = new Position3V(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), max = new Position3V(0, 0, 0);

        final List<Parent> parents = new ArrayList<>();
        for (final Parent old : geometry.getParents()) {
            final Parent parent = old.clone();

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                convertTo1Axis(cube);

                // TODO: inflate.
                if (fixMode == RotationFixMode.HACKY) {
                    RotationUtil.rotateIfPossible(cube);
                }

                final Position3V rotation = cube.getRotation();
                rotation.setX(Math.abs(rotation.getX()) == 180 ? 0 : MathUtil.limitAngle(rotation.getX()));
                rotation.setY(Math.abs(rotation.getY()) == 180 ? 0 : MathUtil.limitAngle(rotation.getY()));
                rotation.setZ(Math.abs(rotation.getZ()) == 180 ? 0 : MathUtil.limitAngle(rotation.getZ()));

                final Position3V from = cube.getPosition().asJavaPosition(cube.getSize());
                final Position3V to = from.add(cube.getSize());
                min.setX(Math.min(min.getX(), from.getX()));
                min.setY(Math.min(min.getY(), from.getY()));
                min.setZ(Math.min(min.getZ(), from.getZ()));
                max.setX(Math.max(max.getX(), to.getX()));
                max.setY(Math.max(max.getY(), to.getY()));
                max.setZ(Math.max(max.getZ(), to.getZ()));
            }

            parents.add(parent);
        }

        final double scale = calculateMinSize(min, max);
        final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize());
        model.getParents().addAll(parents);
        model.setScale(1 / scale);

        for (final Parent parent : parents) {
            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                scale(entry.getValue(), scale);
            }
        }

        return model;
    }

    private static void scale(final Cube cube, final double scale) {
        cube.getPosition().scale(scale);
        cube.getSize().scale(scale);
    }

    private static double calculateMinSize(final Position3V min, final Position3V max) {
        final Position3V overlapMin = min.getJavaOverlap(), overlapMax = max.getJavaOverlap();
        final Position3V totalOverlap = overlapMin.add(overlapMax);
        final double maxSize = Math.max(totalOverlap.getY(), totalOverlap.getX() + totalOverlap.getZ());
        double division = 32;
        if ((totalOverlap.getY() == maxSize && min.getY() < 0 || totalOverlap.getY() != maxSize && (min.getX() < 0 || min.getZ() < 0))) {
            division = 16;
        }

        return maxSize == 0 ? 1 : Math.min(1, division / (maxSize + 48));
    }

    private static void convertTo1Axis(final Cube cube) {
        double largestAxis = 0, axis = -1;
        final List<Double> axes = List.of(cube.getRotation().getX(), cube.getRotation().getY(), cube.getRotation().getZ());
        int index = 0;
        for (double angle : axes) {
            if (Math.abs(angle) > largestAxis && Math.abs(angle) % 45 != 0D) {
                largestAxis = Math.abs(angle);
                axis = index;
            }

            index++;
        }

        if (axis != -1) {
            final Position3V rotation = cube.getRotation();
            rotation.setX(axis == 0 ? 0 : MathUtil.limitAngle(rotation.getX()));
            rotation.setY(axis == 1 ? 0 : MathUtil.limitAngle(rotation.getY()));
            rotation.setZ(axis == 2 ? 0 : MathUtil.limitAngle(-rotation.getZ()));
        }
    }

    public static int getAxis(double[] axes) {
        double largestAxes = 0;
        int axis = 0;
        for (int i = 0; i < axes.length; i++) {
            if (Math.abs(axes[i]) > largestAxes && axes[i] % Math.abs(90) != 0D) {
                largestAxes = Math.abs(axes[i]);
                axis = i;
            }
        }

        return axis;
    }
}