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

package io.airbyte.integrations.javabase;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import io.airbyte.commons.functional.CloseableConsumer;
import io.airbyte.commons.io.IOs;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.Schema;
import io.airbyte.singer.SingerMessage;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoIt {
  private static final Logger LOGGER = LoggerFactory.getLogger(DoIt.class);

  private final Destination destination;

  public DoIt(Destination destination) {
    this.destination = destination;
  }

  private static JsonNode parseConfig(Map<String, Optional<String>> map, String key) {
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Required key %s not found in parsed args", key));
    }

    final String s = map.get(key).orElseThrow(
        () -> new IllegalArgumentException(String.format("%s must contain an argument", key)));
    return Jsons.deserialize(IOs.readFile(Path.of(s)));
  }

  private static <T> T parseConfig(Map<String, Optional<String>> map, String key, Class<T> klass) {
    final JsonNode jsonNode = parseConfig(map, key);
    return Jsons.object(jsonNode, klass);
  }

  private void run(String[] args) throws Exception {
    final Map<String, Optional<String>> parsed = new ArgWrangler().parse(args);

    // if check connection
    if (parsed.containsKey(Command.CHECK.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      System.out.println(Jsons.serialize(destination.check(config)));
    } else if (parsed.containsKey(Command.SPEC.toString().toLowerCase())) {
      System.out.println(Jsons.serialize(destination.spec()));
    } else if (parsed.containsKey(Command.DISCOVER.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      System.out.println(Jsons.serialize(destination.discover(config)));
    } else if (parsed.containsKey(Command.WRITE.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      final Schema schema = parseConfig(parsed, "schema", Schema.class);
      final CloseableConsumer<SingerMessage> consumer = destination.write(config, schema);

      final Scanner input = new Scanner(System.in);
      while (input.hasNextLine()) {
        Jsons.tryDeserialize(input.nextLine(), SingerMessage.class).ifPresent(consumer);
      }
      consumer.close();
    } else {
      throw new IllegalArgumentException("No command found.");
    }
  }

  public static void main(String[] args) throws Exception {

    final String destinationClass = System.getenv().get("DESTINATION_CLASS");
    final String destinationJarPath = System.getenv().get("DESTINATION_JAR_PATH");

    LOGGER.info("destination class: {}", destinationClass);
    LOGGER.info("destination jar path: {}", destinationJarPath);

    final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{Path.of(destinationJarPath).toUri().toURL()});
    Class<?> clazz = Class.forName(destinationClass, true, urlClassLoader);

        Destination destination = (Destination) clazz.getConstructor().newInstance();

    new DoIt(destination).run(args);
  }

}
