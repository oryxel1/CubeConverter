package org.cube.converter.util.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@RequiredArgsConstructor
@ToString(includeFieldNames = false)
@Getter
@Setter
public class Transformation {
    private final Vector3f translation;
    private final Quaternionf leftRotation;
    private final Vector3f scale;
    private final Quaternionf rightRotation;
}
