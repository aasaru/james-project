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
cd server/data/data-api && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-cassandra && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-file && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-jdbc && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-jmap && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-jmap-cassandra && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-ldap && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-library && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)
(
cd server/data/data-memory && ../../../mvnw test >> $BUILD_OUTPUT 2>&1
)



# The build finished without returning an error so dump a tail of the output
dump_output

# nicely terminate the ping output loop
kill $PING_LOOP_PID
