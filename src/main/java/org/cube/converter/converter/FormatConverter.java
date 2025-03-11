package org.cube.converter.converter;

import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.util.math.MathUtil;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.legacy.RotationUtil;
import org.cube.converter.util.math.matrix.MatrixUtil;

import java.util.*;

public class FormatConverter {
    public static List<JavaItemModel> geometryToMultipleModels(final String texture, final BedrockGeometryModel geometry) {
        final List<JavaItemModel> models = new ArrayList<>();
        final Position3V min = new Position3V(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), max = new Position3V(0, 0, 0);

        final JavaItemModel baseModel = new JavaItemModel(texture, geometry.getTextureSize());
        int baseModelCount = 0;

        final Map<String, Parent> parentMap = new HashMap<>();
        for (final Parent parent : geometry.getParents()) {
            parentMap.put(parent.getName(), parent);
        }

        for (final Parent parent : geometry.getParents()) {
            final List<Position3V> parentRotations = new ArrayList<>();
            Parent currentParent = parent;
            while (currentParent != null && !currentParent.getParent().isEmpty()) {
                final Position3V rotation = currentParent.getRotation();

                if (!rotation.isZero()) {
                    parentRotations.add(rotation);
                }

                currentParent = parentMap.get(currentParent.getParent());
            }

            // Correct order.
            Collections.reverse(parentRotations);

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue().clone();

                cube.inflate();
                calculateMinMax(cube, min, max);

                // The rotation is valid and there is no parent rotation either, move it into base model.
                if (MathUtil.isValidRotation(cube.getRotation()) && parentRotations.isEmpty()) {
                    convertTo1Axis(cube);

                    final Parent parent1 = new Parent(cube.getParent() + cube.hashCode(), Position3V.zero(), Position3V.zero());
                    parent1.getCubes().put(baseModelCount++, cube);
                    baseModel.getParents().add(parent1);
                    continue;
                }

                final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize(), MatrixUtil.getTransformation(parentRotations, cube));

                cube.getPivot().set(Position3V.zero());
                cube.getRotation().set(Position3V.zero());
                cube.getPosition().set(Position3V.zero());

                final Parent parent1 = new Parent(cube.getParent() + cube.hashCode(), Position3V.zero(), Position3V.zero());
                parent1.getCubes().put(0, cube);
                baseModel.getParents().add(parent1);

                model.getParents().add(parent1);
                models.add(model);
            }
        }

        if (!baseModel.getParents().isEmpty()) {
            models.add(baseModel);
        }

        final double scale = calculateMinSize(min, max);
        for (final JavaItemModel model : models) {
            scale(model, scale); // Scale down.
        }

        return models;
    }

    public static JavaItemModel geometryToItemModel(final String texture, final BedrockGeometryModel geometry, final boolean workaround) {
        final Position3V min = new Position3V(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), max = new Position3V(0, 0, 0);

        final List<Parent> parents = new ArrayList<>();
        for (final Parent old : geometry.getParents()) {
            final Parent parent = old.clone();

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                convertTo1Axis(cube);
                cube.inflate();

                if (workaround) {
                    RotationUtil.rotateIfPossible(cube);
                }

                cube.fixRotationIfNeeded();
                calculateMinMax(cube, min, max);
            }

            parents.add(parent);
        }

        final double scale = calculateMinSize(min, max);
        final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize());
        model.getParents().addAll(parents);

        scale(model, scale);
        return model;
    }

    private static void scale(final JavaItemModel model, final double scale) {
        model.setScale(1 / scale);

        for (final Parent parent : model.getParents()) {
            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                scale(entry.getValue(), scale);
            }
        }
    }

    private static void scale(final Cube cube, final double scale) {
        cube.getPosition().scale(scale);
        cube.getSize().scale(scale);
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