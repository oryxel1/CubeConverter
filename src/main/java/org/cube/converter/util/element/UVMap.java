package org.cube.converter.util.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cube.converter.util.UVUtil;

@Getter
public final class UVMap {
    private final UVType uvType;
    private final Map<Direction, Double[]> map = new HashMap<>();

    private UVMap(UVType uvType) {
        this.uvType = uvType;
    }

    public UVMap toJavaPerfaceUV(final double textureWidth, final double textureHeight) {
        final UVMap map = new UVMap(UVType.PERFACE);

        for (final Map.Entry<Direction, Double[]> entry : this.map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            Double[] uv = entry.getValue();

            if (this.uvType != UVType.BOX) {
                uv = new Double[] {entry.getValue()[0], entry.getValue()[1], entry.getValue()[0] + entry.getValue()[2], entry.getValue()[1] + entry.getValue()[3]};
            }

            for (int i = 0; i < uv.length; i++) {
                uv[i] = uv[i] * 16 / (i % 2 == 0 ? textureWidth : textureHeight);
            }

            map.getMap().put(entry.getKey(), uv);
        }

        return map;
    }

    public static UVMap fromBoxUV(final Position3V size, final Double[] offset, final boolean mirror) {
        final UVMap map = new UVMap(UVType.BOX);
        final List<UVUtil.Face> faces = UVUtil.toUvFaces(size, mirror);

        for (UVUtil.Face face : faces) {
            map.getMap().put(face.getDirection(), new Double[] { face.getStart().getX() + offset[0], face.getStart().getY() + offset[1], face.getEnd().getX() + offset[0], face.getEnd().getY() + offset[1] });
        }
        return map;
    }

    public static UVMap fromPerfaceUV(final JsonObject object) {
        final JsonObject uv = object.getAsJsonObject("uv");
        final UVMap map = new UVMap(UVType.PERFACE);
        
        putIfExist(Direction.NORTH, uv, map.getMap());
        putIfExist(Direction.EAST, uv, map.getMap());
        putIfExist(Direction.SOUTH, uv, map.getMap());
        putIfExist(Direction.WEST, uv, map.getMap());
        putIfExist(Direction.UP, uv, map.getMap());
        putIfExist(Direction.DOWN, uv, map.getMap());
        return map;
    }

    private static void putIfExist(final Direction direction, final JsonObject object, final Map<Direction, Double[]> map) {
        String name = direction.name().toLowerCase();
        if (object.has(name)) {
            map.put(direction, getUVDirection(object.getAsJsonObject(name)));
        }
    }

    private static Double[] getUVDirection(JsonObject object) {
        if (object == null) {
            return new Double[] { 0D, 0D, 0D };
        }

        final JsonArray arrayUv = object.getAsJsonArray("uv");
        final JsonArray arrayUvSize = object.getAsJsonArray("uv_size");

        return new Double[] {arrayUv.get(0).getAsDouble(), arrayUv.get(1).getAsDouble(), arrayUvSize.get(0).getAsDouble(), arrayUvSize.get(1).getAsDouble()};
    }

    @Override
    public UVMap clone() {
        final UVMap map = new UVMap(uvType);

        for (final Map.Entry<Direction, Double[]> entry : this.map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            map.getMap().put(entry.getKey(), new Double[] {entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]});
        }

        return map;
    }

    public enum UVType {
        PERFACE, BOX
    }
}