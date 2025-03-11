package org.cube.converter.util.math;
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
