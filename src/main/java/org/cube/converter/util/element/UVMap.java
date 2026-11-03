package org.cube.converter.util.element;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        float rotation = (this.uvRotation.getOrDefault(direction, 0F) + roll * 90) % 360;
        if (rotation < 0) rotation += 360;
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

        map.getUvRotation().putAll(this.uvRotation);
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
        if (uv == null) {
            return map;
        }

        final Position3V size = new Position3V(object.getAsJsonArray("size"));
        for (final Direction direction : Direction.values()) {
            final String name = direction.name().toLowerCase(Locale.ROOT);
            if (!uv.has(name)) continue;
            final JsonObject face = uv.getAsJsonObject(name);
            map.uvMap.put(direction, getUVDirection(face, direction, size));
            if (face.has("uv_rotation")) {
                map.uvRotation.put(direction, face.get("uv_rotation").getAsFloat());
            }
        }
        return map;
    }

    private static Float[] getUVDirection(final JsonObject object, final Direction direction, final Position3V size) {
        if (object == null) {
            return new Float[] { 0F, 0F, 0F, 0F };
        }

        final JsonArray arrayUv = object.getAsJsonArray("uv");
        final JsonArray arrayUvSize = object.getAsJsonArray("uv_size");

        final float width = arrayUvSize != null ? arrayUvSize.get(0).getAsFloat()
                : direction == Direction.EAST || direction == Direction.WEST ? size.getZ() : size.getX();
        final float height = arrayUvSize != null ? arrayUvSize.get(1).getAsFloat()
                : direction == Direction.UP || direction == Direction.DOWN ? size.getZ() : size.getY();
        final float u = arrayUv.get(0).getAsFloat(), v = arrayUv.get(1).getAsFloat();
        return new Float[] {u, v, u + width, v + height};
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
