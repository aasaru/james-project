#!/bin/bash
# https://stackoverflow.com/a/26082445/158257
# Abort on Error
set -e

export PING_SLEEP=120s
export WORKDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export BUILD_OUTPUT=$WORKDIR/build.out

touch $BUILD_OUTPUT

dump_output() {
   echo Tailing the last 2000 lines of output:
   tail -2000 $BUILD_OUTPUT
}
error_handler() {
  echo ERROR: An error was encountered with the build.
  dump_output
  exit 1
}
# If an error occurs, run our error handler to output a tail of the build
trap 'error_handler' ERR

# Set up a repeating loop to send some output to Travis.

bash -c "while true; do echo \$(date) - running server/testing tests ...; sleep $PING_SLEEP; done" &
PING_LOOP_PID=$!

# ADD COMMANDS HERE
(
cd $WORKDIR/../server/container/cli && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/cli-integration && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/core && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/filesystem-api && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/guice && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/lifecycle-api && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/mailbox-adapter && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/mailbox-jmx && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/metrics/metrics-es-reporter && ../../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/spring && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd $WORKDIR/../server/container/util && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)


# The build finished without returning an error so dump a tail of the output
dump_output

# nicely terminate the ping output loop
kill $PING_LOOP_PID
