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

    public static Map<Direction, double[]> portUv(Map<Direction, double[]> cubes, double[] from, double[] to, double rawAngle, int tW, int tH,
                                                   boolean boxUv) {
        Map<Direction, double[]> map = new HashMap<>();

        for (Map.Entry<Direction, double[]> entry : cubes.entrySet()) {
            double[] uv = entry.getValue();

            if (!boxUv) {
                switch (entry.getKey()) {
                    case NORTH, SOUTH -> uv = autoUV(entry.getValue(), new double[] { size(0, to, from), size(1, to, from) }, rawAngle, tW, tH );
                    case EAST, WEST -> uv = autoUV(entry.getValue(), new double[] { size(2, to, from), size(1, to, from) }, rawAngle, tW, tH );
                    case UP, DOWN -> uv = autoUV(entry.getValue(), new double[] { size(0, to, from), size(2, to, from) }, rawAngle, tW, tH );
                }

                uv = new double[] { uv[0], uv[1], uv[0] + entry.getValue()[2], uv[1] + entry.getValue()[3] };
            }

            for (int i = 0; i < uv.length; i++) {
                double n = uv[i];
                uv[i] = n * 16 / (i % 2 == 0 ? tW : tH);
            }

            map.put(entry.getKey(), uv);
        }

        return map;
    }

    public static Map<Direction, double[]> portUv(boolean mirror, double[] uvOffset, double[] from, double[] to, double rawAngle, int tW, int tH) {
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

        return portUv(map, from, to, rawAngle, tW, tH, true);
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

    private static double[] autoUV(double[] uv, double[] size, double rotation, int tW, int tH) {
        size[0] = Math.abs(size[0]);
        size[1] = Math.abs(size[1]);
        double sx = uv[0];
        double sy = uv[1];

        if (rotation == 90 || rotation == 270) {
            size[0] = -size[0];
            size[1] = -size[1];
        }

        size[0] = MathUtil.clamp(size[0], -tW, tW);
        size[1] = MathUtil.clamp(size[1], -tH, tH);

        var x = sx + size[0];
        var y = sy + size[1];

        if (x > tW) {
            sx = tW - (x - sx);
            x = tH;
        }

        if (y > tH) {
            sy = tH - (y - sy);
            y = tH;
        }
        if (sx < 0) sx = 0;
        if (sy < 0) sy = 0;
        if (x < sx) x = sx;
        if (y < sy) y = sy;
        return new double[] {sx, sy, x, y};
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
