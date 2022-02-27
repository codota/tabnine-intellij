#! /bin/bash

set -e

xml_file_name=updatePlugins.xml
build_plugin=false

while getopts :u:b flag; do
  case "${flag}" in
  u) url=${OPTARG} ;;
  b) build_plugin=true ;;
  *) ;;
  esac
done

if [ "$build_plugin" == "true" ]; then
echo ">>> Building the plugin..."
  cd ..
  ./gradlew buildPlugin
  cd -
fi
if [ -z "$plugin_id" ]; then
  plugin_id="com.tabnine.TabNine"
fi
if [ -z "$url" ]; then
  echo "'-u' is required"
  exit
fi

cp ../build/distributions/*TabNine*.zip .
plugin_file="$(basename -- "$(ls -al ./*TabNine*.zip)")"
plugin_version="$(echo "$plugin_file" | grep -o -P '(?<=-).*(?=.zip)')"

xml_data="<plugins><plugin id=\"$plugin_id\" url=\"$url/$plugin_file\" version=\"$plugin_version\"/></plugins>"

printf ">>> generating %s with the following data: \"%s\"\n" "$xml_file_name" "$xml_data"
printf "\n$(tput setaf 2)>>> This is the url you need to add as a repository: %s$(tput sgr0)\n\n" "$url/$xml_file_name"

echo "$xml_data" >$xml_file_name

echo ">>> Running http-server..."
http-server
