package org.cube.converter.util.element;

import com.viaversion.viaversion.libs.gson.JsonParser;
import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class UVMapTest {
    @Test
    void preservesAuthoredFaceRotationsThroughParsingCloningAndItemExport() {
        for (final Direction direction : Direction.values()) {
            for (final int rotation : new int[]{0, 90, 180, 270}) {
                final var document = JsonParser.parseString("""
                        {"format_version":"1.21.0","minecraft:geometry":[{
                          "description":{"identifier":"geometry.rotated","texture_width":64,"texture_height":32},
                          "bones":[{"name":"root","cubes":[{"origin":[0,0,0],"size":[4,6,8],"uv":{
                            "%s":{"uv":[12,10],"uv_size":[-4,6],"uv_rotation":%d}}}]}]}]}
                        """.formatted(direction.name().toLowerCase(Locale.ROOT), rotation)).getAsJsonObject();
                final var original = document.deepCopy();
                final var geometry = BedrockGeometryModel.fromJson(document).get(0);
                final var uv = geometry.getParents().get(0).getCubes().get(0).getUvMap();
                assertEquals((float) rotation, uv.getUvRotation().getOrDefault(direction, 0F));
                final var copy = uv.clone();
                assertEquals(uv.getUvRotation(), copy.getUvRotation());
                copy.rotate(direction, 1);
                assertEquals((float) ((rotation + 90) % 360), copy.getUvRotation().get(direction));
                assertEquals((float) rotation, uv.getUvRotation().getOrDefault(direction, 0F));
                final var normalized = uv.clone().toJavaPerfaceUV(64, 32);
                assertEquals(uv.getUvRotation(), normalized.getUvRotation());
                assertArrayEquals(new Float[]{3F, 5F, 2F, 8F}, normalized.getUvMap().get(direction));
                final var exported = geometry.toJavaItemModel("test:actor", RotationType.POST_1_21_11).compile();
                final var faces = exported.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
                assertEquals(1, faces.size());
                final var face = faces.getAsJsonObject(direction.name().toLowerCase(Locale.ROOT));
                assertEquals(rotation, face.has("rotation") ? face.get("rotation").getAsInt() : 0);
                assertEquals(original, document);
            }
        }
    }
}
