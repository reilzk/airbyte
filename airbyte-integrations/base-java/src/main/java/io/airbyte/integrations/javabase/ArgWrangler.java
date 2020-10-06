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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgWrangler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArgWrangler.class);
  private static final String SINGER_EXECUTABLE = "SINGER_EXECUTABLE";

  private static final OptionGroup commandGroup = new OptionGroup();

  static {
    commandGroup.setRequired(true);
    commandGroup.addOption(Option.builder()
        .longOpt(Command.SPEC.toString().toLowerCase())
        .desc("outputs the json configuration specification")
        .build());
    commandGroup.addOption(Option.builder()
        .longOpt(Command.CHECK.toString().toLowerCase())
        .desc("checks the config can be used to connect")
        .build());
    commandGroup.addOption(Option.builder()
        .longOpt(Command.DISCOVER.toString().toLowerCase())
        .desc("outputs a catalog describing the source's schema")
        .build());
    commandGroup.addOption(Option.builder()
        .longOpt(Command.READ.toString().toLowerCase())
        .desc("reads the source and outputs messages to STDOUT")
        .build());
    commandGroup.addOption(Option.builder()
        .longOpt(Command.WRITE.toString().toLowerCase())
        .desc("writes messages from STDIN to the integration")
        .build());
  }

  public Map<String, Optional<String>> parse(final String[] args) throws IOException {
    final Command command = parseCommand(args);
    return parseOptions(args, command);
    // final String newArgs = toCli(newOptions);

    // final String singerExecutable = System.getenv(SINGER_EXECUTABLE);
    // Preconditions.checkNotNull(singerExecutable, SINGER_EXECUTABLE + " environment variable cannot be
    // null.");
    //
    // final String cmd = singerExecutable + newArgs;
    // final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    // final Process process = processBuilder.start();
    //
    // transformOutput(process.getInputStream(), options); // mapping from singer structs back to
    // airbyte and then pipe to stdout.
  }

  private static Command parseCommand(String[] args) {
    final CommandLineParser parser = new RelaxedParser();
    final HelpFormatter helpFormatter = new HelpFormatter();

    final Options options = new Options();
    options.addOptionGroup(commandGroup);

    try {
      final CommandLine parsed = parser.parse(options, args);
      return Command.valueOf(parsed.getOptions()[0].getLongOpt().toUpperCase());
      // if discover, then validate, etc...
    } catch (ParseException e) {
      LOGGER.error(e.toString());
      helpFormatter.printHelp("java-base", options);
      throw new IllegalArgumentException();
    }
  }

  private static Map<String, Optional<String>> parseOptions(String[] args, Command command) {
    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter helpFormatter = new HelpFormatter();

    final Options options = new Options();
    options.addOptionGroup(commandGroup); // so that the parser does not throw an exception when encounter command args.

    if (command.equals(Command.CHECK)) {
      options.addOption(Option.builder().longOpt("config").desc("path to the json configuration file").hasArg(true).required(true).build());
    }

    if (command.equals(Command.DISCOVER)) {
      options.addOption(Option.builder().longOpt("config").desc("path to the json configuration file").hasArg(true).required(true).build());
      options.addOption(Option.builder().longOpt("schema").desc("output path for the discovered schema").hasArg(true).build());
    }

    if (command.equals(Command.READ)) {
      options.addOption(Option.builder().longOpt("config").desc("path to the json configuration file").hasArg(true).required(true).build());
      options.addOption(Option.builder().longOpt("schema").desc("input path for the schema").hasArg(true).build());
      options.addOption(Option.builder().longOpt("state").desc("path to the json-encoded state file").hasArg(true).build());
    }

    if (command.equals(Command.READ)) {
      options.addOption(Option.builder().longOpt("config").desc("path to the json configuration file").hasArg(true).required(true).build());
    }

    try {
      final CommandLine parse = parser.parse(options, args);
      return Arrays.stream(parse.getOptions()).collect(Collectors.toMap(Option::getLongOpt, option -> Optional.ofNullable(option.getValue())));
    } catch (ParseException e) {
      LOGGER.error(e.toString());
      helpFormatter.printHelp(command.toString().toLowerCase(), options);
      return null;
    }
  }

  // https://stackoverflow.com/questions/33874902/apache-commons-cli-1-3-1-how-to-ignore-unknown-arguments
  private static class RelaxedParser extends DefaultParser {

    @Override
    public CommandLine parse(final Options options, final String[] arguments) throws ParseException {
      final List<String> knownArgs = new ArrayList<>();
      for (int i = 0; i < arguments.length; i++) {
        if (options.hasOption(arguments[i])) {
          knownArgs.add(arguments[i]);
          if (i + 1 < arguments.length && options.getOption(arguments[i]).hasArg()) {
            knownArgs.add(arguments[i + 1]);
          }
        }
      }
      return super.parse(options, knownArgs.toArray(new String[0]));
    }

  }

  public static void main(String[] args) throws IOException {
    System.out.println(new ArgWrangler().parse(args));
  }

}
