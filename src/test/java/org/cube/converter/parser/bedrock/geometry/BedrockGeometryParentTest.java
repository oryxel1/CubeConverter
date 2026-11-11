package org.cube.converter.parser.bedrock.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BedrockGeometryParentTest {
    @Test
    void nullRootParentsDoNotDiscardTheGeometryOrItsChildHierarchy() {
        for (final String parent : new String[]{"", " ,\"parent\":null"}) {
            final var models = BedrockGeometryParser.parse("""
                    {"format_version":"1.12.0","minecraft:geometry":[{
                      "description":{"identifier":"geometry.test","texture_width":64,"texture_height":64},
                      "bones":[{"name":"root"%s},{"name":"child","parent":"root"}]}]}
                    """.formatted(parent));
            assertEquals(1, models.size());
            final var bones = models.get(0).getParents();
            assertEquals(2, bones.size());
            assertEquals("", bones.get(0).getParent());
            assertEquals("root", bones.get(1).getParent());
        }
    }
}
