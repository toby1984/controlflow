#!/bin/sh

for dotfile in `ls`; do
  filename=$(basename "$dotfile")
  extension="${filename##*.}"
  filename="${filename%.*}"

  if [ "$extension" = "dot" ]; then
    echo "Writing to 'png/$filename.png'"
    dot $dotfile -Tpng > "png/$filename.png"
  fi
done
