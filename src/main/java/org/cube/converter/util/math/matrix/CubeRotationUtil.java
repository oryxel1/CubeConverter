package org.cube.converter.util.math.matrix;

import org.cube.converter.util.element.Position3V;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Utility class for combining multiple rotations around different pivot points
 * into a single equivalent transformation (position, pivot, rotation).
 * <p>
 * <b>Important:</b> The output {@link RotationResult} provides:
 * <ul>
 *   <li><b>position</b>: The exact final position after all rotations - always accurate</li>
 *   <li><b>rotation</b>: The combined rotation as Euler angles (XYZ). Due to Euler angle
 *       representation limitations (gimbal lock, multiple solutions), this may not perfectly
 *       reproduce the original transformation when re-applied.</li>
 *   <li><b>pivot</b>: A computed pivot point. Combined with the rotation, it can approximate
 *       the original transformation.</li>
 * </ul>
 * <p>
 * For guaranteed accuracy, use the {@code position} field directly. The {@code rotation} and
 * {@code pivot} are best-effort approximations useful when you need to represent the
 * transformation in a single rotation form.
 * <p>
 * For perfect accuracy when re-applying transformations, use {@link #combineRotationsWithQuaternion}
 * which returns the rotation as a quaternion instead of Euler angles.
 */
public class CubeRotationUtil {
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float RADIANS_TO_DEGREES = (float) (180.0 / Math.PI);

    private CubeRotationUtil() {}

    /**
     * Represents a single rotation operation with its pivot point.
     * 
     * @param rotation The rotation angles in degrees (X, Y, Z)
     * @param pivot The pivot point to rotate around (X, Y, Z)
     */
    public record RotationEntry(Position3V rotation, Position3V pivot) {}

    /**
     * Combines multiple rotations into a single equivalent transformation.
     * <p>
     * The output provides:
     * <ul>
     *   <li>position: The exact final position of the cube after all rotations are applied</li>
     *   <li>rotation: The combined rotation as Euler angles (XYZ intrinsic)</li>
     *   <li>pivot: A pivot point such that applying the combined rotation around this pivot
     *              to the original cube position produces the final position</li>
     * </ul>
     * <p>
     * <b>For Bedrock/Minecraft models:</b> Use {@link #combineRotationsForBedrock} which applies
     * the correct sign conventions for Y and Z rotation axes.
     * 
     * @param rotations List of rotation entries, applied in order
     * @param cubePosition The initial position of the cube
     * @param cubeSize The size of the cube (not used in rotation calculation, included for API completeness)
     * @return A RotationResult containing the combined position, pivot, and rotation
     */
    public static RotationResult combineRotations(List<RotationEntry> rotations, Position3V cubePosition, Position3V cubeSize) {
        if (rotations == null || rotations.isEmpty()) {
            return new RotationResult(cubePosition.clone(), Position3V.zero(), Position3V.zero());
        }

        // Build the combined transformation matrix
        // We want to apply T1 first, then T2, then T3, etc.
        // For a point p: result = Tn * ... * T2 * T1 * p
        // So we need to build: combinedMatrix = Tn * ... * T2 * T1
        // This means each new transformation is pre-multiplied (multiplied on the left)
        Matrix4f combinedMatrix = new Matrix4f().identity();
        
        for (RotationEntry entry : rotations) {
            Position3V pivot = entry.pivot();
            Position3V rotation = entry.rotation();
            
            // Create rotation quaternion from Euler angles (XYZ order, intrinsic)
            Quaternionf rotationQuat = new Quaternionf().rotateXYZ(
                rotation.getX() * DEGREES_TO_RADIANS,
                rotation.getY() * DEGREES_TO_RADIANS,
                rotation.getZ() * DEGREES_TO_RADIANS
            );
            
            // Build this rotation's transformation matrix: T(pivot) * R * T(-pivot)
            Matrix4f rotationMatrix = new Matrix4f()
                .translate(pivot.getX(), pivot.getY(), pivot.getZ())
                .rotate(rotationQuat)
                .translate(-pivot.getX(), -pivot.getY(), -pivot.getZ());
            
            // Pre-multiply: combinedMatrix = rotationMatrix * combinedMatrix
            // This ensures transformations are applied in the order they appear in the list
            combinedMatrix = rotationMatrix.mul(combinedMatrix);
        }

        // Transform the cube position through the combined matrix to get exact final position
        Vector3f transformedPos = new Vector3f(cubePosition.getX(), cubePosition.getY(), cubePosition.getZ());
        combinedMatrix.transformPosition(transformedPos);
        Position3V finalPosition = new Position3V(transformedPos.x, transformedPos.y, transformedPos.z);

        // Extract the rotation component from the combined matrix
        // This captures the true combined rotation including how pivots affect the rotation sequence
        Quaternionf combinedRotation = combinedMatrix.getUnnormalizedRotation(new Quaternionf());
        combinedRotation.normalize();
        
        // Convert quaternion to Euler angles (degrees)
        Vector3f eulerAngles = quaternionToEulerXYZ(combinedRotation);
        Position3V finalRotation = new Position3V(
            eulerAngles.x * RADIANS_TO_DEGREES,
            eulerAngles.y * RADIANS_TO_DEGREES,
            eulerAngles.z * RADIANS_TO_DEGREES
        );

        // Calculate the pivot point that makes the single rotation equivalent
        Position3V finalPivot = calculateOptimalPivot(rotations, cubePosition, finalPosition, combinedRotation);

        return new RotationResult(finalPosition, finalPivot, finalRotation);
    }

    /**
     * Combines multiple rotations for Bedrock/Minecraft models.
     * <p>
     * This method applies Bedrock-specific conventions:
     * <ul>
     *   <li>Output rotation Y and Z are negated to match Bedrock's coordinate system</li>
     * </ul>
     * 
     * @param rotations List of rotation entries, applied in order (cube rotation first, then bone rotation)
     * @param cubePosition The initial position of the cube (origin from Bedrock)
     * @param cubeSize The size of the cube
     * @return A RotationResult with rotation in Bedrock convention
     */
    public static RotationResult combineRotationsForBedrock(List<RotationEntry> rotations, Position3V cubePosition, Position3V cubeSize) {
        RotationResult result = combineRotations(rotations, cubePosition, cubeSize);
        
        // Apply Bedrock convention: negate Y and Z rotation
        Position3V bedrockRotation = new Position3V(
            result.rotation().getX(),
            -result.rotation().getY(),
            -result.rotation().getZ()
        );
        
        return new RotationResult(result.position(), result.pivot(), bedrockRotation);
    }

    /**
     * Calculates an optimal pivot point for the combined rotation.
     * <p>
     * The pivot is chosen such that applying the combined rotation around this pivot
     * to the original position produces exactly the final position.
     * <p>
     * Mathematical derivation:
     * <pre>
     * Given: finalPosition = pivot + R * (originalPosition - pivot)
     * We solve for pivot:
     *   finalPosition = pivot + R*originalPosition - R*pivot
     *   finalPosition - R*originalPosition = pivot - R*pivot
     *   finalPosition - R*originalPosition = (I - R) * pivot
     *   pivot = (I - R)^(-1) * (finalPosition - R*originalPosition)
     * </pre>
     */
    private static Position3V calculateOptimalPivot(List<RotationEntry> rotations, 
                                                      Position3V originalPosition,
                                                      Position3V finalPosition,
                                                      Quaternionf combinedRotation) {
        // If there's no effective rotation, pivot doesn't matter - use centroid as fallback
        if (isIdentityRotation(combinedRotation)) {
            return calculatePivotCentroid(rotations);
        }

        Vector3f origPos = new Vector3f(originalPosition.getX(), originalPosition.getY(), originalPosition.getZ());
        Vector3f finPos = new Vector3f(finalPosition.getX(), finalPosition.getY(), finalPosition.getZ());
        
        // Calculate R * originalPosition
        Vector3f rotatedOrigPos = new Vector3f(origPos);
        combinedRotation.transform(rotatedOrigPos);
        
        // Calculate (finalPosition - R * originalPosition)
        Vector3f diff = new Vector3f(finPos).sub(rotatedOrigPos);
        
        // Calculate (I - R) as a 3x3 matrix operation
        // We need to solve: (I - R) * pivot = diff
        Matrix4f rotMatrix = new Matrix4f().rotation(combinedRotation);
        Matrix4f identityMinusR = new Matrix4f().identity().sub(rotMatrix);
        
        // Check if matrix is invertible
        float det = identityMinusR.determinant();
        if (Math.abs(det) > 1e-6f) {
            Matrix4f inverse = new Matrix4f(identityMinusR).invert();
            Vector3f pivot = new Vector3f(diff.x, diff.y, diff.z);
            inverse.transformPosition(pivot);
            return new Position3V(pivot.x, pivot.y, pivot.z);
        }
        
        // If matrix is singular (rotation is ~360 degrees), fall back to centroid
        return calculatePivotCentroid(rotations);
    }

    /**
     * Calculates the centroid (average) of all pivot points.
     */
    private static Position3V calculatePivotCentroid(List<RotationEntry> rotations) {
        if (rotations.isEmpty()) {
            return Position3V.zero();
        }

        float sumX = 0, sumY = 0, sumZ = 0;
        for (RotationEntry entry : rotations) {
            sumX += entry.pivot().getX();
            sumY += entry.pivot().getY();
            sumZ += entry.pivot().getZ();
        }

        int count = rotations.size();
        return new Position3V(sumX / count, sumY / count, sumZ / count);
    }

    /**
     * Checks if a quaternion represents an identity (no rotation) or near-identity rotation.
     */
    private static boolean isIdentityRotation(Quaternionf q) {
        float epsilon = 1e-6f;
        return (Math.abs(q.x) < epsilon && 
                Math.abs(q.y) < epsilon && 
                Math.abs(q.z) < epsilon && 
                Math.abs(Math.abs(q.w) - 1.0f) < epsilon);
    }

    /**
     * Converts a quaternion to Euler angles (XYZ order, intrinsic rotations).
     * Returns angles in radians.
     */
    private static Vector3f quaternionToEulerXYZ(Quaternionf q) {
        Vector3f euler = new Vector3f();
        
        // Roll (X-axis rotation)
        float sinr_cosp = 2.0f * (q.w * q.x + q.y * q.z);
        float cosr_cosp = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
        euler.x = (float) Math.atan2(sinr_cosp, cosr_cosp);
        
        // Pitch (Y-axis rotation)
        float sinp = 2.0f * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinp) >= 1.0f) {
            euler.y = (float) Math.copySign(Math.PI / 2.0, sinp);
        } else {
            euler.y = (float) Math.asin(sinp);
        }
        
        // Yaw (Z-axis rotation)
        float siny_cosp = 2.0f * (q.w * q.z + q.x * q.y);
        float cosy_cosp = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
        euler.z = (float) Math.atan2(siny_cosp, cosy_cosp);
        
        return euler;
    }

    /**
     * Combines multiple rotations and returns the result with a quaternion for perfect accuracy.
     * <p>
     * This method is similar to {@link #combineRotations} but returns the combined rotation
     * as a quaternion instead of Euler angles. This avoids the ambiguity and limitations
     * of Euler angle representation.
     * 
     * @param rotations List of rotation entries, applied in order
     * @param cubePosition The initial position of the cube
     * @param cubeSize The size of the cube (not used in rotation calculation)
     * @return A QuaternionRotationResult containing position, pivot, and quaternion rotation
     */
    public static QuaternionRotationResult combineRotationsWithQuaternion(List<RotationEntry> rotations, 
                                                                           Position3V cubePosition, 
                                                                           Position3V cubeSize) {
        if (rotations == null || rotations.isEmpty()) {
            return new QuaternionRotationResult(cubePosition.clone(), Position3V.zero(), new Quaternionf());
        }

        Matrix4f combinedMatrix = new Matrix4f().identity();
        
        for (RotationEntry entry : rotations) {
            Position3V pivot = entry.pivot();
            Position3V rotation = entry.rotation();
            
            Quaternionf rotationQuat = new Quaternionf().rotateXYZ(
                rotation.getX() * DEGREES_TO_RADIANS,
                rotation.getY() * DEGREES_TO_RADIANS,
                rotation.getZ() * DEGREES_TO_RADIANS
            );
            
            Matrix4f rotationMatrix = new Matrix4f()
                .translate(pivot.getX(), pivot.getY(), pivot.getZ())
                .rotate(rotationQuat)
                .translate(-pivot.getX(), -pivot.getY(), -pivot.getZ());
            
            combinedMatrix = rotationMatrix.mul(combinedMatrix);
        }

        Vector3f transformedPos = new Vector3f(cubePosition.getX(), cubePosition.getY(), cubePosition.getZ());
        combinedMatrix.transformPosition(transformedPos);
        Position3V finalPosition = new Position3V(transformedPos.x, transformedPos.y, transformedPos.z);

        Quaternionf combinedRotation = combinedMatrix.getUnnormalizedRotation(new Quaternionf());
        combinedRotation.normalize();

        Position3V finalPivot = calculateOptimalPivot(rotations, cubePosition, finalPosition, combinedRotation);

        return new QuaternionRotationResult(finalPosition, finalPivot, new Quaternionf(combinedRotation));
    }

    /**
     * Result of combining rotations with quaternion representation.
     */
    public record QuaternionRotationResult(Position3V position, Position3V pivot, Quaternionf rotation) {}

    /**
     * Applies a single rotation around a pivot point to a position.
     * 
     * @param position The position to rotate
     * @param rotation The rotation angles in degrees (X, Y, Z)
     * @param pivot The pivot point
     * @return The rotated position
     */
    public static Position3V applyRotation(Position3V position, Position3V rotation, Position3V pivot) {
        Quaternionf rotationQuat = new Quaternionf().rotateXYZ(
            rotation.getX() * DEGREES_TO_RADIANS,
            rotation.getY() * DEGREES_TO_RADIANS,
            rotation.getZ() * DEGREES_TO_RADIANS
        );

        return applyQuaternionRotation(position, rotationQuat, pivot);
    }

    /**
     * Applies a quaternion rotation around a pivot point to a position.
     * This is more accurate than using Euler angles.
     * 
     * @param position The position to rotate
     * @param rotation The rotation quaternion
     * @param pivot The pivot point
     * @return The rotated position
     */
    public static Position3V applyQuaternionRotation(Position3V position, Quaternionf rotation, Position3V pivot) {
        Vector3f pos = new Vector3f(
            position.getX() - pivot.getX(),
            position.getY() - pivot.getY(),
            position.getZ() - pivot.getZ()
        );
        
        rotation.transform(pos);
        
        return new Position3V(
            pos.x + pivot.getX(),
            pos.y + pivot.getY(),
            pos.z + pivot.getZ()
        );
    }

    /**
     * Applies multiple rotations sequentially to a position.
     * 
     * @param rotations List of rotation entries
     * @param position The initial position
     * @return The final position after all rotations
     */
    public static Position3V applyMultipleRotations(List<RotationEntry> rotations, Position3V position) {
        Position3V current = position.clone();
        for (RotationEntry entry : rotations) {
            current = applyRotation(current, entry.rotation(), entry.pivot());
        }
        return current;
    }
}
