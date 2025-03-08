package org.cube.converter.util.element;

import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Position2V {
    private double x, y;

    public final Position2V add(final Position2V position2V) {
        return this.add(position2V.x, position2V.y);
    }

    public final Position2V add(double x, double y) {
        return new Position2V(this.x + x, this.y + y);
    }

    public final JsonArray toJson() {
        final JsonArray array = new JsonArray();
        array.add(this.x);
        array.add(this.y);
        return array;
    }
}