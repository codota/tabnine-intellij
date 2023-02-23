#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "script dir is $SCRIPT_DIR"
# THIS SCRIPT WILL DOWNLOAD THE BINARIES FROM gs://latest-onprem-binaries
# export the version that you want to download
#VERSION=4.4.242

echo "Downloading version $VERSION"


#for target in x86_64-pc-windows-gnu x86_64-apple-darwin i686-pc-windows-gnu aarch64-apple-darwin x86_64-unknown-linux-musl
for target in x86_64-pc-windows-gnu x86_64-apple-darwin i686-pc-windows-gnu aarch64-apple-darwin x86_64-unknown-linux-musl
do
  # yoni - you can remove the $VERSION from the mkdir path and then the convention in the java/kotlin code won't need a version path when constructing the jar://file///path/to/tabnine
    mkdir -p $SCRIPT_DIR/${target} \
    && cd $SCRIPT_DIR/${target} \
    && gsutil -m cp -r "gs://latest-onprem-binaries/$VERSION/latest/${target}/TabNine.zip" . \
    && unzip TabNine.zip \
    && chmod +x * \
    && rm TabNine.zip \
    && rm *local* \
    && cd $SCRIPT_DIR
done
