package org.cube.converter.util.math;

import org.cube.converter.util.element.Position3V;

public class MathUtil {
    private MathUtil() {}

    public static boolean isValidRotation(final Position3V position3V) {
        float addUp = Math.abs(position3V.getX() + position3V.getY() + position3V.getZ());
        return addUp == 22.5 || addUp == 45 || addUp == 0;
    }

    public static boolean isValidJavaAngle(float rawAngle, boolean old) {
        return MathUtil.limitAngle(rawAngle, old) == rawAngle;
    }

    public static float limitAngle(float rawAngle, boolean old) {
        return MathUtil.clamp(old ? toHackyAngle(rawAngle) : rawAngle, -45, 45);
    }

    public static float toHackyAngle(float rawAngle) {
        return Math.round(rawAngle / 22.5F) * 22.5F;
    }

    public static boolean canDoHacky(float angle, boolean old) {
        if (old) {
            return Math.abs(angle) % 22.5 != 0D || Math.abs(MathUtil.toHackyAngle(angle) - angle) < Math.abs(MathUtil.limitAngle(angle, false) - angle);
        }

        if (MathUtil.isValidJavaAngle(angle, false)) {
            return false;
        }

        float absAngle = Math.abs(angle);
        float newAngle;
        if (absAngle > 90) {
            newAngle = absAngle - 90;
        } else {
            newAngle = 90 - absAngle;
        }
        return MathUtil.isValidJavaAngle(newAngle, false);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static boolean closeEnoughAbs(float angle, float target) {
        return Math.abs(Math.abs(angle) - Math.abs(target)) < 5;
    }
}
