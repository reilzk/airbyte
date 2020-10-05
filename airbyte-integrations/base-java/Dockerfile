FROM airbyte/base:dev
COPY --from=openjdk:14.0.2-slim / /

WORKDIR /airbyte

ENV AIRBYTE_SPEC_CMD "bin/${APPLICATION} spec"
ENV AIRBYTE_CHECK_CMD "bin/${APPLICATION} check"
ENV AIRBYTE_DISCOVER_CMD "bin/${APPLICATION} discover"
ENV AIRBYTE_READ_CMD "bin/${APPLICATION} write"
