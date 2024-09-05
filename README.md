# CubeConverter
A library for reading and converting minecraft model for Java/Bedrock edition.

### Basic example
- I'm too lazy to write an actual wiki :P
```java
// This contains model identifier, model textures, model geometries, variables.
// ---- For custom entity.
final BedrockGeneralData data = BedrockGeneralSerializer.deserialize(content);

// Convert from bedrock to java model.
final List<BedrockGeometry> geometries = BedrockGeometrySerializer.deserialize(content);

geometries.forEach(geometry -> {
    // Item model on java have a limited size, you can clamp it or scale it down and the scale it up in-game
    // SCALING is recommend, CLAMP is worse than scaling and half broken anyway.
    ItemModelData model = FormatConverter.bedrockToJava("texture-location", geometry, OverflowFixType.SCALING);
});
```

### Offset to correct position
- If you put the model on item display it can be in the wrong position, you can fix this by add extra (scale * 0.5F) to the y position. That should do the trick.

Feel free to write a wiki for this project if you want (~~**PLEASE.**~~)

## Useful resources
CubeConverter would not have been possible without the following projects:
- [Blockbench](https://github.com/JannisX11/blockbench/): Used for debugging/testing models, helped with figure out converting, **DIRECT CODE IMPLEMENT FOR** [UV MAPPING](https://github.com/Oryxel/CubeConverter/blob/main/src/main/java/org/oryxel/cube/util/UVUtil.java). 
