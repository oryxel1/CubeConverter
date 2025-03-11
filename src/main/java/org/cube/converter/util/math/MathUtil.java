package org.cube.converter.util.math;

import org.cube.converter.util.element.Position3V;

public class MathUtil {
    private MathUtil() {}

    public static boolean isValidRotation(final Position3V position3V) {
        double addUp = Math.abs(position3V.getX() + position3V.getY() + position3V.getZ());
        return addUp == 22.5 || addUp == 45 || addUp == 0;
    }

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
