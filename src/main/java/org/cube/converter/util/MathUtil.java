package org.cube.converter.util;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2025-2026 Oryxel and contributors
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
public class MathUtil {
    private MathUtil() {}

    public static boolean isValidJavaAngle(double rawAngle) {
        return rawAngle % 22.5 == 0D && rawAngle >= -45 && rawAngle <= 45;
    }

    public static double limitAngle(double rawAngle) {
        return MathUtil.clamp(Math.round(rawAngle / 22.5) * 22.5, -45, 45);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }
}
