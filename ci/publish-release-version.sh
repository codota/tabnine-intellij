#! /bin/bash

set -e

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)

./gradlew publishPlugin -Pchannel=production
