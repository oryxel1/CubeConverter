package org.cube.converter.util.legacy;

import com.google.gson.JsonArray;
import org.cube.converter.util.math.MathUtil;
public class ArrayUtil {

    private ArrayUtil() {}

    public static float[] build(float d, float d1) {
        return new float[] { d, d1 };
    }

    public static float[] compileAngle(float[] d) {
        return new float[] { -d[0], -d[1], d[2] };
    }

    public static float[] clone(float[] d) {
        return new float[] { d[0], d[1], d[2] };
    }

    public static float[] cloneW2Index(float[] d) {
        return new float[] { d[0], d[1] };
    }

    public static float[] size(float[] to, float[] from) {
        float x = to[0] - from[0], y = to[1] - from[1], z = to[2] - from[2];
        return new float[] { x, y, z };
    }

    public static float[] sizeAbs(float[] to, float[] from) {
        float x = Math.abs(to[0] - from[0]), y = Math.abs(to[1] - from[1]), z = Math.abs(to[2] - from[2]);
        return new float[] { x, y, z };
    }

    public static float[] toArray(JsonArray array) {
        if (array == null)
            return new float[] { 0F, 0f, 0F };

        return new float[] { array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat() };
    }

    public static float[] javaOffsetArray(float[] array) {
        return new float[] { array[0] + 8, array[1], array[2] + 8 };
    }

    public static float[] add(float[] array, float[] array1) {
        return new float[] { array[0] + array1[0], array[1] + array1[1], array[2] + array1[2] };
    }

    public static float[] minus(float[] array, float[] array1) {
        return new float[] { array[0] - array1[0], array[1] - array1[1], array[2] - array1[2] };
    }

    public static boolean isAllEmpty(float[] array) {
        for (float d : array) {
            if (Math.abs(d) != 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isOneNotEmpty(float[] array) {
        int notEmpties = 0;
        for (float d : array) {
            if (Math.abs(d) != 0) {
                notEmpties++;
            }
        }

        return notEmpties == 1 || notEmpties == 0;
    }

    public static float[] getFrom(float[] origin, float[] size) {
        float[] d = ArrayUtil.clone(origin);
        d[0] = -(d[0] + size[0]);
        d = ArrayUtil.javaOffsetArray(d);

        return d;
    }

    public static float[] getOverlap(float[] box) {
        float[] overlap = new float[3];
        float[] cloned = ArrayUtil.clone(box);
        cloned[0] = MathUtil.clamp(cloned[0], -16, 32);
        cloned[1] = MathUtil.clamp(cloned[1], -16, 32);
        cloned[2] = MathUtil.clamp(cloned[2], -16, 32);

        for (int i = 0; i < box.length; i++) {
            overlap[i] = Math.abs(box[i] - cloned[i]);
        }

        return overlap;
    }

}
