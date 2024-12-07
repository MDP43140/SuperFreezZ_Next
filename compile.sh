#!/usr/bin/env sh
clear
ops="$1"
[ "$ops" ] || ops="assembleRelease"
exec sh gradlew $ops