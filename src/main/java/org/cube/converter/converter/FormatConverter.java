package org.cube.converter.converter;

import org.cube.converter.converter.enums.RotationFixMode;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.util.MathUtil;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.legacy.RotationUtil;

import java.util.List;
import java.util.Map;

public class FormatConverter {
    public static JavaItemModel geometryToItemModel(final String texture, final BedrockGeometryModel geometry, RotationFixMode fixMode) {
        final Position3V min = new Position3V(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), max = new Position3V(0, 0, 0);
        for (final Parent parent : geometry.getParents()) {
            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                // TODO: inflate.
                if (fixMode == RotationFixMode.HACKY) {
                    RotationUtil.rotateIfPossible(cube);
                }

                convertTo1Axis(cube);

                final Position3V from = cube.getPosition().asJavaPosition(cube.getSize());
                final Position3V to = from.add(cube.getSize());
                min.setX(Math.min(min.getX(), from.getX()));
                min.setY(Math.min(min.getY(), from.getY()));
                min.setZ(Math.min(min.getZ(), from.getZ()));
                max.setX(Math.max(max.getX(), to.getX()));
                max.setY(Math.max(max.getY(), to.getY()));
                max.setZ(Math.max(max.getZ(), to.getZ()));
            }
        }

        final double scale = calculateMinSize(min, max);
        final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize());
        model.getParents().addAll(geometry.getParents());
        model.setScale(1 / scale);

        for (final Parent parent : geometry.getParents()) {
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
        double largestAxis = 0, axis = 0;
        final List<Double> axes = List.of(cube.getRotation().getX(), cube.getRotation().getY(), cube.getRotation().getZ());
        for (double angle : axes) {
            if (Math.abs(angle) > largestAxis && angle % Math.abs(90) != 0D) {
                largestAxis = Math.abs(angle);
                axis = Math.abs(angle) == 180 ? 0 : MathUtil.limitAngle(angle);
                if (angle != cube.getRotation().getZ()) {
                    axis = -angle;
                }
            }
        }

        final Position3V rotation = cube.getRotation();
        cube.getRotation().setX(rotation.getX() != axis ? 0 : rotation.getX());
        cube.getRotation().setY(rotation.getY() != axis ? 0 : rotation.getY());
        cube.getRotation().setZ(rotation.getZ() != axis ? 0 : rotation.getZ());
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