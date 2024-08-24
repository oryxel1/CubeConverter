package org.oryxel.cube.converter;

import org.oryxel.cube.converter.enums.OverflowFixType;
import org.oryxel.cube.model.bedrock.EntityGeometry;
import org.oryxel.cube.model.bedrock.other.Bone;
import org.oryxel.cube.model.bedrock.other.Cube;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.model.java.other.Element;
import org.oryxel.cube.model.java.other.Group;
import org.oryxel.cube.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class FormatConverter {

    public static ItemModelData bedrockToJava(String texture, EntityGeometry geometry, OverflowFixType type) {
        int childrenCount = 0;
        double[] cubeOffset = new double[3];
        final List<Group> groups = new ArrayList<>();
        final List<Element> elements = new ArrayList<>();

        double[] minFrom = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE },
                maxTo = new double[3];

        for (Bone bone : geometry.bones()) {
            Group group = new Group(bone.name(), ArrayUtil.getArrayWithOffset(bone.pivot()), 0);

            for (Cube cube : bone.cubes()) {
                double[] from = getFrom(cube.origin(), cube.size());
                double[] to = ArrayUtil.combineArray(from, cube.size());
                double[] origin = ArrayUtil.getArrayWithOffset(cube.pivot());

                int axisIndex = getAxis(cube.rotation());
                String axis = axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z";
                float rawAngle = (float) cube.rotation()[axisIndex];
                float angle = MathUtil.clampToJavaAngle(rawAngle);

                from = ArrayUtil.addOffsetToArray(from, -cube.inflate());
                to = ArrayUtil.addOffsetToArray(to, cube.inflate());

                // overflow fix.
                if (type == OverflowFixType.CLAMP) {
                    double[] clonedFrom = ArrayUtil.clampToMax(from, cube.size(), 0);

                    for (int i = 0; i < 3; i++) {
                        double offset = clonedFrom[i] - from[i];
                        if (Math.abs(offset) > Math.abs(cubeOffset[i]))
                            cubeOffset[i] = offset;
                    }
                } else {
                    if (ArrayUtil.isSmaller(from, minFrom))
                        minFrom = ArrayUtil.clone(from);
                    else if (ArrayUtil.isBigger(to, maxTo) && ArrayUtil.isBigger(to, minFrom))
                        maxTo = ArrayUtil.clone(to);
                }

                Element element = new Element(geometry, cube, bone.name(), angle, rawAngle, axis, origin, from, to);
                elements.add(element);

                group.children().put(childrenCount, element);
                childrenCount++;
            }

            groups.add(group);
        }

        double scale = getScalingSize(minFrom, maxTo);
        ItemModelData itemModelData = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
        itemModelData.scale(scale);

        if (type == OverflowFixType.SCALING) {
            scaleEverything(itemModelData.elements(), scale);
        } else {
            elements.forEach(element -> {
                element.from(ArrayUtil.combineArray(element.from(), cubeOffset));
                element.to(ArrayUtil.combineArray(element.to(), cubeOffset));

                ArrayUtil.clampToMax(element.from(), element.size(), 0);
                ArrayUtil.clampToMax(element.to(), element.size(), 1);
            });
        }

        itemModelData.groups().addAll(groups);
        itemModelData.elements().addAll(elements);
        itemModelData.positionOffset(cubeOffset);
        return itemModelData;
    }

    public static List<ItemModelData> bedrockToJavaModels(String texture, EntityGeometry geometry) {
        final List<ItemModelData> list = new ArrayList<>();

        ItemModelData rotation000 = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());

        double[] minFrom = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE },
                    maxTo = new double[3];
        final Map<Long, ItemModelData> modelDataMap = new HashMap<>();
        final Map<String, Bone> bones = new HashMap<>();

        for (Bone bone : geometry.bones()) {
            bones.put(bone.name(), bone);
        }

        for (Bone bone : geometry.bones()) {
            double[] boneRotation = bone.rotation();
            if (!bone.parent().isEmpty()) {
                Bone parent = bones.get(bone.parent());
                while (parent != null) {
                    if (parent != null)
                        boneRotation = ArrayUtil.combineArray(parent.rotation(), boneRotation);

                    if (parent.parent().isEmpty()) {
                        break;
                    } else {
                        parent = bones.get(parent.parent());
                    }
                }
            }

            for (Cube cube : bone.cubes()) {
                double[] from = getFrom(cube.origin(), cube.size());
                double[] to = ArrayUtil.combineArray(from, cube.size());
                double[] origin = ArrayUtil.combineArray(bone.pivot(), cube.pivot());
                double[] rotation = ArrayUtil.combineArray(cube.rotation(), boneRotation);

                from = ArrayUtil.addOffsetToArray(from, -cube.inflate());
                to = ArrayUtil.addOffsetToArray(to, -cube.inflate());

                int axisIndex = getAxis(rotation);
                float rawAngle = (float) rotation[axisIndex];

                if (ArrayUtil.isSmaller(from, minFrom)) {
                    minFrom = ArrayUtil.clone(from);
                } else if (ArrayUtil.isBigger(to, maxTo) && ArrayUtil.isBigger(to, minFrom)) {
                    maxTo = ArrayUtil.clone(to);
                }

                Element element = new Element(geometry, cube, bone.name(), 0, rawAngle, "x", origin, from, to);
                element.parent(cube.parent());

                if (ArrayUtil.isAllCloseEnough(rotation, 0)) {
                    rotation000.elements().add(element);
                } else {
                    ItemModelData model = putIfNotExist(modelDataMap, texture, geometry, rotation);
                    model.elements().add(element);
                }
            }
        }

        if (!rotation000.elements().isEmpty())
            list.add(rotation000);

        for (Map.Entry<Long, ItemModelData> entry : modelDataMap.entrySet())
            list.add(entry.getValue());

        double scale = getScalingSize(minFrom, maxTo);

        for (ItemModelData model : list) {
            scaleEverything(model.elements(), scale);
            model.scale(scale);
        }

        return list;
    }

    private static ItemModelData putIfNotExist(Map<Long, ItemModelData> map, String texture, EntityGeometry geometry, double[] rotation) {
        ItemModelData model;
        Map.Entry<Long, ItemModelData> entry = getModel(map, ArrayUtil.pack(rotation));
        if (entry == null) {
            model = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
            model.rotation(rotation);

            map.put(ArrayUtil.pack(rotation), model);
        } else {
            model = entry.getValue();
        }

        return model;
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

    private static Map.Entry<Long, ItemModelData> getModel(Map<Long, ItemModelData> map, long l) {
        for (Map.Entry<Long, ItemModelData> entry : map.entrySet()) {
            long diff = Math.abs(entry.getKey() - l);
            if (diff < 12) {
                return entry;
            }
        }

        return null;
    }

    private static double getScalingSize(double[] minFrom, double[] maxTo) {
        double[] totalSize = ArrayUtil.size(maxTo, minFrom);
        double[] overlappedMin = ArrayUtil.getOverlapSize(minFrom, totalSize), overlappedMax =
                ArrayUtil.getOverlapSize(maxTo, totalSize);

        double[] totalOverlappedSize = ArrayUtil.combineArrayAbs(overlappedMin, overlappedMax);
        double[] maxSize = ArrayUtil.combineArrayAbs(totalOverlappedSize, new double[] { 48, 48, 48 });
        double maxOverlapSize = Math.max(maxSize[0], Math.max(maxSize[1], maxSize[2]));
        double scale = (1 / (maxOverlapSize / 48)) / 2;

        scale -= 0.02;

        return scale;
    }

    private static double[] getFrom(double[] origin, double[] size) {
        double[] d = ArrayUtil.clone(origin);
        d[0] = -(d[0] + size[0]);
        d = ArrayUtil.getArrayWithOffset(d);

        return d;
    }

    // TODO: better implement than this...
    private static int getAxis(double[] axes) {
        for (int i = 0; i < axes.length; i++) {
            if (axes[i] != 0) {
                return i;
            }
        }

        return 0;
    }

}
