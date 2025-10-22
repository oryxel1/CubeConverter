package org.cube.converter.util.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cube.converter.util.minecraft.UVUtil;

@Getter
public final class UVMap {
    private final UVType uvType;
    private final Map<Direction, Float[]> uvMap = new HashMap<>();
    private final Map<Direction, Float> uvRotation = new HashMap<>();

    private UVMap(UVType uvType) {
        this.uvType = uvType;
    }

    public void rotate(final Direction direction, final int roll) {
        final Float[] uv = this.uvMap.get(direction);
        if (uv == null) {
            return;
        }

        float rotation = (roll * 90) % 360;
        this.uvRotation.put(direction, rotation);
    }

    public UVMap toJavaPerfaceUV(final float textureWidth, final float textureHeight) {
        final UVMap map = new UVMap(UVType.PERFACE);

        for (final Map.Entry<Direction, Float[]> entry : this.uvMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            final Float[] uv = entry.getValue();

            for (int i = 0; i < uv.length; i++) {
                uv[i] = uv[i] * 16 / (i % 2 == 0 ? textureWidth : textureHeight);
            }

            map.getUvMap().put(entry.getKey(), uv);
        }

        return map;
    }

    public static UVMap fromBoxUV(final Position3V size, final Float[] offset, final boolean mirror) {
        final UVMap map = new UVMap(UVType.BOX);
        final List<UVUtil.Face> faces = UVUtil.toUvFaces(size, mirror);

        for (UVUtil.Face face : faces) {
            map.getUvMap().put(face.getDirection(), new Float[] { face.getStart().getX() + offset[0], face.getStart().getY() + offset[1], face.getEnd().getX() + offset[0], face.getEnd().getY() + offset[1] });
        }
        return map;
    }

    public static UVMap fromPerfaceUV(final JsonObject object) {
        final JsonObject uv = object.getAsJsonObject("uv");
        final UVMap map = new UVMap(UVType.PERFACE);
        
        putIfExist(Direction.NORTH, uv, map.getUvMap());
        putIfExist(Direction.EAST, uv, map.getUvMap());
        putIfExist(Direction.SOUTH, uv, map.getUvMap());
        putIfExist(Direction.WEST, uv, map.getUvMap());
        putIfExist(Direction.UP, uv, map.getUvMap());
        putIfExist(Direction.DOWN, uv, map.getUvMap());
        return map;
    }

    private static void putIfExist(final Direction direction, final JsonObject object, final Map<Direction, Float[]> map) {
        String name = direction.name().toLowerCase();
        if (object.has(name)) {
            map.put(direction, getUVDirection(object.getAsJsonObject(name)));
        }
    }

    private static Float[] getUVDirection(JsonObject object) {
        if (object == null) {
            return new Float[] { 0F, 0F, 0F };
        }

        final JsonArray arrayUv = object.getAsJsonArray("uv");
        final JsonArray arrayUvSize = object.getAsJsonArray("uv_size");

        return new Float[] {arrayUv.get(0).getAsFloat(), arrayUv.get(1).getAsFloat(), arrayUv.get(0).getAsFloat() + arrayUvSize.get(0).getAsFloat(), arrayUv.get(1).getAsFloat() + arrayUvSize.get(1).getAsFloat()};
    }

    @Override
    public UVMap clone() {
        final UVMap map = new UVMap(this.uvType);

        for (final Map.Entry<Direction, Float[]> entry : this.uvMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            map.getUvMap().put(entry.getKey(), new Float[] {entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]});
        }

        map.getUvRotation().putAll(this.uvRotation);

        return map;
    }

    public enum UVType {
        PERFACE, BOX
    }
}