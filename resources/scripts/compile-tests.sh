#!/bin/bash

# Compile all test files in test-src to test-classes
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
echo $DIR
cd "$DIR/../java"
javac test-src/*.java -d test-classes
