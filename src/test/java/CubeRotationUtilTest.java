import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.math.matrix.CubeRotationUtil;
import org.cube.converter.util.math.matrix.CubeRotationUtil.RotationEntry;
import org.cube.converter.util.math.matrix.CubeRotationUtil.QuaternionRotationResult;
import org.cube.converter.util.math.matrix.RotationResult;

import java.util.List;

public class CubeRotationUtilTest {
    public static void main(String[] args) {
        testBedrockModel();
        testSingleRotation();
        testMultipleRotations();
        testNoRotation();
        testSamePivotRotations();
        testComplexRotations();
        
        System.out.println("\nAll tests completed!");
    }

    /**
     * Test case from Bedrock geometry model:
     * {
     *   "bones": [{
     *     "name": "cube",
     *     "pivot": [0, 0, -2],
     *     "rotation": [45, 180, 0],
     *     "cubes": [{
     *       "origin": [-1.5, 0, 0],
     *       "size": [14, 14, 14],
     *       "pivot": [8.5, 16, 2],
     *       "rotation": [0, 0, 45]
     *     }]
     *   }]
     * }
     * 
     * Expected output rotation: (144.73561, 30.0, -144.73561)
     */
    private static void testBedrockModel() {
        System.out.println("=== Test: Bedrock Model (Real World Case) ===");
        
        // Raw Bedrock values (as they appear in the JSON file)
        Position3V boneRotation = new Position3V(45, 180, 0);
        Position3V bonePivot = new Position3V(0, 0, -2);
        Position3V cubeRotation = new Position3V(0, 0, 45);
        Position3V cubePivot = new Position3V(8.5f, 16, 2);
        Position3V cubeOrigin = new Position3V(-1.5f, 0, 0);
        Position3V cubeSize = new Position3V(14, 14, 14);
        
        System.out.println("Bedrock file values:");
        System.out.println("  Bone rotation: " + boneRotation + " at pivot " + bonePivot);
        System.out.println("  Cube rotation: " + cubeRotation + " at pivot " + cubePivot);
        System.out.println("  Cube origin: " + cubeOrigin + ", size: " + cubeSize);
        
        Position3V expectedRotation = new Position3V(144.73561f, 30.0f, -144.73561f);
        System.out.println("  Expected combined rotation: " + expectedRotation);
        
        // For Bedrock: cube rotation is applied first, then bone rotation
        List<RotationEntry> rotations = List.of(
            new RotationEntry(cubeRotation, cubePivot),  // Cube rotation first
            new RotationEntry(boneRotation, bonePivot)   // Then bone rotation
        );
        
        // Use the Bedrock-specific method
        RotationResult result = CubeRotationUtil.combineRotationsForBedrock(rotations, cubeOrigin, cubeSize);
        
        System.out.println("\nResult (using combineRotationsForBedrock):");
        System.out.println("  Rotation: " + result.rotation());
        System.out.println("  Position: " + result.position());
        System.out.println("  Pivot: " + result.pivot());
        System.out.println("  Rotation matches expected: " + isCloseRotation(result.rotation(), expectedRotation));
        System.out.println();
    }

    private static void testSingleRotation() {
        System.out.println("=== Test: Single Rotation ===");
        
        Position3V cubePosition = new Position3V(5, 0, 0);
        Position3V cubeSize = new Position3V(2, 2, 2);
        
        List<RotationEntry> rotations = List.of(
            new RotationEntry(new Position3V(0, 90, 0), new Position3V(0, 0, 0))
        );
        
        RotationResult result = CubeRotationUtil.combineRotations(rotations, cubePosition, cubeSize);
        
        System.out.println("Input position: " + cubePosition);
        System.out.println("Rotation: 90 degrees around Y-axis at origin");
        System.out.println("Result: " + result);
        
        Position3V expected = CubeRotationUtil.applyMultipleRotations(rotations, cubePosition);
        System.out.println("Expected position: " + expected);
        System.out.println("Position match: " + isClose(result.position(), expected));
        System.out.println();
    }

    private static void testMultipleRotations() {
        System.out.println("=== Test: Multiple Rotations ===");
        
        Position3V cubePosition = new Position3V(5, 0, 0);
        Position3V cubeSize = new Position3V(2, 2, 2);
        
        List<RotationEntry> rotations = List.of(
            new RotationEntry(new Position3V(0, 45, 0), new Position3V(0, 0, 0)),
            new RotationEntry(new Position3V(0, 45, 0), new Position3V(0, 0, 0))
        );
        
        RotationResult result = CubeRotationUtil.combineRotations(rotations, cubePosition, cubeSize);
        
        System.out.println("Input position: " + cubePosition);
        System.out.println("Rotations: Two 45-degree Y rotations at origin");
        System.out.println("Result: " + result);
        
        Position3V expected = CubeRotationUtil.applyMultipleRotations(rotations, cubePosition);
        System.out.println("Expected position: " + expected);
        System.out.println("Position match: " + isClose(result.position(), expected));
        System.out.println();
    }

    private static void testNoRotation() {
        System.out.println("=== Test: No Rotation ===");
        
        Position3V cubePosition = new Position3V(3, 4, 5);
        Position3V cubeSize = new Position3V(1, 1, 1);
        
        List<RotationEntry> rotations = List.of();
        
        RotationResult result = CubeRotationUtil.combineRotations(rotations, cubePosition, cubeSize);
        
        System.out.println("Input position: " + cubePosition);
        System.out.println("No rotations applied");
        System.out.println("Result: " + result);
        System.out.println("Position unchanged: " + isClose(result.position(), cubePosition));
        System.out.println();
    }

    private static void testSamePivotRotations() {
        System.out.println("=== Test: Multiple Rotations (Same Pivot) ===");
        
        Position3V cubePosition = new Position3V(5, 0, 0);
        Position3V cubeSize = new Position3V(2, 2, 2);
        Position3V pivot = new Position3V(0, 0, 0);
        
        List<RotationEntry> rotations = List.of(
            new RotationEntry(new Position3V(30, 0, 0), pivot),
            new RotationEntry(new Position3V(0, 45, 0), pivot),
            new RotationEntry(new Position3V(0, 0, 60), pivot)
        );
        
        RotationResult result = CubeRotationUtil.combineRotations(rotations, cubePosition, cubeSize);
        
        System.out.println("Input position: " + cubePosition);
        System.out.println("Multiple rotations around same pivot (origin)");
        System.out.println("Result: " + result);
        
        Position3V expected = CubeRotationUtil.applyMultipleRotations(rotations, cubePosition);
        System.out.println("Expected position: " + expected);
        System.out.println("Position match: " + isClose(result.position(), expected));
        
        QuaternionRotationResult quatResult = CubeRotationUtil.combineRotationsWithQuaternion(rotations, cubePosition, cubeSize);
        Position3V fromQuatResult = CubeRotationUtil.applyQuaternionRotation(cubePosition, quatResult.rotation(), quatResult.pivot());
        System.out.println("From quaternion result: " + fromQuatResult);
        System.out.println("Quaternion result match: " + isClose(fromQuatResult, expected));
        System.out.println();
    }

    private static void testComplexRotations() {
        System.out.println("=== Test: Complex Rotations (Different Pivots) ===");
        
        Position3V cubePosition = new Position3V(2, 0, 0);
        Position3V cubeSize = new Position3V(1, 1, 1);
        
        List<RotationEntry> rotations = List.of(
            new RotationEntry(new Position3V(90, 0, 0), new Position3V(0, 0, 0)),
            new RotationEntry(new Position3V(0, 90, 0), new Position3V(1, 1, 1)),
            new RotationEntry(new Position3V(0, 0, 45), new Position3V(0, 2, 0))
        );
        
        RotationResult result = CubeRotationUtil.combineRotations(rotations, cubePosition, cubeSize);
        
        System.out.println("Input position: " + cubePosition);
        System.out.println("Multiple rotations around different pivots");
        System.out.println("Result: " + result);
        
        Position3V expected = CubeRotationUtil.applyMultipleRotations(rotations, cubePosition);
        System.out.println("Expected position: " + expected);
        System.out.println("Position match: " + isClose(result.position(), expected));
        System.out.println();
    }

    private static boolean isClose(Position3V a, Position3V b) {
        float epsilon = 0.01f;
        return Math.abs(a.getX() - b.getX()) < epsilon &&
               Math.abs(a.getY() - b.getY()) < epsilon &&
               Math.abs(a.getZ() - b.getZ()) < epsilon;
    }
    
    private static boolean isCloseRotation(Position3V a, Position3V b) {
        float epsilon = 1.0f;
        return Math.abs(a.getX() - b.getX()) < epsilon &&
               Math.abs(a.getY() - b.getY()) < epsilon &&
               Math.abs(a.getZ() - b.getZ()) < epsilon;
    }
}
