import org.cube.converter.data.bedrock.BedrockAttachableData;
import org.cube.converter.data.bedrock.BedrockEntityData;
import org.cube.converter.parser.bedrock.data.impl.BedrockAttachableParser;
import org.cube.converter.parser.bedrock.data.impl.BedrockEntityParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
public class BedrockDataParserTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        if (!args[0].endsWith("\\") && !args[0].endsWith("/")) {
            args[0] = args[0] + "\\";
        }

        final Path testPath = Paths.get(args[0]);

        try (Stream<Path> stream = Files.walk(testPath)) {
            List<File> files = stream.filter(Files::isRegularFile).map(Path::toFile).toList();

            for (final File file : files) {
                String path = file.getAbsolutePath().replace(args[0], "");

                if (!path.startsWith("entity") && !path.toLowerCase().startsWith("attachables")) {
                    continue;
                }

                if (!path.toLowerCase().endsWith(".json")) {
                    continue;
                }

                final String content = new String(Files.readAllBytes(file.toPath()));
                final BedrockAttachableData attachable = BedrockAttachableParser.parse(content);
                final BedrockEntityData entity = BedrockEntityParser.parse(content);

                if (entity == null && attachable == null) {
                    continue;
                }

                if (attachable != null) {
                    System.out.println("ATTACHABLE=" + attachable);
                } else {
                    System.out.println("ENTITY=" + entity);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
