package org.cube.converter.util.legacy;

import com.google.gson.JsonArray;
import org.cube.converter.util.math.MathUtil;
public class ArrayUtil {

    private ArrayUtil() {}

    public static double[] build(double d, double d1) {
        return new double[] { d, d1 };
    }

    public static double[] compileAngle(double[] d) {
        return new double[] { -d[0], -d[1], d[2] };
    }

    public static double[] clone(double[] d) {
        return new double[] { d[0], d[1], d[2] };
    }

    public static double[] cloneW2Index(double[] d) {
        return new double[] { d[0], d[1] };
    }

    public static double[] size(double[] to, double[] from) {
        double x = to[0] - from[0], y = to[1] - from[1], z = to[2] - from[2];
        return new double[] { x, y, z };
    }

    public static double[] sizeAbs(double[] to, double[] from) {
        double x = Math.abs(to[0] - from[0]), y = Math.abs(to[1] - from[1]), z = Math.abs(to[2] - from[2]);
        return new double[] { x, y, z };
    }

    public static double[] toArray(JsonArray array) {
        if (array == null)
            return new double[] { 0D, 0D, 0D };

        return new double[] { array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble() };
    }

    public static double[] javaOffsetArray(double[] array) {
        return new double[] { array[0] + 8, array[1], array[2] + 8 };
    }

    public static double[] add(double[] array, double[] array1) {
        return new double[] { array[0] + array1[0], array[1] + array1[1], array[2] + array1[2] };
    }

    public static double[] minus(double[] array, double[] array1) {
        return new double[] { array[0] - array1[0], array[1] - array1[1], array[2] - array1[2] };
    }

    public static boolean isAllEmpty(double[] array) {
        for (double d : array) {
            if (Math.abs(d) != 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isOneNotEmpty(double[] array) {
        int notEmpties = 0;
        for (double d : array) {
            if (Math.abs(d) != 0) {
                notEmpties++;
            }
        }

        return notEmpties == 1 || notEmpties == 0;
    }

    public static double[] getFrom(double[] origin, double[] size) {
        double[] d = ArrayUtil.clone(origin);
        d[0] = -(d[0] + size[0]);
        d = ArrayUtil.javaOffsetArray(d);

        return d;
    }

    public static double[] getOverlap(double[] box) {
        double[] overlap = new double[3];
        double[] cloned = ArrayUtil.clone(box);
        cloned[0] = MathUtil.clamp(cloned[0], -16, 32);
        cloned[1] = MathUtil.clamp(cloned[1], -16, 32);
        cloned[2] = MathUtil.clamp(cloned[2], -16, 32);

        for (int i = 0; i < box.length; i++) {
            overlap[i] = Math.abs(box[i] - cloned[i]);
        }

        return overlap;
    }

}
