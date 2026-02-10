import org.cube.converter.util.Triple;
import org.cube.converter.util.math.matrix.MatrixUtil;
import org.joml.*;

import java.lang.Math;

public class Test {
    public static void main(String[] args) {
        Matrix4f matrix = new Matrix4f().identity();

//        matrix = matrix.translate(0, 0, -5);
//        matrix = matrix.rotateY((float) Math.toRadians(50));
//        matrix = matrix.translate(0, 0, 5);

        matrix = matrix.translate(0, 0, -20);
        matrix = matrix.rotateZ((float) Math.toRadians(60));
        matrix = matrix.translate(0, 0, 20);

        float var1 = 1.0F / matrix.m33();
        Triple var2 = MatrixUtil.svdDecompose((new Matrix3f(matrix)).scale(var1));
        Vector3f translation = matrix.getTranslation(new Vector3f()).mul(var1);
        Quaternionf leftRotation = new Quaternionf((Quaternionfc)var2.left());
        Vector3f scale = new Vector3f((Vector3fc)var2.middle());
        Quaternionf rightRotation = new Quaternionf((Quaternionfc)var2.right());

        System.out.println(translation.x + "," + translation.y + "," + translation.z);
        System.out.println(leftRotation.x + "," + leftRotation.y + "," + leftRotation.z + "," + leftRotation.w);
        System.out.println(rightRotation.x + "," + rightRotation.y + "," + rightRotation.z + "," + rightRotation.w);
    }
}
