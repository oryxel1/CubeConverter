import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.parser.bedrock.controller.BedrockControllerParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
public class BedrockControllerParserTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        if (!args[0].endsWith("\\") && !args[0].endsWith("/")) {
            args[0] = args[0] + "\\";
        }

        final Path testPath = Paths.get(args[0]);

        try (final Stream<Path> stream = Files.walk(testPath)) {
            List<File> files = stream.filter(Files::isRegularFile).map(Path::toFile).toList();

            for (final File file : files) {
                final String path = file.getAbsolutePath().replace(args[0], "");

                if (!path.startsWith("render_controllers") || !path.toLowerCase().endsWith(".json")) {
                    continue;
                }

                final String content = new String(Files.readAllBytes(file.toPath()));

                if (path.startsWith("render_controllers")) {
                    final List<BedrockRenderController> controller = BedrockControllerParser.parse(content);
                    System.out.println(controller);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
