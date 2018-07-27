#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$DIR/../dot"
for dotfile in `ls`; do
  filename=$(basename "$dotfile")
  extension="${filename##*.}"
  filename="${filename%.*}"

  if [ "$extension" = "dot" ]; then
    echo "Writing to 'png/$filename.png'"
    dot $dotfile -Tpng > "png/$filename.png"
  fi
done
