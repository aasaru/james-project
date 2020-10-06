#!/usr/bin/env bash
#Exit immediately if a command exits with a non-zero status.
set -e
#Propagate script exit code to the output of this program
EXIT_STATUS=0

# This is phase #1 where we compile code and build all snapshot.jar files
# and install them into $HOME/.m2 directory

# prepare
docker pull linagora/mock-smtp-server:latest

# do the main work
./mvnw -q -T 8 -DskipTests=true install || EXIT_STATUS=$?

# besides above we also need following artifacts:
# org.apache.james:apache-james-mailbox-api:jar:3.6.0-SNAPSHOT
# and
# org.apache.james:apache-james-mailbox-api:jar:tests:3.6.0-SNAPSHOT
(
cd mailbox/api
../../mvnw -q install || EXIT_STATUS=$?
)

# Now we should have all dependencies populated and we can start running test sin parallel
# All processes in phase #2 get the same $HOME/.m2 directory contents we built in this step

exit ${EXIT_STATUS}
