package org.cube.converter.util.element;

import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cube.converter.util.math.MathUtil;

@AllArgsConstructor
@ToString
@Getter
@Setter
public final class Position3V {
    private float x, y, z;

    public Position3V(final JsonArray array) {
        if (array == null) {
            this.x = this.y = this.z = 0;
            return;
        }

        this.x = array.get(0).getAsFloat();
        this.y = array.get(1).getAsFloat();
        this.z = array.get(2).getAsFloat();
    }

    public Position3V(final float[] array) {
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
    }

    public static Position3V zero() {
        return new Position3V(0, 0, 0);
    }

    public static Position3V fromOrigin(final JsonArray origin, final JsonArray size) {
        float x = -(origin.get(0).getAsFloat() + size.get(0).getAsFloat()),
                y = origin.get(1).getAsFloat(), z = origin.get(2).getAsFloat();
        return new Position3V(x, y, z);
    }

    public Position3V getJavaOverlap() {
        final Position3V overlap = new Position3V(0, 0, 0);
        overlap.setX(Math.abs(this.x - MathUtil.clamp(this.x, -16, 32)));
        overlap.setY(Math.abs(this.y - MathUtil.clamp(this.y, -16, 32)));
        overlap.setZ(Math.abs(this.z - MathUtil.clamp(this.z, -16, 32)));
        return overlap;
    }

    public void scale(float scale) {
        this.x = this.x * scale;
        this.y = this.y * scale;
        this.z = this.z * scale;
    }

    public Position3V multiply(float x, float y, float z) {
        Position3V position3V = clone();
        position3V.x = this.x * x;
        position3V.y = this.y * y;
        position3V.z = this.z * z;
        return position3V;
    }

    public void set(final Position3V position3V) {
        this.set(position3V.x, position3V.y, position3V.z);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position3V add(final Position3V position3V) {
        return this.add(position3V.x, position3V.y, position3V.z);
    }

    public Position3V add(float x, float y, float z) {
        return new Position3V(this.x + x, this.y + y, this.z + z);
    }

    public Position3V subtract(final Position3V position3V) {
        return this.subtract(position3V.x, position3V.y, position3V.z);
    }

    public Position3V subtract(float x, float y, float z) {
        return new Position3V(this.x - x, this.y - y, this.z - z);
    }

    public Position3V asJavaPosition(final Position3V size) {
        return new Position3V(-(this.x + size.x), this.y, this.z).withJavaOffset();
    }

    public Position3V asBedrockPosition(final Position3V size) {
        final float newX = this.x - 8;

        return new Position3V(-newX - size.x, this.y, this.z - 8);
    }

    public Position3V withJavaOffset() {
        return new Position3V(this.x + 8, this.y, this.z + 8);
    }

    public boolean isZero() {
        return this.x == 0 && this.y == 0 && this.z == 0;
    }

    public float[] toArray() {
        return new float[] {this.x, this.y, this.z};
    }

    @Override
    public Position3V clone() {
        return this.add(0, 0, 0);
    }

    public JsonArray toJson() {
        final JsonArray array = new JsonArray();
        array.add(this.x);
        array.add(this.y);
        array.add(this.z);
        return array;
    }
}