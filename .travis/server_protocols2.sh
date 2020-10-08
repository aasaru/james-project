#!/bin/bash
# Travis-CI has a 4MB limitation for log length.
# For this reason we write the output to a separate file and only show tail of this file.

# Abort on Error
set -e

export PING_SLEEP=120s
export ROOTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
export BUILD_OUTPUT=$ROOTDIR/travis_console.log

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
bash -c "while true; do echo \$(date) - running tests ...; sleep $PING_SLEEP; done" &
PING_LOOP_PID=$!

# Actual commands to run tests
# We need mailbox-test artifact
( cd $ROOTDIR/mailbox && ../mvnw install >> $BUILD_OUTPUT 2>&1 )

( cd $ROOTDIR/server/protocols/jwt && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )
( cd $ROOTDIR/server/protocols/protocols-imap4 && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )
( cd $ROOTDIR/server/protocols/protocols-library && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )
( cd $ROOTDIR/server/protocols/protocols-lmtp && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )

( cd $ROOTDIR/server/protocols/protocols-managesieve && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )
( cd $ROOTDIR/server/protocols/protocols-pop3 && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )
( cd $ROOTDIR/server/protocols/protocols-smtp && ../../../mvnw --no-transfer-progress test >> $BUILD_OUTPUT 2>&1 )


echo BUILD PASSED.
dump_output

# terminate the ping output loop
kill $PING_LOOP_PID
