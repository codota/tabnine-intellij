#!/usr/bin/env bash

set -e

if [[ -z "$JAVA_HOME" ]]; then
  echo "Please define JAVA_HOME"
  exit -1
fi

VERSION=$1

NO_JDK=${NO_JDK:--no-jbr}
NO_JDK_SUPPORTED=${NO_JDK_SUPPORTED:-true}

if [ $NO_JDK_SUPPORTED == false ]; then
  unset NO_JDK
fi

VERIFIER_VERSION=${VERIFIER_VERSION:-1.203}
PRODUCT=${PRODUCT:-idea}

IDE_TYPE=${IDE_TYPE:-ideaIU}

SOURCE_DIR=`dirname "$(cd "$(dirname "$0")"; pwd)/$(basename "$0")"`

cd ${SOURCE_DIR}/..

./gradlew buildPlugin -Penv=production -PuploadMap=false -Pobfuscate=false $ADDITIONAL_BUILD_PARAMS

cd -

DOWNLOAD_DIR=${DOWNLOAD_DIR:-$HOME}
TMP_DIR=${TMP_DIR:-/tmp}
cd $TMP_DIR

IDE_DIR=$DOWNLOAD_DIR/${IDE_TYPE}-${VERSION}
if [[ ! -d $IDE_DIR ]]; then
    DOWNLOAD_FILE=${IDE_TYPE}-${VERSION}$NO_JDK.tar.gz
    if [[ ! -f  $DOWNLOAD_FILE ]]; then
      DOWNLOAD_URL=https://download-cf.jetbrains.com/$PRODUCT/$IDE_TYPE-$VERSION$NO_JDK.tar.gz
      echo "DOWNLOADING $DOWNLOAD_URL"
        curl -f -L --output $DOWNLOAD_FILE $DOWNLOAD_URL
    fi
    mkdir -p $IDE_DIR
    tar xzvf $DOWNLOAD_FILE -C $IDE_DIR
fi

VERIFIER_JAR=~/verifier-tool/verifier-$VERIFIER_VERSION-all.jar
if [[ ! -f $VERIFIER_JAR ]]; then
    mkdir -p ~/verifier-tool
    curl -L --output $VERIFIER_JAR https://dl.bintray.com/jetbrains/intellij-plugin-service/org/jetbrains/intellij/plugins/verifier-cli/$VERIFIER_VERSION/verifier-cli-$VERIFIER_VERSION-all.jar
fi

for d in $IDE_DIR/*; do
  VERSION_DIR=${d}
  break
done

for f in ${SOURCE_DIR}/../build/libs/*.jar; do
  PLUGIN_JAR=${f}
  break
done

ADDITIONAL_FLAGS=
if [[ ! -z "$TEAMCITY_VERSION" ]]; then
  ADDITIONAL_FLAGS=-tc
fi

java -jar $VERIFIER_JAR -runtime-dir ${JAVA_HOME} check-plugin ${PLUGIN_JAR} ${VERSION_DIR} -ip ${SOURCE_DIR}/verifier-ignored-problems $ADDITIONAL_FLAGS -external-prefixes com.codota.sentry:com.codota.okhttp3:$ADDITIONAL_IGNORED_PACKAGES

cd -
