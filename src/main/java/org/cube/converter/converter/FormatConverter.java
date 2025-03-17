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
        final Position3V min = new Position3V(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), max = new Position3V(0, 0, 0);

        final JavaItemModel baseModel = new JavaItemModel(texture, geometry.getTextureSize());
        int baseModelCount = 0;

        final Map<String, Parent> parentMap = new HashMap<>();
        for (final Parent parent : geometry.getParents()) {
            parentMap.put(parent.getName(), parent);
        }

        final Map<Cube, List<Map.Entry<Position3V, Position3V>>> rotations = new HashMap<>();

        for (final Parent old : geometry.getParents()) {
            final Parent parent = old.clone();

            final List<Map.Entry<Position3V, Position3V>> parentRotations = new ArrayList<>();
            Parent currentParent = parent;

            while (currentParent != null) {
                final Position3V rotation = currentParent.getRotation();

                if (!rotation.isZero()) {
                    parentRotations.add(Map.entry(currentParent.getPivot(), rotation));
                }

                currentParent = parentMap.get(currentParent.getParent());
            }

            // Correct order.
            Collections.reverse(parentRotations);

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                cube.getPivot().setX(-cube.getPivot().getX());

                cube.inflate();
                calculateMinMax(cube, min, max);

                // This rotation is valid and there is no parent rotation either, move it into base model.
                if (MathUtil.isValidRotation(cube.getRotation()) && parentRotations.isEmpty()) {
                    convertTo1Axis(cube);
                    cube.fixRotationIfNeeded();

                    final Parent parent1 = new Parent(cube.getParent() + cube.hashCode(), Position3V.zero(), Position3V.zero());
                    parent1.getCubes().put(baseModelCount++, cube);
                    baseModel.getParents().add(parent1);
                    continue;
                }

                final JavaItemModel model = new JavaItemModel(texture, geometry.getTextureSize());

                final Parent parent1 = new Parent(cube.getParent() + cube.hashCode(), Position3V.zero(), Position3V.zero());
                parent1.getCubes().put(0, cube);
                model.getParents().add(parent1);
                models.add(model);

                rotations.put(cube, parentRotations);
            }
        }

        if (!baseModel.getParents().isEmpty()) {
            models.add(baseModel);
        }

        Collections.reverse(models);

        final float scale = calculateMinSize(min, max);
        for (final JavaItemModel model : models) {
            scale(model, scale); // Scale down.

            if (model.getParents().size() > 1) {
                continue;
            }

            final Parent parent = model.getParents().get(0);
            if (!parent.getCubes().containsKey(0)) {
                continue;
            }

            final Cube cube = parent.getCubes().get(0);
            if (!rotations.containsKey(cube)) {
                continue;
            }

            model.setDefaultTransformation(MatrixUtil.getTransformation(rotations.get(cube), cube, 1F / scale));
            cube.getPivot().set(Position3V.zero());
            cube.getRotation().set(Position3V.zero());
            // cube.getPosition().set(Position3V.zero());
        }

        return models;
    }

    public static JavaItemModel geometryToItemModel(final String texture, final BedrockGeometryModel geometry, final boolean workaround) {
        final Position3V min = new Position3V(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), max = new Position3V(0, 0, 0);

        final List<Parent> parents = new ArrayList<>();
        for (final Parent old : geometry.getParents()) {
            final Parent parent = old.clone();

            for (Map.Entry<Integer, Cube> entry : parent.getCubes().entrySet()) {
                final Cube cube = entry.getValue();

                cube.inflate();
                cube.getPivot().setX(-cube.getPivot().getX());

                convertTo1Axis(cube);

                if (workaround) {
                    RotationUtil.doHackyRotationIfPossible(cube);
                }

                cube.fixRotationIfNeeded();
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

    public static int getAxis(float[] axes) {
        float largestAxes = 0;
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