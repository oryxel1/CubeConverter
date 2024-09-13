package org.oryxel.cube.converter;

import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.bedrock.model.Bone;
import org.oryxel.cube.model.bedrock.model.Cube;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.model.java.other.Element;
import org.oryxel.cube.model.java.other.Group;
import org.oryxel.cube.util.*;

import java.util.*;

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
public class FormatConverter {

    public static List<ItemModelData> bedrockToJavaModels(String texture, BedrockGeometry geometry) {
        double[] minFrom = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE }, maxTo = new double[3];
        final ItemModelData main = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());

        final Map<String, Bone> bones = new HashMap<>();

        for (Bone bone : geometry.bones()) {
            bones.put(bone.name(), bone);
        }

        final List<ItemModelData> models = new ArrayList<>();
        for (Bone bone : geometry.bones()) {
            final Map<double[], double[]> pivotRotation = new HashMap<>();
            pivotRotation.put(ArrayUtil.clone(bone.pivot()), ArrayUtil.clone(bone.rotation()));
            Bone parent = bones.get(bone.parent());
            while (parent != null) {
                pivotRotation.put(ArrayUtil.clone(parent.pivot()), ArrayUtil.clone(parent.rotation()));
                parent = bones.get(parent.parent());
            }
            double[] combinedRotation = new double[3];
//            for (Map.Entry<double[], double[]> entry : pivotRotation.entrySet()) {
//                combinedRotation = ArrayUtil.combineArray(combinedRotation, ArrayUtil.clone(entry.getValue()));
//            }

            for (Cube cube : bone.cubes()) {
                double[] from = ArrayUtil.getFrom(cube.origin(), cube.size());
                double[] to = ArrayUtil.add(from, cube.size());
                double[] origin = ArrayUtil.clone(cube.pivot());

                if (ArrayUtil.isAllEmpty(combinedRotation) && ArrayUtil.isOneNotEmpty(cube.rotation()) && MathUtil.isValidJavaAngle(cube.rotation()[getAxis(cube.rotation())])) {
                    int axisIndex = getAxis(cube.rotation());
                    float angle = MathUtil.clampToJavaAngle(cube.rotation()[axisIndex]);

                    Element element = new Element(geometry, cube, bone.name(), axisIndex != 2 ? -angle : angle, axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z", origin, from, to);
                    main.elements().add(element);
                } else {
                    // Have to create one for each so prepareForRotation works properly
                    // TODO: find a better solution?
                    ItemModelData model = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
                    model.rotation(ArrayUtil.add(combinedRotation, cube.rotation()));
                    model.elements().add(new Element(geometry, cube, bone.name(), 0, "x", origin, from, to));
                    models.add(model);
                }

                if (from[0] < minFrom[0]) minFrom[0] = from[0];
                if (from[1] < minFrom[1]) minFrom[1] = from[1];
                if (from[2] < minFrom[2]) minFrom[2] = from[2];
                if (to[0] > maxTo[0]) maxTo[0] = to[0];
                if (to[1] > maxTo[1]) maxTo[1] = to[1];
                if (to[2] > maxTo[2]) maxTo[2] = to[2];
            }
        }

        for (ItemModelData model : models) {
            prepareForRotation(model);
        }

        if (!main.elements().isEmpty()) {
            models.add(main);
        }

        double scale = getScalingSize(minFrom, maxTo);
        for (ItemModelData model : models) {
            scaleEverything(model.elements(), scale);
            model.scale(1 / scale);
            model.offset(new double[] { model.offset()[0] * scale, model.offset()[0] * scale, model.offset()[0] * scale });
        }

        return Collections.unmodifiableList(models);
    }

    private static void prepareForRotation(ItemModelData model) {
        // should only be 1 here, loop through it anyway.
        for (Element element : model.elements()) {
            double[] before = ArrayUtil.clone(element.from());
            element.from(new double[] {
                    8 - (element.size()[0] / 2),
                    8 - (element.size()[1] / 2),
                    8 - (element.size()[2] / 2)
            });
            element.to(ArrayUtil.add(element.from(), element.size()));

            model.offset(ArrayUtil.minus(before, ArrayUtil.minus(element.from(), before)));
        }
    }

    public static ItemModelData bedrockToJava(String texture, BedrockGeometry geometry) {
        int childrenCount = 0;
        final List<Group> groups = new ArrayList<>();
        final List<Element> elements = new ArrayList<>();

        double[] minFrom = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE },
                maxTo = new double[3];

        for (Bone bone : geometry.bones()) {
            Group group = new Group(bone.name(), ArrayUtil.javaOffsetArray(bone.pivot()), 0);

            for (Cube cube : bone.cubes()) {
                double[] from = ArrayUtil.getFrom(cube.origin(), cube.size());
                double[] to = ArrayUtil.add(from, cube.size());
                double[] origin = ArrayUtil.clone(cube.pivot());

                // from = ArrayUtil.addOffsetToArray(from, -cube.inflate());
                // to = ArrayUtil.addOffsetToArray(to, cube.inflate());

                int axisIndex = getAxis(cube.rotation());
                String axis = axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z";
                float rawAngle = (float) cube.rotation()[axisIndex];
                float angle = Math.abs(rawAngle) == 180 ? 0 : MathUtil.clampToJavaAngle(rawAngle);
                if (axisIndex != 2)
                    angle = -angle;

                Element element = new Element(geometry, cube, bone.name(), angle, axis, origin, from, to);
                elements.add(element);

                if (element.from()[0] < minFrom[0]) minFrom[0] = element.from()[0];
                if (element.from()[1] < minFrom[1]) minFrom[1] = element.from()[1];
                if (element.from()[2] < minFrom[2]) minFrom[2] = element.from()[2];
                if (element.to()[0] > maxTo[0]) maxTo[0] = to[0];
                if (element.to()[1] > maxTo[1]) maxTo[1] = to[1];
                if (element.to()[2] > maxTo[2]) maxTo[2] = to[2];

                group.children().put(childrenCount, element);
                childrenCount++;
            }

            groups.add(group);
        }

        double[] size = ArrayUtil.sizeAbs(maxTo, minFrom);
        double[] gameSize = new double[] {
                (size[0] + size[2]) / 2,
                size[1]
        };

        double scale = getScalingSize(minFrom, maxTo);
        ItemModelData itemModelData = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
        itemModelData.groups().addAll(groups);
        itemModelData.elements().addAll(elements);
        itemModelData.scale(1 / scale);
        itemModelData.size(gameSize);

        scaleEverything(itemModelData.elements(), scale);

        return itemModelData;
    }

    private static void scaleEverything(List<Element> elements, double scale) {
        for (Element element : elements) {
            for (int i = 0; i < 3; i++) {
                element.from()[i] = (element.from()[i] - (i == 1 ? 0 : 8)) * scale;
                element.from()[i] = element.from()[i] + (i == 1 ? 0 : 8);

                element.to()[i] = element.from()[i] + (element.size()[i] * scale);

                element.origin()[i] = element.origin()[i] * scale;
                element.origin()[i] = element.origin()[i] + (i == 1 ? 0 : 8);
            }
        }
    }

    private static double getScalingSize(double[] minFrom, double[] maxTo) {
        double[] overlapFrom = ArrayUtil.getOverlap(minFrom), overlapTo = ArrayUtil.getOverlap(maxTo);
        double[] totalOverlap = ArrayUtil.add(overlapFrom, overlapTo);
        double maxSize = Math.max(totalOverlap[1], totalOverlap[0] + totalOverlap[2]);
        double divideValue = 32;
        if ((totalOverlap[1] == maxSize && minFrom[1] < 0 || totalOverlap[1] != maxSize && (minFrom[0] < 0 || minFrom[2] < 0))) {
            divideValue = 16;
        }

        return maxSize == 0 ? 1 : Math.min(1, divideValue / (maxSize + 48));
    }

    // TODO: better implement than this...
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
