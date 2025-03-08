import org.cube.converter.data.bedrock.controller.BedrockRenderController;
import org.cube.converter.parser.bedrock.controller.BedrockControllerParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2025-2026 Oryxel and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
