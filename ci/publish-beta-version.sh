#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)
TIMESTAMP=$(date '+%Y%m%d%H%M%S')

./gradlew publishPlugin -PexternalVersion=$CURRENT_VERSION-beta.$TIMESTAMP -Pchannel=beta
