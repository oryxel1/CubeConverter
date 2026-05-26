package org.cube.converter.util.element;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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

    public static UVMap fromPerfaceUV(final JSONObject object) {
        final JSONObject uv = object.getJSONObject("uv");
        final UVMap map = new UVMap(UVType.PERFACE);
        
        putIfExist(Direction.NORTH, uv, map.getUvMap());
        putIfExist(Direction.EAST, uv, map.getUvMap());
        putIfExist(Direction.SOUTH, uv, map.getUvMap());
        putIfExist(Direction.WEST, uv, map.getUvMap());
        putIfExist(Direction.UP, uv, map.getUvMap());
        putIfExist(Direction.DOWN, uv, map.getUvMap());
        return map;
    }

    private static void putIfExist(final Direction direction, final JSONObject object, final Map<Direction, Float[]> map) {
        String name = direction.name().toLowerCase();
        if (object.containsKey(name)) {
            map.put(direction, getUVDirection(object.getJSONObject(name)));
        }
    }

    private static Float[] getUVDirection(JSONObject object) {
        if (object == null) {
            return new Float[] { 0F, 0F, 0F };
        }

        final JSONArray arrayUv = object.getJSONArray("uv");
        final JSONArray arrayUvSize = object.getJSONArray("uv_size");

        return new Float[] {arrayUv.getFloat(0), arrayUv.getFloat(1), arrayUv.getFloat(0) + arrayUvSize.getFloat(0), arrayUv.getFloat(1) + arrayUvSize.getFloat(1)};
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