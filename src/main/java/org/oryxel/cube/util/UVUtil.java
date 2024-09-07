package org.oryxel.cube.util;

import java.util.Arrays;
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
public class UVUtil {

    public static Map<Direction, double[]> portUv(Map<Direction, double[]> cubes, int tW, int tH, boolean boxUv) {
        Map<Direction, double[]> map = new HashMap<>();

        for (Map.Entry<Direction, double[]> entry : cubes.entrySet()) {
            double[] uv = entry.getValue();

            if (!boxUv) {
                uv = new double[] { entry.getValue()[0], entry.getValue()[1], entry.getValue()[0] + entry.getValue()[2], entry.getValue()[1] + entry.getValue()[3] };
            }

            for (int i = 0; i < uv.length; i++) {
                double n = uv[i];
                uv[i] = n * 16 / (i % 2 == 0 ? tW : tH);
            }

            map.put(entry.getKey(), uv);
        }

        return map;
    }

    public static double[] portToBoxUv(Map<Direction, double[]> cubes, double[] from, double[] to) {
        final Map<Direction, Face> faces = facesAsMap(from, to);

        double[] uv = new double[2];
        for (Map.Entry<Direction, double[]> entry : cubes.entrySet()) {
            Face face = faces.get(entry.getKey());
            if (face == null)
                continue;

            double[] entryUv = entry.getValue();
            uv = new double[] { entryUv[0] - face.from[0], entryUv[1] - face.from[1] };
        }

        return uv;
    }

    public static Map<Direction, double[]> rawPortUv(boolean mirror, double[] uvOffset, double[] from, double[] to) {
        final List<Face> faces = faces(from, to);

        if (mirror) {
            faces.forEach(face -> {
                face.from[0] = face.from[0] + face.size[0];
                face.size[0] = face.size[0] * -1;
            });
            Face index0Face = faces.get(0), index1Face = faces.get(1);
            double[] clonedFrom = ArrayUtil.cloneW2Index(index0Face.from),
                    clonedSize = ArrayUtil.cloneW2Index(index0Face.size);

            index0Face.from = index1Face.from;
            index0Face.size = index1Face.size;

            index1Face.from = clonedFrom;
            index1Face.size = clonedSize;
        }

        Map<Direction, double[]> map = new HashMap<>();
        for (Face face : faces) {
            double[] uv = new double[] { face.from[0] + uvOffset[0], face.from[1] + uvOffset[1], face.from[0] + face.size[0] + uvOffset[0], face.from[1] + face.size[1] + uvOffset[1] };
            map.put(face.direction, uv);
        }

        return map;
    }

    private static List<Face> faces(double[] from, double[] to) {
        double[] size = ArrayUtil.size(to, from);
        final List<Face> faces = Arrays.asList(
                new Face(Direction.DOWN, ArrayUtil.build(size[2] + size[0] * 2, 0), ArrayUtil.build(-size[0], size[2])),
                new Face(Direction.UP, ArrayUtil.build(size[2] + size[0], size[2]), ArrayUtil.build( -size[0], -size[2])),
                new Face(Direction.NORTH, ArrayUtil.build(size[2], size[2]), ArrayUtil.build(size[0], size[1])),
                new Face(Direction.SOUTH, ArrayUtil.build(size[2] * 2 + size[0], size[2]), ArrayUtil.build(size[0], size[1])),
                new Face(Direction.WEST, ArrayUtil.build(size[2] + size[0], size[2]), ArrayUtil.build(size[2], size[1])),
                new Face(Direction.EAST, ArrayUtil.build(0, size[2]), ArrayUtil.build(size[2], size[1]))
        );
        return faces;
    }

    private static Map<Direction, Face> facesAsMap(double[] from, double[] to) {
        double[] size = ArrayUtil.size(to, from);
        final Map<Direction, Face> faces = Map.of(
                Direction.DOWN, new Face(Direction.DOWN, ArrayUtil.build(size[2] + size[0] * 2, 0), ArrayUtil.build(-size[0], size[2])),
                Direction.UP, new Face(Direction.UP, ArrayUtil.build(size[2] + size[0], size[2]), ArrayUtil.build( -size[0], -size[2])),
                Direction.NORTH, new Face(Direction.NORTH, ArrayUtil.build(size[2], size[2]), ArrayUtil.build(size[0], size[1])),
                Direction.SOUTH, new Face(Direction.SOUTH, ArrayUtil.build(size[2] * 2 + size[0], size[2]), ArrayUtil.build(size[0], size[1])),
                Direction.WEST, new Face(Direction.WEST, ArrayUtil.build(size[2] + size[0], size[2]), ArrayUtil.build(size[2], size[1])),
                Direction.EAST, new Face(Direction.EAST, ArrayUtil.build(0, size[2]), ArrayUtil.build(size[2], size[1]))
        );
        return faces;
    }

    private static class Face {
        private final Direction direction;
        private double[] from, size;

        private Face(Direction direction, double[] from, double[] size) {
            this.direction = direction;
            this.from = from;
            this.size = size;
        }
    }

    public static double size(int axis, double[] to, double[] from) {
        return to[axis] - from[axis];
    }

}
