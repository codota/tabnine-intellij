#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "script dir is $SCRIPT_DIR"
# THIS SCRIPT WILL DOWNLOAD THE BINARIES FROM https://update.tabnine.com/bundles/
# set the version that you want to download
VERSION=4.4.242

echo "Downloading version $VERSION"
tmp_dir=$(mktemp -d -t ci-XXXXXXXXXX)

echo $tmp_dir
cd $tmp_dir


for target in x86_64-pc-windows-gnu x86_64-apple-darwin i686-pc-windows-gnu aarch64-apple-darwin x86_64-unknown-linux-musl
do
    mkdir $SCRIPT_DIR/${target}
    mkdir $target && cd $target && curl -s -o tabnine.zip \
        https://update.tabnine.com/bundles/${VERSION}/${target}/TabNine.zip \
            && unzip tabnine.zip \
            && rm tabnine.zip \
            && rm *local* \
            && cp * $SCRIPT_DIR/${target} \
            && cd -
done
