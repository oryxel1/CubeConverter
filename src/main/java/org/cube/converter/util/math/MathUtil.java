package org.cube.converter.util.math;

import org.cube.converter.util.element.Position3V;

public class MathUtil {
    private MathUtil() {}

    public static boolean isValidRotation(final Position3V position3V) {
        float addUp = Math.abs(position3V.getX() + position3V.getY() + position3V.getZ());
        return addUp == 22.5 || addUp == 45 || addUp == 0;
    }

    public static boolean isValidJavaAngle(float rawAngle) {
        return rawAngle % 22.5 == 0D && rawAngle >= -45 && rawAngle <= 45;
    }

    public static float limitAngle(float rawAngle) {
        return MathUtil.clamp(Math.round(rawAngle / 22.5F) * 22.5F, -45, 45);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }
}
