#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)
CHANNEL=$1

if [ "$CHANNEL" = "ga" ]; then
  FILE_NAME=build/distributions/TabNine-$CURRENT_VERSION.zip
else
  FILE_NAME=$(ls -t build/distributions/TabNine-*.zip | grep $CHANNEL | head -1)
fi

STRIPPED_FILE_NAME=$(basename $FILE_NAME .zip)
RELEASE_NAME=${STRIPPED_FILE_NAME:8}

ci/bin/ghr -n "v$RELEASE_NAME" "$RELEASE_NAME" $FILE_NAME