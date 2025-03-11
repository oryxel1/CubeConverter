package org.cube.converter.util.math.matrix;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public record GivensPair(float sinHalf, float cosHalf) {
    public static GivensPair normalize(float a, float b) {
        float h = Math.invsqrt(a * a + b * b);
        return new GivensPair(h * a, h * b);
    }

    public static GivensPair fromAngle(float radians) {
        float g = Math.sin(radians / 2.0f);
        float h = Math.cosFromSin(g, radians / 2.0f);
        return new GivensPair(g, h);
    }

    public GivensPair negateSin() {
        return new GivensPair(-this.sinHalf, this.cosHalf);
    }

    public Quaternionf setXRotation(Quaternionf quaternionf) {
        return quaternionf.set(this.sinHalf, 0.0f, 0.0f, this.cosHalf);
    }

    public Quaternionf setYRotation(Quaternionf quaternionf) {
        return quaternionf.set(0.0f, this.sinHalf, 0.0f, this.cosHalf);
    }

    public Quaternionf setZRotation(Quaternionf quaternionf) {
        return quaternionf.set(0.0f, 0.0f, this.sinHalf, this.cosHalf);
    }

    public float cosDouble() {
        return this.cosHalf * this.cosHalf - this.sinHalf * this.sinHalf;
    }

    public float sinDouble() {
        return 2.0f * this.sinHalf * this.cosHalf;
    }

    public Matrix3f setRotationX(Matrix3f matrix3f) {
        matrix3f.m01 = 0.0f;
        matrix3f.m02 = 0.0f;
        matrix3f.m10 = 0.0f;
        matrix3f.m20 = 0.0f;
        float f = this.cosDouble();
        float g = this.sinDouble();
        matrix3f.m11 = f;
        matrix3f.m22 = f;
        matrix3f.m12 = g;
        matrix3f.m21 = -g;
        matrix3f.m00 = 1.0f;
        return matrix3f;
    }

    public Matrix3f setRotationY(Matrix3f matrix3f) {
        matrix3f.m01 = 0.0f;
        matrix3f.m10 = 0.0f;
        matrix3f.m12 = 0.0f;
        matrix3f.m21 = 0.0f;
        float f = this.cosDouble();
        float g = this.sinDouble();
        matrix3f.m00 = f;
        matrix3f.m22 = f;
        matrix3f.m02 = -g;
        matrix3f.m20 = g;
        matrix3f.m11 = 1.0f;
        return matrix3f;
    }

    public Matrix3f setRotationZ(Matrix3f matrix3f) {
        matrix3f.m02 = 0.0f;
        matrix3f.m12 = 0.0f;
        matrix3f.m20 = 0.0f;
        matrix3f.m21 = 0.0f;
        float f = this.cosDouble();
        float g = this.sinDouble();
        matrix3f.m00 = f;
        matrix3f.m11 = f;
        matrix3f.m01 = g;
        matrix3f.m10 = -g;
        matrix3f.m22 = 1.0f;
        return matrix3f;
    }
}
