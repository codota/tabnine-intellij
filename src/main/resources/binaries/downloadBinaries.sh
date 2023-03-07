#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "script dir is $SCRIPT_DIR"
echo "Downloading version $VERSION"


for target in x86_64-pc-windows-gnu x86_64-apple-darwin i686-pc-windows-gnu aarch64-apple-darwin x86_64-unknown-linux-musl
do
    mkdir -p $SCRIPT_DIR/${target} \
    && cd $SCRIPT_DIR/${target} \
    && gsutil -m cp -r "gs://latest-onprem-binaries/$VERSION/artifacts/${target}/TabNine.zip" . \
    && unzip TabNine.zip \
    && chmod +x * \
    && rm TabNine.zip \
    && rm *local* \
    && rm -f eval \
    && rm -f lsp-wrapper \
    && cd $SCRIPT_DIR
done

echo $VERSION > $SCRIPT_DIR/version
