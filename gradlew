#!/usr/bin/env sh
set -eu
VERSION="9.5.0"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/open-archive-bootstrap"
HOME_DIR="$BASE/gradle-$VERSION"
ZIP="$BASE/gradle-$VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
if [ ! -x "$HOME_DIR/bin/gradle" ]; then
  mkdir -p "$BASE"
  if [ ! -f "$ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" "$URL"
    else echo "curl or wget is required to bootstrap Gradle" >&2; exit 1; fi
  fi
  rm -rf "$HOME_DIR"
  unzip -q "$ZIP" -d "$BASE"
fi
exec "$HOME_DIR/bin/gradle" "$@"
