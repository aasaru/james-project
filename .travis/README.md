Here we keep scripts that are run by Travis-CI (triggered by .travis.yml in root dir).

Travis has a limitation of how much output can be sent to console.
This is why we log into separate file and in the end of the build we tail it.

Also Travis allows any job to run max 50 minutes.
For this reason we have split the build into several individual jobs.
At first (phase 1) - we compile the project and install jar-s into $HOME/.m2 directory
Then we start to run tests in parallel.
