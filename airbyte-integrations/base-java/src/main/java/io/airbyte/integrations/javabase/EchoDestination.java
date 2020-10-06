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
import io.airbyte.commons.functional.CloseableConsumer;
import io.airbyte.config.DestinationConnectionSpecification;
import io.airbyte.config.Schema;
import io.airbyte.config.StandardCheckConnectionOutput;
import io.airbyte.config.StandardDiscoverSchemaOutput;
import io.airbyte.singer.SingerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoDestination implements Destination {
  private static final Logger LOGGER = LoggerFactory.getLogger(EchoDestination.class);

  @Override
  public DestinationConnectionSpecification spec() {
    LOGGER.info("spec");
    return null;
  }

  @Override
  public StandardCheckConnectionOutput check(JsonNode config) {
    LOGGER.info("check. args: {}", config);
    return null;
  }

  @Override
  public StandardDiscoverSchemaOutput discover(JsonNode config) {
    LOGGER.info("discover. args: {}", config);
    return null;
  }

  @Override
  public CloseableConsumer<SingerMessage> write(JsonNode config, Schema schema) {
    LOGGER.info("write. args: {} schema: {}", config, schema);

    return new CloseableConsumer<>() {
      @Override
      public void close() {
        LOGGER.info("Closing " + EchoDestination.class);
      }

      @Override
      public void accept(SingerMessage singerMessage) {
        LOGGER.info("SingerMessage: {}", singerMessage);
      }
    };
  }

}
