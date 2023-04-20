#! /bin/bash

set -e

ID_STR=$(cat Tabnine/src/main/resources/META-INF/plugin.xml | grep "<id>.*</id>" | xargs)
EXPECTED_ID="<id>com.tabnine.TabNine</id>"
if [ "$ID_STR" != "$EXPECTED_ID" ]; then
  echo "Check failed: '$ID_STR' is not '$EXPECTED_ID'"
  exit 1
fi

ID_STR=$(cat TabnineSelfHosted/src/main/resources/META-INF/plugin.xml | grep "<id>.*</id>" | xargs)
EXPECTED_ID="<id>com.tabnine.TabNine-Enterprise</id>"
if [ "$ID_STR" != "$EXPECTED_ID" ]; then
  echo "Check failed: '$ID_STR' is not '$EXPECTED_ID'"
  exit 1
fi