#! /bin/bash

set -e

VERSION=$1

[ -z "$VERSION" ] && echo "Version must be provided" && exit 1

[[ ! $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] && echo "Invalid version format for '$VERSION'" && exit 2

cd "$(dirname "$0")/.."

CURRENT_VERSION=$(./gradlew -q currentVersion)

CMP=$(ci/semver.sh $VERSION $CURRENT_VERSION)

(( $CMP <= 0)) && echo "New version must be newer than current" && exit 3

exit 0