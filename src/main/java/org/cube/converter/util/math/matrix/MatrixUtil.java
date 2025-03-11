package org.cube.converter.util.math.matrix;

import org.cube.converter.util.Triple;
import org.cube.converter.util.minecraft.Transformation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// Taken from minecraft src code :tm:
public class MatrixUtil {
    private static final float COT_PI_OVER_8 = 3.0F + 2.0F * (float) Math.sqrt(2.0F);
    private static final GivensPair SIN_COS_PI_OVER_8 = GivensPair.fromAngle(0.7853982f);

    private MatrixUtil() {
    }

    public static Transformation fromMatrix(final Matrix4f matrix) {
        float f = 1.0f / matrix.m33();
        Vector3f translation = matrix.getTranslation(new Vector3f()).mul(f);
        Triple<Quaternionf, Vector3f, Quaternionf> triple = MatrixUtil.svdDecompose(new Matrix3f(matrix).scale(f));
        Quaternionf leftRotation = new Quaternionf(triple.left());
        Vector3f scale = new Vector3f(triple.middle());
        Quaternionf rightRotation = new Quaternionf(triple.right());

        return new Transformation(translation, leftRotation, scale, rightRotation);
    }

    public static Matrix4f scale(Matrix4f matrix, float scalar) {
        return matrix.set(matrix.m00() * scalar, matrix.m01() * scalar, matrix.m02() * scalar, matrix.m03() * scalar, matrix.m10() * scalar, matrix.m11() * scalar, matrix.m12() * scalar, matrix.m13() * scalar, matrix.m20() * scalar, matrix.m21() * scalar, matrix.m22() * scalar, matrix.m23() * scalar, matrix.m30() * scalar, matrix.m31() * scalar, matrix.m32() * scalar, matrix.m33() * scalar);
    }

    private static GivensPair approximateGivensQuaternion(float a11, float a12, float a22) {
        float i = 2.0f * (a11 - a22);
        if (COT_PI_OVER_8 * a12 * a12 < i * i) {
            return GivensPair.normalize(a12, i);
        }
        return SIN_COS_PI_OVER_8;
    }

    private static GivensPair qrGivensQuaternion(float a1, float a2) {
        float h = (float)java.lang.Math.hypot(a1, a2);
        float i = h > 1.0E-6f ? a2 : 0.0f;
        float j = Math.abs(a1) + Math.max(h, 1.0E-6f);
        if (a1 < 0.0f) {
            float k = i;
            i = j;
            j = k;
        }
        return GivensPair.normalize(i, j);
    }

    private static void conjugate(Matrix3f X, Matrix3f A) {
        X.mul(A);
        A.transpose();
        A.mul(X);
        X.set(A);
    }

    private static void applyJacobiIteration(Matrix3f AtA, Matrix3f matrix3f2, Quaternionf quaternionf, Quaternionf quaternionf2) {
        Quaternionf quaternionf3;
        GivensPair lv;
        if (AtA.m01 * AtA.m01 + AtA.m10 * AtA.m10 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(AtA.m00, 0.5f * (AtA.m01 + AtA.m10), AtA.m11);
            quaternionf3 = lv.setZRotation(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.setRotationZ(matrix3f2);
            MatrixUtil.conjugate(AtA, matrix3f2);
        }
        if (AtA.m02 * AtA.m02 + AtA.m20 * AtA.m20 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(AtA.m00, 0.5f * (AtA.m02 + AtA.m20), AtA.m22).negateSin();
            quaternionf3 = lv.setYRotation(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.setRotationY(matrix3f2);
            MatrixUtil.conjugate(AtA, matrix3f2);
        }
        if (AtA.m12 * AtA.m12 + AtA.m21 * AtA.m21 > 1.0E-6f) {
            lv = MatrixUtil.approximateGivensQuaternion(AtA.m11, 0.5f * (AtA.m12 + AtA.m21), AtA.m22);
            quaternionf3 = lv.setXRotation(quaternionf);
            quaternionf2.mul(quaternionf3);
            lv.setRotationX(matrix3f2);
            MatrixUtil.conjugate(AtA, matrix3f2);
        }
    }

    public static Quaternionf applyJacobiIterations(Matrix3f AtA, int numJacobiIterations) {
        Quaternionf quaternionf = new Quaternionf();
        Matrix3f matrix3f2 = new Matrix3f();
        Quaternionf quaternionf2 = new Quaternionf();
        for (int j = 0; j < numJacobiIterations; ++j) {
            MatrixUtil.applyJacobiIteration(AtA, matrix3f2, quaternionf2, quaternionf);
        }
        quaternionf.normalize();
        return quaternionf;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f A) {
        Matrix3f matrix3f2 = new Matrix3f(A);
        matrix3f2.transpose();
        matrix3f2.mul(A);
        Quaternionf quaternionf = MatrixUtil.applyJacobiIterations(matrix3f2, 5);
        float f = matrix3f2.m00;
        float g = matrix3f2.m11;
        boolean bl = (double)f < 1.0E-6;
        boolean bl2 = (double)g < 1.0E-6;
        Matrix3f matrix3f3 = matrix3f2;
        Matrix3f matrix3f4 = A.rotate(quaternionf);
        Quaternionf quaternionf2 = new Quaternionf();
        Quaternionf quaternionf3 = new Quaternionf();
        GivensPair lv = bl ? MatrixUtil.qrGivensQuaternion(matrix3f4.m11, -matrix3f4.m10) : MatrixUtil.qrGivensQuaternion(matrix3f4.m00, matrix3f4.m01);
        Quaternionf quaternionf4 = lv.setZRotation(quaternionf3);
        Matrix3f matrix3f5 = lv.setRotationZ(matrix3f3);
        quaternionf2.mul(quaternionf4);
        matrix3f5.transpose().mul(matrix3f4);
        matrix3f3 = matrix3f4;
        lv = bl ? MatrixUtil.qrGivensQuaternion(matrix3f5.m22, -matrix3f5.m20) : MatrixUtil.qrGivensQuaternion(matrix3f5.m00, matrix3f5.m02);
        lv = lv.negateSin();
        Quaternionf quaternionf5 = lv.setYRotation(quaternionf3);
        Matrix3f matrix3f6 = lv.setRotationY(matrix3f3);
        quaternionf2.mul(quaternionf5);
        matrix3f6.transpose().mul(matrix3f5);
        matrix3f3 = matrix3f5;
        lv = bl2 ? MatrixUtil.qrGivensQuaternion(matrix3f6.m22, -matrix3f6.m21) : MatrixUtil.qrGivensQuaternion(matrix3f6.m11, matrix3f6.m12);
        Quaternionf quaternionf6 = lv.setXRotation(quaternionf3);
        Matrix3f matrix3f7 = lv.setRotationX(matrix3f3);
        quaternionf2.mul(quaternionf6);
        matrix3f7.transpose().mul(matrix3f6);
        Vector3f vector3f = new Vector3f(matrix3f7.m00, matrix3f7.m11, matrix3f7.m22);
        return new Triple<>(quaternionf2, vector3f, quaternionf.conjugate());
    }

    public static boolean isIdentity(Matrix4f matrix) {
        return (matrix.properties() & 4) != 0;
    }

    public static boolean isTranslation(Matrix4f matrix) {
        return (matrix.properties() & 8) != 0;
    }

    public static boolean isOrthonormal(Matrix4f matrix) {
        return (matrix.properties() & 0x10) != 0;
    }
}


