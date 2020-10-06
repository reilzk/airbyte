/*
 * MIT License
 *
 * Copyright (c) 2020 Airbyte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.airbyte.integrations.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import io.airbyte.commons.functional.CloseableConsumer;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.DestinationConnectionSpecification;
import io.airbyte.config.Schema;
import io.airbyte.config.StandardCheckConnectionOutput;
import io.airbyte.config.StandardCheckConnectionOutput.Status;
import io.airbyte.config.StandardDiscoverSchemaOutput;
import io.airbyte.config.Stream;
import io.airbyte.integrations.javabase.Destination;
import io.airbyte.singer.SingerMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvDestination implements Destination {
  private static final Logger LOGGER = LoggerFactory.getLogger(CsvDestination.class);

  private final URLClassLoader classLoader;

  public CsvDestination() {
    classLoader = getClassLoader();

  }

  private URLClassLoader getClassLoader() {
    final String destinationJarPath = System.getenv().get("DESTINATION_JAR_PATH");

    try {
      return new URLClassLoader(new URL[]{Path.of(destinationJarPath).toUri().toURL()});
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public DestinationConnectionSpecification spec() throws IOException {
    final String resourceString = Resources.toString(classLoader.findResource("spec.json"), StandardCharsets.UTF_8);
    return Jsons.deserialize(resourceString, DestinationConnectionSpecification.class);
  }

  @Override
  public StandardCheckConnectionOutput check(JsonNode config) {
    // todo (cgardens) - inject local volume.
    try {
      FileUtils.forceMkdir(new File(config.get("destination_path").asText()));
    } catch (IOException e) {
      return new StandardCheckConnectionOutput().withStatus(Status.FAILURE).withMessage(e.getMessage());
    }
    return new StandardCheckConnectionOutput().withStatus(Status.SUCCESS);
  }

  @Override
  public StandardDiscoverSchemaOutput discover(JsonNode config) {
    throw new RuntimeException("Not Implemented");
  }

  @Override
  public CloseableConsumer<SingerMessage> write(JsonNode config, Schema schema)
      throws IOException {
    final Path destinationDir = Path.of(config.get("destination_path").asText());

    FileUtils.forceMkdir(destinationDir.toFile());

    final long now = Instant.now().toEpochMilli();
    final Map<String, WriteConfig> map = new HashMap<>();
    for (final Stream stream : schema.getStreams()) {
      Path tmpPath = destinationDir.resolve(stream.getName() + "_" + now + ".csv");
      Path finalPath = destinationDir.resolve(stream.getName() + ".csv");
      final FileWriter fileWriter = new FileWriter(tmpPath.toFile());
      map.put(stream.getName(), new WriteConfig(fileWriter, tmpPath, finalPath));
    }

    return new OmNom(map);
  }

  public static class WriteConfig {

    private final FileWriter fileWriter;
    private final Path tmpPath;
    private final Path finalPath;

    public WriteConfig(FileWriter fileWriter, Path tmpPath, Path finalPath) {
      this.fileWriter = fileWriter;
      this.tmpPath = tmpPath;
      this.finalPath = finalPath;
    }

    public FileWriter getFileWriter() {
      return fileWriter;
    }

    public Path getTmpPath() {
      return tmpPath;
    }

    public Path getFinalPath() {
      return finalPath;
    }

  }

  public static class OmNom implements CloseableConsumer<SingerMessage> {

    private final Map<String, WriteConfig> map;

    public OmNom(Map<String, WriteConfig> map) {
      this.map = map;
    }

    @Override
    public void close() throws Exception {
      for (final WriteConfig writeConfig : map.values()) {
        Files.move(writeConfig.getTmpPath(), writeConfig.getFinalPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }

    @Override
    public void accept(SingerMessage singerMessage) {
      if (map.containsKey(singerMessage.getStream())) {
        try {
          map.get(singerMessage.getStream()).getFileWriter().write(Jsons.serialize(singerMessage.getValue()));
        } catch (IOException e) {
          // todo - ?
          throw new RuntimeException(e);
        }
      }
    }

  }

}
