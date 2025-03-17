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
    private final Map<Direction, Float[]> map = new HashMap<>();

    private UVMap(UVType uvType) {
        this.uvType = uvType;
    }

    public void rotate(final Direction direction, final int roll) {
        final Float[] uvAddSize = this.map.get(direction);
        if (uvAddSize == null) {
            return;
        }

        final Float[] uv = new Float[] { uvAddSize[0], uvAddSize[1], uvAddSize[2] - uvAddSize[0], uvAddSize[3] - uvAddSize[1] };

        float rotation = (roll * 90) % 360;
        while (rotation > 0) {
            float a = uv[0];
            uv[0] = uv[2];
            uv[2] = uv[3];
            uv[3] = uv[1];
            uv[1] = a;
            rotation -= 90;
        }

        this.map.put(direction, new Float[] { uv[0], uv[1], uv[0] + uv[2], uv[1] + uv[3] });
    }

    public UVMap toJavaPerfaceUV(final float textureWidth, final float textureHeight) {
        final UVMap map = new UVMap(UVType.PERFACE);

        for (final Map.Entry<Direction, Float[]> entry : this.map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            final Float[] uv = entry.getValue();

            for (int i = 0; i < uv.length; i++) {
                uv[i] = uv[i] * 16 / (i % 2 == 0 ? textureWidth : textureHeight);
            }

            map.getMap().put(entry.getKey(), uv);
        }

        return map;
    }

    public static UVMap fromBoxUV(final Position3V size, final Float[] offset, final boolean mirror) {
        final UVMap map = new UVMap(UVType.BOX);
        final List<UVUtil.Face> faces = UVUtil.toUvFaces(size, mirror);

        for (UVUtil.Face face : faces) {
            map.getMap().put(face.getDirection(), new Float[] { face.getStart().getX() + offset[0], face.getStart().getY() + offset[1], face.getEnd().getX() + offset[0], face.getEnd().getY() + offset[1] });
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
        final UVMap map = new UVMap(uvType);

        for (final Map.Entry<Direction, Float[]> entry : this.map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            map.getMap().put(entry.getKey(), new Float[] {entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]});
        }

        return map;
    }

    public enum UVType {
        PERFACE, BOX
    }
}