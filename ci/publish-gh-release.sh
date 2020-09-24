#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)
CHANNEL=$1

ci/bin/ghr -n "v$CURRENT_VERSION-$CHANNEL" "$CURRENT_VERSION-$CHANNEL" build/distributions/TabNine-$CURRENT_VERSION.zip