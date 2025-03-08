package org.cube.converter.util;

import lombok.Getter;
import lombok.Setter;
import org.cube.converter.util.element.Direction;
import org.cube.converter.util.element.Position2V;
import org.cube.converter.util.element.Position3V;

import java.util.Arrays;
import java.util.List;

public class UVUtil {
    public static List<Face> toUvFaces(final Position3V size, boolean mirror) {
        Face face = new Face(Direction.DOWN, new Position2V(size.getZ() + size.getX() * 2, 0), new Position2V(-size.getX(), size.getZ()), mirror);
        Face face1 = new Face(Direction.UP, new Position2V(size.getZ() + size.getX(), size.getZ()), new Position2V( -size.getX(), -size.getZ()), mirror);

        return Arrays.asList(
                mirror ? face1 : face,
                mirror ? face : face1,
                new Face(Direction.NORTH, new Position2V(size.getZ(), size.getZ()), new Position2V(size.getX(), size.getY()), mirror),
                new Face(Direction.SOUTH, new Position2V(size.getZ() * 2 + size.getX(), size.getZ()), new Position2V(size.getX(), size.getY()), mirror),
                new Face(Direction.WEST, new Position2V(size.getZ() + size.getX(), size.getZ()), new Position2V(size.getZ(), size.getY()), mirror),
                new Face(Direction.EAST, new Position2V(0, size.getZ()), new Position2V(size.getZ(), size.getY()), mirror));
    }

    @Getter
    @Setter
    public static class Face {
        private final Direction direction;
        private Position2V start, end;

        public Face(Direction direction, Position2V position, Position2V size, boolean mirror) {
            this.direction = direction;

            if (mirror) {
                position = position.add(size.getX(), 0);
                size.setX(-size.getX());
            }

            this.start = position;
            this.end = position.add(size);
        }
    }
}