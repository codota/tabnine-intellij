#!/usr/bin/env bash

export IDE_TYPE=ideaIC

SOURCE_DIR=`dirname "$(cd "$(dirname "$0")"; pwd)/$(basename "$0")"`

LATEST=2020.2.2

echo "Verifying LATEST ----> $LATEST"
$SOURCE_DIR/verify-plugin.sh $LATEST


EARLIEST=2016.2.5
export NO_JDK=-no-jdk

echo "Verifying EARLIEST ----> $EARLIEST"
$SOURCE_DIR/verify-plugin.sh $EARLIEST
