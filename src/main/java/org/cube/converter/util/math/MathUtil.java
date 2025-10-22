package org.cube.converter.util.math;

import org.cube.converter.util.element.Position3V;

public class MathUtil {
    private MathUtil() {}

    public static boolean isValidRotation(final Position3V position3V) {
        float addUp = Math.abs(position3V.getX() + position3V.getY() + position3V.getZ());
        return addUp == 22.5 || addUp == 45 || addUp == 0;
    }

    public static boolean isValidJavaAngle(float rawAngle) {
        return MathUtil.limitAngle(rawAngle) == rawAngle;
    }

    public static float limitAngle(float rawAngle) {
        return MathUtil.clamp(toHackyAngle(rawAngle), -45, 45);
    }

    public static float toHackyAngle(float rawAngle) {
        return Math.round(rawAngle / 22.5F) * 22.5F;
    }

    public static boolean canDoHacky(float angle) {
        return Math.abs(angle) % 22.5 != 0D || Math.abs(MathUtil.toHackyAngle(angle) - angle) < Math.abs(MathUtil.limitAngle(angle) - angle);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static boolean closeEnoughAbs(float angle, float target) {
        return Math.abs(Math.abs(angle) - Math.abs(target)) < 5;
    }
}
