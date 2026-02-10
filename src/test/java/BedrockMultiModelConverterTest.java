import org.cube.converter.converter.enums.RotationType;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.parser.bedrock.geometry.BedrockGeometryParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class BedrockMultiModelConverterTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        if (!args[0].endsWith("\\") && !args[0].endsWith("/")) {
            args[0] = args[0] + "\\";
        }

        final File dir = new File("test");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        final Path testPath = Paths.get(args[0]);

        try (final Stream<Path> stream = Files.walk(testPath)) {
            List<File> files = stream.filter(Files::isRegularFile).map(Path::toFile).toList();

            for (final File file : files) {
                String path = file.getAbsolutePath().replace(args[0].replace("/", "\\"), "");

                if (path.startsWith("entity\\") || !path.toLowerCase().endsWith(".json")) {
                    continue;
                }

                final String content = new String(Files.readAllBytes(file.toPath()));

                if (path.startsWith("models\\entity") || path.startsWith("\\models\\entity")) {
                    List<BedrockGeometryModel> geometries = BedrockGeometryParser.parse(content);
                    if (geometries.isEmpty()) {
                        continue;
                    }

                    for (final BedrockGeometryModel geometry : geometries) {
                        final List<JavaItemModel> models = geometry.toJavaMultiItemModel("test");

                        System.out.println(file.getAbsolutePath());

                        int i = 0;
                        for (JavaItemModel model : models) {
                            final String json = model.compile().toString();
                            File newPath = new File("test\\" + file.getName().replace(".json", "") + file.getAbsolutePath().hashCode() + "_" + i + ".json");
                            System.out.println(newPath.getAbsolutePath());

                            if (!newPath.exists()) {
                                newPath.createNewFile();
                            }

                            try (final FileWriter writer = new FileWriter(newPath)) {
                                writer.write(json);
                            }
                            i++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
