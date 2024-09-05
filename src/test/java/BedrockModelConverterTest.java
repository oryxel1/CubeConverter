import org.oryxel.cube.converter.FormatConverter;
import org.oryxel.cube.converter.enums.OverflowFixType;
import org.oryxel.cube.model.bedrock.BedrockGeometry;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.parser.bedrock.BedrockGeometrySerializer;
import org.oryxel.cube.parser.java.JavaModelSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2023-2024 Oryxel and contributors
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
public class BedrockModelConverterTest {

    public static void main(String[] args) {
        if (args.length < 1) return;
        Path testPath = Paths.get(args[0]);

        try (Stream<Path> stream = Files.walk(testPath)) {
            List<File> files = stream.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File file : files) {
                String path = file.getAbsolutePath().replace(args[0], "");

                if (path.startsWith("entity\\") || !path.toLowerCase().endsWith(".json"))
                    continue;

                String content = new String(Files.readAllBytes(file.toPath()));

                if (path.startsWith("models\\entity")) {
                    List<BedrockGeometry> geometries = BedrockGeometrySerializer.deserialize(content);
                    if (geometries.isEmpty())
                        continue;

                    int i = 0;
                    for (BedrockGeometry geometry : geometries) {
                        ItemModelData model = FormatConverter.bedrockToJava("test", geometry, OverflowFixType.SCALING);

                        String json = JavaModelSerializer.serializeToString(model);
                        File newPath = new File("test\\" + file.getName().replace(".json", "") + file.getAbsolutePath().hashCode() + "_" + i + ".json");

                        System.out.println(file.getAbsolutePath());
                        System.out.println(newPath.getAbsolutePath());

                        if (!newPath.exists())
                            newPath.createNewFile();

                        try (final FileWriter writer = new FileWriter(newPath)) {
                            writer.write(json);
                        }

                        i++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
