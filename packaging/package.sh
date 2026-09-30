#!/usr/bin/env bash
# Builds the self-contained application packages (bundled Java runtime, no Java installation needed).
#
#   packaging/package.sh [version]
#
# Linux:   dist/ImageOrganizr-<version>-linux-x64.tar.gz and dist/imageorganizr_<version>_amd64.deb
# Windows: dist/ImageOrganizr-<version>-windows-x64.zip and dist/ImageOrganizr-<version>-windows-x64.msi (needs WiX 3)
#
# Runs in Git Bash on Windows as well, which is what the GitHub workflow uses.
set -euo pipefail

cd "$(dirname "$0")/.."

VERSION="${1:-$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)}"
VERSION="${VERSION%-SNAPSHOT}"   # jpackage accepts plain numeric versions only

NAME=ImageOrganizr
MODULE=eu.spex/eu.spex.iorg.ImageOrganizr
DESCRIPTION="Organize your photos by comparing, rating and categorizing them"
WORK=target/jpackage
DIST=dist

mvn -B -q -DskipTests clean package javafx:jlink

rm -rf "$WORK" "$DIST"
mkdir -p "$WORK" "$DIST"

COMMON=(
  --name "$NAME"
  --app-version "$VERSION"
  --vendor "Bert Speckels"
  --description "$DESCRIPTION"
  --copyright "Copyright (c) 2023-2026 Bert Speckels, MIT License"
  --runtime-image target/iorg-image
  --module "$MODULE"
)

case "$(uname -s)" in
  Linux*)
    jpackage "${COMMON[@]}" --type app-image --icon packaging/iorg.png --dest "$WORK"
    tar -C "$WORK" -czf "$DIST/$NAME-$VERSION-linux-x64.tar.gz" "$NAME"

    jpackage "${COMMON[@]}" --type deb --icon packaging/iorg.png --dest "$DIST" \
      --linux-package-name imageorganizr \
      --linux-app-category graphics \
      --linux-menu-group "Graphics;Photography" \
      --linux-shortcut \
      --license-file LICENSE
    ;;
  MINGW*|MSYS*|CYGWIN*)
    jpackage "${COMMON[@]}" --type app-image --icon packaging/iorg.ico --dest "$WORK"
    (cd "$WORK" && 7z a -tzip -bso0 "../../$DIST/$NAME-$VERSION-windows-x64.zip" "$NAME")

    jpackage "${COMMON[@]}" --type msi --icon packaging/iorg.ico --dest "$WORK" \
      --win-menu --win-menu-group "$NAME" \
      --win-shortcut --win-shortcut-prompt \
      --win-dir-chooser \
      --win-per-user-install \
      --win-upgrade-uuid 7c1f0f5e-5b8e-4a55-9a53-3f0c2a0d1e42 \
      --license-file LICENSE
    mv "$WORK"/*.msi "$DIST/$NAME-$VERSION-windows-x64.msi"
    ;;
  *)
    echo "Unsupported platform: $(uname -s)" >&2
    exit 1
    ;;
esac

ls -l "$DIST"
