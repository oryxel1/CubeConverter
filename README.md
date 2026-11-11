# CubeConverter
A library for reading and converting minecraft model for Java/Bedrock edition.

## Dependencies

Both artifacts use the same source and tests. Select one artifact for your application.
Replace `<version>` with a release tag or commit from this repository.

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

// Standard Gson types (com.google.gson).
implementation 'com.github.oryxel1.CubeConverter:cubeconverter:<version>'
```

For ViaVersion Gson types (`com.viaversion.viaversion.libs.gson`), use this dependency instead:

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
    maven { url = 'https://repo.viaversion.com' }
}

implementation 'com.github.oryxel1.CubeConverter:vv-json:<version>'
```

Use the named artifact. The JitPack repository dependency (`com.github.oryxel1:CubeConverter:<version>`) can include both modules.
The two artifacts define the same CubeConverter classes and cannot share a classpath.
Existing dependencies pinned to commits on the `vv-json` branch remain valid.

Run `./gradlew build publishToMavenLocal` to build and test both artifacts with Java 17 or later.
The build generates the ViaVersion sources and tests under `vv-json/build/` by replacing the Gson package name.
Edit `src/main/java` and `src/test/java` to change either artifact.
The ViaVersion source JAR contains the generated sources used by its compiler.

Render controllers now expose ordered material declarations through `materialAssignments()`.
Use this accessor when evaluating overrides because a pattern can occur more than once.
The map constructor and `materialsMap()` accessor remain available for existing callers, but maps retain only the last value for each pattern.

### Basic example
- Not now.

### Offset to correct position
- If you put the model on item display it can be in the wrong position, you can fix this by add extra (scale * 0.5F) to the y position. That should do the trick.

Feel free to write a wiki for this project if you want (~~**PLEASE.**~~)

## Useful resources
CubeConverter would not have been possible without the following projects:
- [Blockbench](https://github.com/JannisX11/blockbench/): Used for debugging/testing models, helped with figure out converting, **DIRECT CODE IMPLEMENT FOR** [UV MAPPING](https://github.com/Oryxel/CubeConverter/blob/main/src/main/java/org/oryxel/cube/util/UVUtil.java). 
