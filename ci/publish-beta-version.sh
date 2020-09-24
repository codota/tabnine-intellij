#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)

./gradlew publishPlugin -PexternalVersion=$CURRENT_VERSION.beta -Pchannel=beta
