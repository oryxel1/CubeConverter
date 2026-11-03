package org.cube.converter.converter;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.libs.gson.JsonObject;
import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.parser.bedrock.geometry.BedrockGeometryParser;
import org.cube.converter.util.GsonUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatConverterTest {

    @Test
    void preservesOptionalGeometryFieldsAndNativeTextureDefaults() {
        for (final boolean modern : new boolean[]{false, true}) {
            for (final boolean bones : new boolean[]{false, true}) {
                final JsonObject document = modern ? geometryJson("[]")
                        : GsonUtil.getGson().fromJson("{\"geometry.example\":{}}", JsonObject.class);
                final JsonObject geometry = modern ? document.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject()
                        : document.getAsJsonObject("geometry.example");
                final JsonObject description = modern ? geometry.getAsJsonObject("description") : geometry;
                final String width = modern ? "texture_width" : "texturewidth";
                final String height = modern ? "texture_height" : "textureheight";
                description.remove(width);
                description.remove(height);
                if (bones) geometry.add("bones", new JsonArray());
                else geometry.remove("bones");
                for (int overrides = 0; overrides < 4; overrides++) {
                    if ((overrides & 1) != 0) description.addProperty(width, 32);
                    else description.add(width, JsonNull.INSTANCE);
                    if ((overrides & 2) != 0) description.addProperty(height, 16);
                    else description.remove(height);
                    final JsonObject original = document.deepCopy();
                    final var models = BedrockGeometryParser.parse(document);
                    assertEquals(1, models.size());
                    final var parsed = models.get(0);
                    assertEquals((overrides & 1) != 0 ? 32 : 64, parsed.getTextureSize().getX());
                    assertEquals((overrides & 2) != 0 ? 16 : 64, parsed.getTextureSize().getY());
                    assertTrue(parsed.getParents().isEmpty());
                    for (final RotationType rotation : RotationType.values()) {
                        final var model = parsed.toJavaItemModel("example:item/actor", rotation);
                        assertEquals(1, model.getScale());
                        assertTrue(finiteExport(model).getAsJsonArray("elements").isEmpty());
                    }
                    assertEquals(original, document);
                }
            }
        }
    }

    @Test
    void exportsGeometryWithoutBonesAtUnitScaleForEveryRotationMode() {
        final var geometry = BedrockGeometryParser.parse(geometryJson("[]")).get(0);
        for (final RotationType rotation : RotationType.values()) {
            final var model = geometry.toJavaItemModel("example:item/actor", rotation);
            final JsonObject json = finiteExport(model);
            assertEquals(1, model.getScale());
            assertTrue(json.getAsJsonArray("elements").isEmpty());
            assertTrue(json.getAsJsonArray("groups").isEmpty());
        }
        assertTrue(geometry.getParents().isEmpty());
    }

    @Test
    void preservesLocatorOnlyBonesAndSourceGeometryAtUnitScale() {
        final JsonObject source = geometryJson("""
            [{"name":"root","pivot":[2,3,4],"rotation":[5,10,15],
              "locators":{"attachment":[1,2,3]}},
             {"name":"attachment","parent":"root","pivot":[-2,8,1],"rotation":[0,20,0],
              "cubes":[],"locators":{"trail":{"offset":[0,1,0],"rotation":[0,45,0]}}}]
            """);
        final JsonObject original = source.deepCopy();
        final var geometry = BedrockGeometryParser.parse(source).get(0);
        final var model = geometry.toJavaItemModel("example:item/actor", RotationType.POST_1_21_11);
        final JsonObject json = finiteExport(model);
        assertEquals(1, model.getScale());
        assertTrue(json.getAsJsonArray("elements").isEmpty());
        assertEquals(2, json.getAsJsonArray("groups").size());
        assertEquals(2, model.getParents().size());
        for (int index = 0; index < geometry.getParents().size(); index++) {
            final var before = geometry.getParents().get(index);
            final var after = model.getParents().get(index);
            assertNotSame(before, after);
            assertEquals(before.getName(), after.getName());
            assertEquals(before.getParent(), after.getParent());
            assertArrayEquals(before.getPivot().toArray(), after.getPivot().toArray());
            assertArrayEquals(before.getRotation().toArray(), after.getRotation().toArray());
            assertTrue(before.getCubes().isEmpty());
            assertTrue(after.getCubes().isEmpty());
            assertTrue(json.getAsJsonArray("groups").get(index).getAsJsonObject().getAsJsonArray("children").isEmpty());
        }
        assertArrayEquals(new float[]{2, 3, 4}, geometry.getParents().get(0).getPivot().toArray());
        assertArrayEquals(new float[]{5, 10, 15}, geometry.getParents().get(0).getRotation().toArray());
        assertEquals("root", geometry.getParents().get(1).getParent());
        assertEquals(original, source);
        assertEquals(json, model.compile());
    }

    @Test
    void preservesPopulatedGeometryFittingAndSourceCoordinates() {
        final var geometry = BedrockGeometryParser.parse(geometryJson("""
            [{"name":"root","pivot":[0,0,0],"cubes":[
                {"origin":[-64,0,0],"size":[64,64,64],"uv":[0,0]}]}]
            """)).get(0);
        final var source = geometry.getParents().get(0).getCubes().get(0);
        final var model = geometry.toJavaItemModel("example:item/actor", RotationType.POST_1_21_11);
        final JsonObject json = finiteExport(model);
        assertEquals(4, model.getScale());
        assertEquals(1, json.getAsJsonArray("elements").size());
        final var converted = model.getParents().get(0).getCubes().get(0);
        assertNotSame(source, converted);
        assertArrayEquals(new float[]{16, 16, 16}, converted.getSize().toArray());
        assertArrayEquals(new float[]{-16, 0, 0}, converted.getPosition().toArray());
        assertArrayEquals(new float[]{64, 64, 64}, source.getSize().toArray());
        assertArrayEquals(new float[]{-64, 0, 0}, source.getPosition().toArray());
        assertEquals(6, json.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces").size());
    }

    private static JsonObject finiteExport(final JavaItemModel model) {
        assertTrue(Float.isFinite(model.getScale()));
        assertTrue(model.getScale() > 0);
        final JsonObject export = new JsonObject();
        export.addProperty("scale", model.getScale());
        export.add("model", model.compile());
        final var gson = GsonUtil.getGson();
        final JsonObject decoded = gson.fromJson(gson.toJson(export), JsonObject.class);
        assertEquals(export, decoded);
        assertEquals(model.getScale(), decoded.get("scale").getAsFloat());
        return decoded.getAsJsonObject("model");
    }

    private static JsonObject geometryJson(final String bones) {
        return GsonUtil.getGson().fromJson("""
            {"format_version":"1.12.0","minecraft:geometry":[{
                "description":{"identifier":"geometry.example","texture_width":64,"texture_height":64},
                "bones":%s
            }]}
            """.formatted(bones), JsonObject.class);
    }
}
