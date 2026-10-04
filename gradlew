#!/bin/sh
# Minimal, checksum-pinned Gradle bootstrap launcher.
# For the standard Gradle wrapper, use gradle-wrapper.jar generated from this distribution.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.10
GRADLE_SHA256=5b9c5eb3f9fc2c94abaea57d90bd78747ca117ddbbf96c859d3741181a12bf2a
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/lia-bootstrap"
DIST_DIR="$CACHE_DIR/gradle-$GRADLE_VERSION"
ZIP="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  mkdir -p "$CACHE_DIR"
  if [ ! -f "$ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl --fail --location --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" --output "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    else
      echo "curl or wget is required to download Gradle $GRADLE_VERSION" >&2
      exit 1
    fi
  fi
  ACTUAL=$(sha256sum "$ZIP" | awk '{print $1}')
  if [ "$ACTUAL" != "$GRADLE_SHA256" ]; then
    rm -f "$ZIP"
    echo "Gradle archive checksum mismatch; refusing to execute it." >&2
    exit 1
  fi
  unzip -q "$ZIP" -d "$CACHE_DIR"
fi
exec "$DIST_DIR/bin/gradle" -p "$ROOT" "$@"
