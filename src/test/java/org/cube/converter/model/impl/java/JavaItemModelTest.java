package org.cube.converter.model.impl.java;

import com.viaversion.viaversion.libs.gson.JsonObject;
import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.parser.bedrock.geometry.BedrockGeometryParser;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaItemModelTest {

    @Test
    void omitsUntexturedJavaElementsAndPreservesSourceCubes() {
        final var geometry = geometry("""
            {"origin":[0,0,0],"size":[2,2,2],"uv":{}},
            {"origin":[2,0,0],"size":[2,2,2],"uv":{"north":{"uv":[0,0],"uv_size":[2,2]}}}
            """);
        final var javaModel = geometry.toJavaItemModel("example:item/actor", RotationType.POST_1_21_11);
        final JsonObject model = javaModel.compile();
        final var elements = model.getAsJsonArray("elements");
        assertEquals(1, elements.size());
        int renderedFaces = 0;
        for (var element : elements) {
            final JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
            assertFalse(faces.isEmpty());
            for (var face : faces.entrySet()) {
                renderedFaces++;
                final String texture = face.getValue().getAsJsonObject().get("texture").getAsString();
                assertTrue(texture.startsWith("#"));
                assertTrue(model.getAsJsonObject("textures").has(texture.substring(1)));
            }
        }
        assertEquals(1, renderedFaces);
        assertEquals(0, model.getAsJsonArray("groups").get(0).getAsJsonObject().getAsJsonArray("children").get(0).getAsInt());
        assertEquals(2, geometry.getParents().get(0).getCubes().size());
        assertEquals(2, javaModel.getParents().get(0).getCubes().size());
        assertTrue(geometry.getParents().get(0).getCubes().get(0).getUvMap().getUvMap().isEmpty());
        assertEquals(model, javaModel.compile());
    }

    @Test
    void retainsModelsThatOmitCubeUvData() {
        final var geometry = geometry("""
            {"origin":[-3,2,-3],"size":[6,8,6]},
            {"origin":[0,0,0],"size":[2,2,2],"uv":[0,0]}
            """);
        assertEquals(2, geometry.getParents().get(0).getCubes().size());
        assertTrue(geometry.getParents().get(0).getCubes().get(0).getUvMap().getUvMap().isEmpty());
        assertEquals(1, geometry.toJavaItemModel("example:item/actor", RotationType.POST_1_21_11)
                .compile().getAsJsonArray("elements").size());
    }

    @Test
    void usesFaceDimensionsForOmittedUvSizesAndPreservesAuthoredSizes() {
        final var geometry = geometry("""
            {"origin":[0,0,0],"size":[2,3,5],"uv":{
                "north":{"uv":[7,11]},"south":{"uv":[7,11]},
                "east":{"uv":[7,11]},"west":{"uv":[7,11]},
                "up":{"uv":[7,11]},"down":{"uv":[7,11]}}},
            {"origin":[0,0,0],"size":[2,3,5],"uv":{"north":{"uv":[7,11],"uv_size":[-4,6]}}}
            """);
        final var cubes = geometry.getParents().get(0).getCubes();
        final var faces = cubes.get(0).getUvMap().getUvMap();
        for (final var face : org.cube.converter.util.element.Direction.values()) {
            final float width = face == org.cube.converter.util.element.Direction.EAST || face == org.cube.converter.util.element.Direction.WEST ? 5 : 2;
            final float height = face == org.cube.converter.util.element.Direction.UP || face == org.cube.converter.util.element.Direction.DOWN ? 5 : 3;
            assertArrayEquals(new Float[] {7F, 11F, 7 + width, 11 + height}, faces.get(face));
        }
        assertArrayEquals(new Float[] {7F, 11F, 3F, 17F}, cubes.get(1).getUvMap().getUvMap()
                .get(org.cube.converter.util.element.Direction.NORTH));
    }

    @Test
    void exportsWhollyInvisibleGeometryWithNoJavaElements() {
        final var geometry = geometry("""
            {"origin":[0,0,0],"size":[2,2,2],"uv":{}}
            """);
        final JsonObject model = geometry.toJavaItemModel("example:item/actor", RotationType.POST_1_21_11).compile();
        assertTrue(model.getAsJsonArray("elements").isEmpty());
        assertTrue(model.getAsJsonArray("groups").get(0).getAsJsonObject().getAsJsonArray("children").isEmpty());
        assertEquals(1, geometry.getParents().get(0).getCubes().size());
    }

    @Test
    void preservesVisibleBoxFacesAndTheirTextureBindings() {
        final JsonObject model = convert("""
            {"origin":[0,0,0],"size":[2,2,2],"uv":[0,0]}
            """);
        final JsonObject faces = model.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
        assertEquals(6, faces.size());
        for (var face : faces.entrySet()) {
            final String key = face.getValue().getAsJsonObject().get("texture").getAsString().substring(1);
            assertEquals("example:item/actor", model.getAsJsonObject("textures").get(key).getAsString());
        }
    }

    private static JsonObject convert(final String cubes) {
        return geometry(cubes).toJavaItemModel("example:item/actor", RotationType.POST_1_21_11).compile();
    }

    private static BedrockGeometryModel geometry(final String cubes) {
        final String json = """
            {"format_version":"1.12.0","minecraft:geometry":[{
                "description":{"identifier":"geometry.example","texture_width":32,"texture_height":32},
                "bones":[{"name":"root","pivot":[0,0,0],"cubes":[%s]}]
            }]}
            """.formatted(cubes);
        return BedrockGeometryParser.parse(json).get(0);
    }

}
