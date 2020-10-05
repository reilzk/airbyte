package io.airbyte.integrations.javabase;

import io.airbyte.config.DestinationConnectionSpecification;
import io.airbyte.config.StandardCheckConnectionInput;
import io.airbyte.config.StandardCheckConnectionOutput;
import io.airbyte.config.StandardDiscoverSchemaInput;
import io.airbyte.config.StandardDiscoverSchemaOutput;
import io.airbyte.config.StandardTargetConfig;
import io.airbyte.singer.SingerMessage;
import java.util.function.Consumer;

public class NoOpDestination implements Destination {

  @Override
  public DestinationConnectionSpecification spec() {
    return null;
  }

  @Override
  public StandardCheckConnectionOutput check(StandardCheckConnectionInput input) {
    return null;
  }

  @Override
  public StandardDiscoverSchemaOutput discover(StandardDiscoverSchemaInput input) {
    return null;
  }

  @Override
  public Consumer<SingerMessage> write(StandardTargetConfig input) {
    return null;
  }
}
