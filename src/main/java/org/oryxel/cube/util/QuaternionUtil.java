package org.oryxel.cube.util;

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
public class QuaternionUtil {

    public static double[] toQuaternion(double x, double y, double z, boolean nonRadius) {
        if (nonRadius) {
            x = Math.toRadians(x);
            y = Math.toRadians(y);
            z = Math.toRadians(z);
        }

        double cx = Math.cos(x / 2);
        double cy = Math.cos(y / 2);
        double cz = Math.cos(z / 2);
        double sx = Math.sin(x / 2);
        double sy = Math.sin(y / 2);
        double sz = Math.sin(z / 2);

        float qw = (float) (cx * cy * cz + sx * sy * sz);
        float qx = (float) (sx * cy * cz - cx * sy * sz);
        float qy = (float) (cx * sy * cz + sx * cy * sz);
        float qz = (float) (cx * cy * sz - sx * sy * cz);

        return new double[] { qx, qy, qz, qw };
    }

}
