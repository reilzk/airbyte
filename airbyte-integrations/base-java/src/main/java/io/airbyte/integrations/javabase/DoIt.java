package io.airbyte.integrations.javabase;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.commons.io.IOs;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.StandardDataSchema;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class DoIt {

  private final Destination destination;

  public DoIt(Destination destination) {
    this.destination = destination;
  }

  private static JsonNode parseConfig(Map<String, Optional<String>> map, String key) {
    if(!map.containsKey(key)){
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

  private void run(String[] args) throws IOException {
    final Map<String, Optional<String>> parsed = new ArgWrangler().parse(args);

    // if check connection
    if(parsed.containsKey(Command.CHECK.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      destination.check(config);
    } else if(parsed.containsKey(Command.SPEC.toString().toLowerCase())) {
      destination.spec();
    } else if(parsed.containsKey(Command.DISCOVER.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      destination.discover(config);
    } else if(parsed.containsKey(Command.WRITE.toString().toLowerCase())) {
      final JsonNode config = parseConfig(parsed, "config");
      final StandardDataSchema schema = parseConfig(parsed, "schema", StandardDataSchema.class);
      this.destination.write(config, schema);
    } else {
      throw new IllegalArgumentException("No command found.");
    }
  }

  public static void main(String[] args) throws IOException {
    new DoIt(new NoOpDestination()).run(args);
  }
}
