package org.cube.converter.converter;

import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.legacy.RotationUtil;

import java.util.*;

public class FormatConverter {
    public static JavaItemModel geometryToItemModel(final String texture, final BedrockGeometryModel geometry, final RotationType type) {
        final Position3V min = new Position3V(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), max = new Position3V(0, 0, 0);

        final List<Parent> parents = new ArrayList<>();
        for (final Parent old : geometry.getParents()) {
            final Parent parent = old.clone();

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                cube.inflate();
                cube.getPivot().setX(-cube.getPivot().getX());

                if (type != RotationType.HACKY_POST_1_21_60) {
                    convertTo1Axis(cube);
                }

                if (type == RotationType.HACKY_PRE_1_21_60) {
                    RotationUtil.doHackyRotationIfPossiblePre1_21_60(cube);
                } else if (type == RotationType.HACKY_POST_1_21_60) {
                    RotationUtil.doHackyRotationIfPossiblePost1_21_60(cube);
                    convertTo1Axis(cube);
                }

                if (type == RotationType.PRE_1_21_60 || type == RotationType.HACKY_PRE_1_21_60) {
                    cube.clampToJavaLimitedAngle();
                }
                calculateMinMax(cube, min, max);
            }

            parents.add(parent);
        }

        final float scale = calculateMinSize(min, max);
        final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize());
        model.getParents().addAll(parents);

        scale(model, scale);
        return model;
    }

    private static void scale(final JavaItemModel model, final float scale) {
        model.setScale(1 / scale);

        for (final Parent parent : model.getParents()) {
            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                scale(entry.getValue(), scale);
            }
        }
    }

    private static void scale(final Cube cube, final float scale) {
        cube.getPosition().scale(scale);
        cube.getSize().scale(scale);
        cube.getPivot().scale(scale);
    }

    private static void calculateMinMax(final Cube cube, final Position3V min, final Position3V max) {
        final Position3V from = cube.getPosition().asJavaPosition(cube.getSize());
        final Position3V to = from.add(cube.getSize());
        min.setX(Math.min(min.getX(), from.getX()));
        min.setY(Math.min(min.getY(), from.getY()));
        min.setZ(Math.min(min.getZ(), from.getZ()));
        max.setX(Math.max(max.getX(), to.getX()));
        max.setY(Math.max(max.getY(), to.getY()));
        max.setZ(Math.max(max.getZ(), to.getZ()));
    }

    // I can't remember how the fuck this size calculation even works, but it works.
    private static float calculateMinSize(final Position3V min, final Position3V max) {
        final Position3V overlapMin = min.getJavaOverlap(), overlapMax = max.getJavaOverlap();
        final Position3V totalOverlap = overlapMin.add(overlapMax);
        final float maxSize = Math.max(totalOverlap.getY(), totalOverlap.getX() + totalOverlap.getZ());
        float division = 32;
        if ((totalOverlap.getY() == maxSize && min.getY() < 0 || totalOverlap.getY() != maxSize && (min.getX() < 0 || min.getZ() < 0))) {
            division = 16;
        }

        return maxSize == 0 ? 1 : Math.min(1, division / (maxSize + 48));
    }

    // Find the largest angle and use that one.
    private static void convertTo1Axis(final Cube cube) {
        float largestAxis = 0, axis = -1;
        final List<Float> axes = List.of(cube.getRotation().getX(), cube.getRotation().getY(), cube.getRotation().getZ());
        int index = 0;
        for (float angle : axes) {
            if (Math.abs(angle) > largestAxis) {
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
        }
    }
}