package io.airbyte.integrations.javabase;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.config.DestinationConnectionSpecification;
import io.airbyte.config.StandardCheckConnectionInput;
import io.airbyte.config.StandardCheckConnectionOutput;
import io.airbyte.config.StandardDataSchema;
import io.airbyte.config.StandardDiscoverSchemaInput;
import io.airbyte.config.StandardDiscoverSchemaOutput;
import io.airbyte.config.StandardTapConfig;
import io.airbyte.config.StandardTargetConfig;
import io.airbyte.singer.SingerMessage;
import java.util.function.Consumer;

public interface Destination {
  DestinationConnectionSpecification spec();

  StandardCheckConnectionOutput check(JsonNode config);

  StandardDiscoverSchemaOutput discover(JsonNode config);

  Consumer<SingerMessage> write(JsonNode config, StandardDataSchema standardDataSchema);

}
