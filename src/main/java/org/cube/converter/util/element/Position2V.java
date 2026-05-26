package org.cube.converter.util.element;

import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Position2V {
    private float x, y;

    public final Position2V add(final Position2V position2V) {
        return this.add(position2V.x, position2V.y);
    }

    public final Position2V add(float x, float y) {
        return new Position2V(this.x + x, this.y + y);
    }

    @Override
    public Position2V clone() {
        return this.add(0, 0);
    }

    public final JSONArray toJson() {
        final JSONArray array = new JSONArray();
        array.add(this.x);
        array.add(this.y);
        return array;
    }
}