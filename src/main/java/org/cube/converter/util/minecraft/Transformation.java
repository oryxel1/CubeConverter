package org.cube.converter.util.minecraft;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public record Transformation(Vector3f translation, Quaternionf left, Vector3f scale, Quaternionf right) {

}
