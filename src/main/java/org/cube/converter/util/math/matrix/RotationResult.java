package org.cube.converter.util.math.matrix;

import org.cube.converter.util.element.Position3V;

/**
 * Holds the result of combining multiple rotations into a single transformation.
 * 
 * @param position The final position of the cube after all rotations
 * @param pivot The pivot point for the combined rotation
 * @param rotation The combined rotation angles (X, Y, Z in degrees)
 */
public record RotationResult(Position3V position, Position3V pivot, Position3V rotation) {
    
    @Override
    public String toString() {
        return "RotationResult{position=" + position + ", pivot=" + pivot + ", rotation=" + rotation + "}";
    }
}
