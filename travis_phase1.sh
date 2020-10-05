#!/usr/bin/env bash

# This is phase #1 where we compile code and build all snapshot.jar files
# and install them into $HOME/.m2 directory

./mvnw -q -DskipTests=true install

# besides above we also need following artifacts:
# org.apache.james:apache-james-mailbox-api:jar:3.6.0-SNAPSHOT
# and
# org.apache.james:apache-james-mailbox-api:jar:tests:3.6.0-SNAPSHOT
(
cd mailbox/api
../../mvnw -q install
)

# Now we should have all dependencies populated and we can start running test sin parallel
# All processes in phase #2 get the same $HOME/.m2 directory contents we built in this step

