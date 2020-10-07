#!/usr/bin/env bash
# Travis has a 4MB limitation for log length.
# For this reason we write the output to a separate file

#Exit immediately if a command exits with a non-zero status.
set -e

export PING_SLEEP=120s
export WORKDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export BUILD_OUTPUT=$WORKDIR/travis_phase1.out

echo 'WORKDIR:'
echo $WORKDIR

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

# Set up a repeating loop to send some output to Travis (so it would consider the process alive)
bash -c "while true; do echo \$(date) - building ...; sleep $PING_SLEEP; done" &
PING_LOOP_PID=$!

cd..
./mvnw -DskipTests=true install >> $BUILD_OUTPUT 2>&1

# The build finished without returning an error so dump a tail of the output
dump_output

# terminate the ping output loop
kill $PING_LOOP_PID
