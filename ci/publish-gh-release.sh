#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)
CHANNEL=$1

if [ "$CHANNEL" = "ga" ]; then
  FILE_VERSION=$CURRENT_VERSION
else
  FILE_VERSION=$CURRENT_VERSION.$CHANNEL
fi

ci/bin/ghr -n "v$CURRENT_VERSION-$CHANNEL" "$CURRENT_VERSION-$CHANNEL" build/distributions/TabNine-"$FILE_VERSION".zip