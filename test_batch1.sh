#!/usr/bin/env bash

(
cd core && ../mvnw --no-transfer-progress test
)
(
cd event-sourcing && ../mvnw --no-transfer-progress test
)
(
cd examples && ../mvnw --no-transfer-progress test
)
(
  cd javax-mail-extension && ../mvnw --no-transfer-progress test
)
(
cd json && ../mvnw --no-transfer-progress test
)
