package org.oryxel.cube.converter;

import com.viaversion.viaversion.util.Pair;
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
                    if (parent != null) {
                        boneRotation = ArrayUtil.combineArray(parent.rotation(), boneRotation);
                    }

                    if (parent.name().isEmpty()) {
                        break;
                    } else {
                        parent = bones.get(parent.parent());
                    }
                }
            }

            for (Cube cube : bone.cubes()) {
                double[] from = ArrayUtil.getArrayWithOffset(cube.origin());
                double[] to = ArrayUtil.combineArray(from, cube.size());
                double[] origin = ArrayUtil.combineArray(bone.pivot(), cube.pivot());
                double[] actualRotation = ArrayUtil.combineArray(cube.rotation(), boneRotation);

                int axisIndex = getAxis(actualRotation);
                float rawAngle = (float) actualRotation[axisIndex];

                double[] lastFrom = ArrayUtil.clone(from), lastTo = ArrayUtil.clone(to);
                double[] tempFrom = ArrayUtil.clone(from), tempTo = ArrayUtil.clone(to);
                tempTo = ArrayUtil.clamp(tempTo, 32, -16);
                tempFrom = ArrayUtil.clamp(tempFrom, 32, -16);
                tempTo[1] = MathUtil.clamp(lastTo[1], -16, 32);
                tempFrom[1] = MathUtil.clamp(lastFrom[1], -16, 32 - cube.size()[1]);

                if (ArrayUtil.isSmaller(from, minFrom)) {
                    minFrom = ArrayUtil.clone(from);
                } else if (ArrayUtil.isBigger(to, maxTo) && ArrayUtil.isBigger(to, minFrom)) {
                    maxTo = ArrayUtil.clone(to);
                }

                if (ArrayUtil.isAllCloseEnough(cube.rotation(), 0)) {
                    Element element = new Element(bone.name(), 0, rawAngle, "x", origin, cube.size(), cube.mirror());
                    element.parent(cube.parent());
                    element.from(from);
                    element.to(to);

                    Map<Direction, double[]> uv = new HashMap<>();
                    if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                        uv = UVUtil.portUv(perface.uvMap(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                    } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                        uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                    }
                    element.uvMap().putAll(uv);
                    if (ArrayUtil.isAllCloseEnough(boneRotation, 0)) {
                        rotation000.elements().add(element);
                    } else {
                        ItemModelData model;
                        double[] rotation = ArrayUtil.combineArray(cube.rotation(), boneRotation);
                        Map.Entry<Long, ItemModelData> entry = getModel(modelDataMap, ArrayUtil.pack(rotation));
                        if (entry == null) {
                            model = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
                            model.rotation(rotation);

                            modelDataMap.put(ArrayUtil.pack(rotation), model);
                        } else {
                            model = entry.getValue();
                        }

                        model.elements().add(element);
                    }
                } else {
                    ItemModelData model;
                    double[] rotation = ArrayUtil.combineArray(cube.rotation(), boneRotation);
                    Map.Entry<Long, ItemModelData> entry = getModel(modelDataMap, ArrayUtil.pack(rotation));
                    if (entry == null) {
                        model = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
                        model.rotation(rotation);

                        modelDataMap.put(ArrayUtil.pack(rotation), model);
                    } else {
                        model = entry.getValue();
                    }

                    Element element = new Element(bone.name(), 0, rawAngle, "x", origin, cube.size(), cube.mirror());
                    element.parent(cube.parent());
                    element.from(from);
                    element.to(to);
                    element.inflate(cube.inflate());

                    Map<Direction, double[]> uv = new HashMap<>();
                    if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                        uv = UVUtil.portUv(perface.uvMap(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                    } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                        uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                    }
                    element.uvMap().putAll(uv);

                    model.elements().add(element);
                }
            }
        }

        if (!rotation000.elements().isEmpty()) {
            list.add(rotation000);
        }

        for (Map.Entry<Long, ItemModelData> entry : modelDataMap.entrySet()) {
            list.add(entry.getValue());
        }

        double[] totalSize = ArrayUtil.size(maxTo, minFrom);

        double[] overlappedMin = ArrayUtil.getOverlapSize(minFrom, totalSize), overlappedMax =
                ArrayUtil.getOverlapSize(maxTo, totalSize);

        double[] totalOverlappedSize = ArrayUtil.combineArrayAbs(overlappedMin, overlappedMax);
        double[] maxSize = ArrayUtil.combineArrayAbs(totalOverlappedSize, new double[] { 48, 48, 48 });
        double maxOverlapSize = Math.max(maxSize[0], Math.max(maxSize[1], maxSize[2]));
        double scale = (1 / (maxOverlapSize / 48)) / 2;

        scale -= 0.02;

        for (ItemModelData model : list) {
            for (Element element : model.elements()) {
                for (int i = 0; i < 3; i++) {
                    element.from()[i] = (element.from()[i] - (i == 1 ? 0 : 8)) * scale;
                    element.from()[i] = element.from()[i] + (i == 1 ? 0 : 8);

                    element.to()[i] = element.from()[i] + (element.size()[i] * scale);

                    element.origin()[i] = element.origin()[i] * scale;
                    element.origin()[i] = element.origin()[i] + (i == 1 ? 0 : 8);
                }

                element.from(ArrayUtil.addOffsetToArray(element.from(), -element.inflate()));
                element.to(ArrayUtil.addOffsetToArray(element.to(), element.inflate()));
            }

            model.scale(scale);
        }

        return list;
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

    public static ItemModelData bedrockToJava(String texture, EntityGeometry geometry) {
        int childrenCount = 0;
        double[] cubeOffset = new double[3];
        final List<Group> groups = new ArrayList<>();
        final List<Element> elements = new ArrayList<>();
        for (Bone bone : geometry.bones()) {
            Group group = new Group(bone.name(), ArrayUtil.getArrayWithOffset(bone.pivot()), 0);

            for (Cube cube : bone.cubes()) {
                double[] from = ArrayUtil.getArrayWithOffset(cube.origin());
                double[] to = ArrayUtil.combineArray(from, cube.size());
                double[] origin = ArrayUtil.getArrayWithOffset(cube.pivot());

                int axisIndex = getAxis(cube.rotation());
                String axis = axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z";
                float rawAngle = (float) cube.rotation()[axisIndex];
                float axisAngle = (float) (Math.round(rawAngle / 22.5) * 22.5);
                float angle = MathUtil.clamp(axisAngle, -45, 45);

                from = ArrayUtil.addOffsetToArray(from, -cube.inflate());
                to = ArrayUtil.addOffsetToArray(to, cube.inflate());

                // overflow fix.
                double[] lastFrom = ArrayUtil.clone(from), lastTo = ArrayUtil.clone(to);
                to = ArrayUtil.clamp(to, 32, -16);
                from = ArrayUtil.clamp(from, 32, -16);
                to[1] = MathUtil.clamp(lastTo[1], -16, 32);
                from[1] = MathUtil.clamp(lastFrom[1], -16, 32 - cube.size()[1]);

                for (int i = 0; i < from.length; i++) {
                    double offset = from[i] - lastFrom[i];
                    if (offset == 0)
                        continue;

                    if (Math.abs(offset) > Math.abs(cubeOffset[i])) {
                        cubeOffset[i] = offset;
                    }

                    from[i] = lastFrom[i];
                    to[i] = lastTo[i];
                }

                Element element = new Element(bone.name(), angle, rawAngle, axis, origin, cube.size(), cube.mirror());
                element.from(from);
                element.to(to);

                Map<Direction, double[]> uv = new HashMap<>();
                if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                    uv = UVUtil.portUv(perface.uvMap(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                    uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                }
                element.uvMap().putAll(uv);

                group.children().put(childrenCount, element);
                elements.add(element);
                childrenCount++;
            }

            groups.add(group);
        }

        elements.forEach(element -> {
            for (int i = 0; i < cubeOffset.length; i++) {
                if (cubeOffset[i] == 0)
                    continue;

                element.to()[i] = element.to()[i] + cubeOffset[i];
                element.from()[i] = element.from()[i] + cubeOffset[i];
            }

            double[] lastFrom = ArrayUtil.clone(element.from()), lastTo = ArrayUtil.clone(element.to());
            element.to(ArrayUtil.clamp(element.to(), 32, -16));
            element.from(ArrayUtil.clamp(element.from(), 32, -16));
            element.to()[1] = MathUtil.clamp(lastTo[1], -16, 32);
            element.from()[1] = MathUtil.clamp(lastFrom[1], -16, 32 - element.size()[1]);
        });

        // TODO: implement children/parent group
        ItemModelData itemModelData = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
        itemModelData.groups().addAll(groups);
        itemModelData.elements().addAll(elements);
        itemModelData.positionOffset(cubeOffset);
        return itemModelData;
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
