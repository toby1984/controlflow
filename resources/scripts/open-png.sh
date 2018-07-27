#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$DIR/../dot"
for pngfile in `ls png`; do
  filename=$(basename "$pngfile")
  extension="${filename##*.}"
  filename="${filename%.*}"

  if [ "$extension" = "png" ]; then
    echo "Opening 'png/$filename.png'"
    gnome-open "png/$pngfile"
  fi
done
