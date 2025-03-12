package org.cube.converter.model.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cube.converter.util.element.Position3V;
import org.cube.converter.util.element.UVMap;
import org.cube.converter.util.math.MathUtil;

@RequiredArgsConstructor
@Getter
@Setter
public final class Cube {
    private String parent = "";
    private float inflate = 0;

    private final Position3V pivot, position, size;
    private final Position3V rotation;
    private final boolean mirror;
    private final UVMap uvMap;

    // Java Only.
    public void inflate() {
        this.position.set(this.position.add(-this.inflate, -this.inflate, -this.inflate));
        this.size.set(this.size.add(this.inflate, this.inflate, this.inflate));
    }

    // Java Only.
    public void fixRotationIfNeeded() {
        final Position3V rotation = this.getRotation();
        rotation.setX(Math.abs(rotation.getX()) == 180 ? 0 : MathUtil.limitAngle(rotation.getX()));
        rotation.setY(Math.abs(rotation.getY()) == 180 ? 0 : MathUtil.limitAngle(rotation.getY()));
        rotation.setZ(Math.abs(rotation.getZ()) == 180 ? 0 : MathUtil.limitAngle(rotation.getZ()));
    }

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