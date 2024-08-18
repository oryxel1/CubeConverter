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
        List<ItemModelData> list = new ArrayList<>();

        ItemModelData rotation000 = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());

        double[] minFrom = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE },
                    maxTo = new double[3];
        final Map<Long, ItemModelData> modelDataMap = new HashMap<>();
        for (Bone bone : geometry.bones()) {
//            final Map<Long, Pair<Double, ItemModelData>> overlapped = new HashMap<>();

            for (Cube cube : bone.cubes()) {
                double[] from = ArrayUtil.getArrayWithOffset(cube.origin());
                double[] to = ArrayUtil.combineArray(from, cube.size());
                double[] origin = ArrayUtil.getArrayWithOffset(cube.pivot());
                double[] actualRotation = ArrayUtil.combineArray(cube.rotation(), bone.rotation());

                int axisIndex = getAxis(actualRotation);
                float rawAngle = (float) actualRotation[axisIndex];
                String axis = axisIndex == 0 ? "x" : axisIndex == 1 ? "y" : "z";
                float axisAngle = (float) (Math.round(rawAngle / 22.5) * 22.5);
                float angle = MathUtil.clamp(axisAngle, -45, 45);

                double angleDiff = Math.abs(rawAngle - angle);

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

                if (!ArrayUtil.isAllCloseEnough(lastFrom, tempFrom, 2)) {
                    // This comment code is for in case you want to split it to multiple entity
//                    double diffToX = lastTo[0] == 0 ? 1 : tempTo[0] / lastTo[0],
//                            diffToY = lastTo[1] == 0 ? 1 : tempTo[1] / lastTo[1],
//                            diffToZ = lastTo[2] == 0 ? 1 : tempTo[2] / lastTo[2];
//                    double diffFromX = lastFrom[0] == 0 ? 1 : tempFrom[0] / lastFrom[0],
//                            diffFromY = lastFrom[1] == 0 ? 1 : tempFrom[1] / lastFrom[1],
//                            diffFromZ = lastFrom[2] == 0 ? 1 : tempFrom[2] / lastFrom[2];
//
//                    double[] from = ArrayUtil.multiply(from, new double[] { diffFromX, diffFromY, diffFromZ });
//                    double[] to = ArrayUtil.multiply(to, new double[] { diffToX, diffToY, diffToZ });
                } else {
                    // close enough, just clamp it!
                    to = tempTo;
                    from = tempFrom;
                }

                // the angle is close enough, ignore!
                if (ArrayUtil.isCloseEnough(cube.rotation(), 0, axisIndex) && angleDiff <= 5) {
                    Element element = new Element(bone.name(), angle, rawAngle, axis, origin, cube.size(), cube.mirror());
                    element.from(from);
                    element.to(to);

                    Map<Direction, double[]> uv = new HashMap<>();
                    if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                        uv = UVUtil.portUv(perface.uvMap(), lastFrom, lastTo, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                    } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                        uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), lastFrom, lastTo, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                    }
                    element.uvMap().putAll(uv);

                    rotation000.elements().add(element);
                    continue;
                }

                // if it's small enough then ignore!
                if (ArrayUtil.pack(cube.size()) <= 10) {
                    Element element = new Element(bone.name(), 0, rawAngle, "x", origin, cube.size(), cube.mirror());
                    element.from(from);
                    element.to(to);

                    Map<Direction, double[]> uv = new HashMap<>();
                    if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                        uv = UVUtil.portUv(perface.uvMap(), lastFrom, lastTo, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                    } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                        uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), lastFrom, lastTo, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                    }
                    element.uvMap().putAll(uv);

                    rotation000.elements().add(element);
                    continue;
                }

                if (ArrayUtil.isAllCloseEnough(cube.rotation(), 0)) {
                    Element element = new Element(bone.name(), 0, rawAngle, "x", origin, cube.size(), cube.mirror());
                    element.from(from);
                    element.to(to);

                    Map<Direction, double[]> uv = new HashMap<>();
                    if (cube instanceof Cube.PerFaceCube perface && !perface.uvMap().isEmpty()) {
                        uv = UVUtil.portUv(perface.uvMap(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight(), false);
                    } else if (cube instanceof Cube.BoxCube boxCube && boxCube.uvOffset() != null) {
                        uv = UVUtil.portUv(element.mirror(), boxCube.uvOffset(), from, to, rawAngle, geometry.textureWidth(), geometry.textureHeight());
                    }
                    element.uvMap().putAll(uv);
                    if (ArrayUtil.isAllCloseEnough(bone.rotation(), 0)) {
                        rotation000.elements().add(element);
                    } else {
                        ItemModelData model;
                        double[] rotation = ArrayUtil.combineArray(cube.rotation(), bone.rotation());
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
                    double[] rotation = ArrayUtil.combineArray(cube.rotation(), bone.rotation());
                    Map.Entry<Long, ItemModelData> entry = getModel(modelDataMap, ArrayUtil.pack(rotation));
                    if (entry == null) {
                        model = new ItemModelData(texture, geometry.textureWidth(), geometry.textureHeight());
                        model.rotation(rotation);

                        modelDataMap.put(ArrayUtil.pack(rotation), model);
                    } else {
                        model = entry.getValue();
                    }

                    Element element = new Element(bone.name(), 0, rawAngle, "x", origin, cube.size(), cube.mirror());
                    element.from(from);
                    element.to(to);

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
        double[] scaled = ArrayUtil.divide(totalSize, new double[] { 48, 48, 48 });
        scaled = ArrayUtil.max(scaled, 1);
        double scale = ArrayUtil.smallest(scaled);

        for (ItemModelData model : list) {
            for (Element element : model.elements()) {
                element.from(ArrayUtil.multiply(element.from(), scale));
                element.to(ArrayUtil.multiply(element.to(), scale));
            }

            model.scale(scale);
        }

        return list;
    }

    public static Map.Entry<Long, ItemModelData> getModel(Map<Long, ItemModelData> map, long l) {
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
