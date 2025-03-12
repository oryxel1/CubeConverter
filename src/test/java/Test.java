import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Test {
    public static void main(String[] args) {
        float f = 0.017453292519943295F;

        float offset = 10 / 16.0F;

        Matrix4f matrix4f = new Matrix4f();
        matrix4f = matrix4f.translate(0, offset, 0);
        matrix4f = matrix4f.rotateX(90 * f);
        matrix4f = matrix4f.scale(1.5F);
        matrix4f = matrix4f.transpose();

        System.out.println(matrix4f.m00() + "," + matrix4f.m01() + "," + matrix4f.m02() + "," + matrix4f.m03() + ","
        + matrix4f.m10() + "," + matrix4f.m11() + "," + matrix4f.m12() + "," + matrix4f.m13() + "," + matrix4f.m20() + ","
        + matrix4f.m21() + "," + matrix4f.m22() + "," + matrix4f.m23() + "," + matrix4f.m30() + "," + matrix4f.m31() + ","
        + matrix4f.m32() + "," + matrix4f.m33());
    }
}
