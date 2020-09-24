#! /bin/bash

set -e

cd "$(dirname "$0")/.."

VERSION=$1
CHANNEL=$2

ci/bin/ghr -n "v$VERSION-$CHANNEL" "$VERSION-$CHANNEL" build/distributions/TabNine-$VERSION.zip