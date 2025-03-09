package org.cube.converter.model.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.element.UVMap;

@RequiredArgsConstructor
@Getter
@Setter
public final class Cube {
    private String parent = "";
    private double inflate = 0.0;

    private final Position3V pivot, position, size;
    private final Position3V rotation;
    private final boolean mirror;
    private final UVMap uvMap;

    public String getAxis() {
        return this.rotation.getX() != 0 ? "x" : this.rotation.getY() != 0 ? "y" : "z";
    }

    @Override
    public Cube clone() {
        final Cube cube = new Cube(pivot.clone(), position.clone(), size.clone(), rotation.clone(), mirror, uvMap.clone());
        cube.setParent(parent);
        cube.setInflate(inflate);

        return cube;
    }
}