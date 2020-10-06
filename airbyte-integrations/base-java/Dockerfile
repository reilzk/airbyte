FROM airbyte/base:dev
# todo: hacking and whacking
COPY --from=openjdk:14.0.2-slim / /

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

ENV JAVA_HOME /usr/local/openjdk-14
ENV PATH $JAVA_HOME/bin:$PATH

# backwards compatibility shim
RUN { echo '#/bin/sh'; echo 'echo "$JAVA_HOME"'; } > /usr/local/bin/docker-java-home && chmod +x /usr/local/bin/docker-java-home && [ "$JAVA_HOME" = "$(docker-java-home)" ]

# https://adoptopenjdk.net/upstream.html
# >
# > What are these binaries?
# >
# > These binaries are built by Red Hat on their infrastructure on behalf of the OpenJDK jdk8u and jdk11u projects. The binaries are created from the unmodified source code at OpenJDK. Although no formal support agreement is provided, please report any bugs you may find to https://bugs.java.com/.
# >
ENV JAVA_VERSION 14.0.2

# todo: end

WORKDIR /airbyte
ENV APPLICATION base-java

COPY build/distributions/${APPLICATION}*.tar ${APPLICATION}.tar
#COPY build/distributions/base-java-0.1.0-alpha.tar ${APPLICATION}.tar

RUN tar xf ${APPLICATION}.tar --strip-components=1

ENV DESTINATION_CLASS io.airbyte.integrations.javabase.EchoDestination
ENV AIRBYTE_SPEC_CMD "./bin/base-java --spec"
ENV AIRBYTE_CHECK_CMD "./bin/base-java --check"
ENV AIRBYTE_DISCOVER_CMD "./bin/base-java --discover"
ENV AIRBYTE_READ_CMD "./bin/base-java --write"
