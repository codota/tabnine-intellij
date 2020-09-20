#! /bin/bash

set -e

VERSION=$1

cd "$(dirname "$0")/.."

ci/validate-version.sh $VERSION

CURRENT_VERSION=$(./gradlew -q currentVersion)

sed -i "s/^version '$CURRENT_VERSION'/version '$VERSION'/g" build.gradle