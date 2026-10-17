#!/bin/sh
#
# GAIA Desktop launcher — macOS and Linux.
#
# Builds the class path itself: gaia-desktop.jar first, then every .jar in lib/ beside it, and
# launches the app with -cp. That is why the main class is named here — -cp does not read the
# manifest's Main-Class. It does still follow its Class-Path, so the jars the manifest names load
# either way; this script lists lib/ itself and does not rely on that, so a jar whose manifest is
# stale, edited or missing entries still starts. "java -jar gaia-desktop.jar" by hand works
# through the manifest alone.
#
# It also means the same script runs either packaging: the single-file build (mvn -Pfat) has no
# lib/ to add, and everything it needs is already inside the jar on the class path.
#
# Arguments pass straight through, so the app's own options work:
#
#     ./run.sh
#     ./run.sh --language=FR
#
# Extra JVM options go in GAIA_DESKTOP_OPTS:
#
#     GAIA_DESKTOP_OPTS=-Xmx1g ./run.sh
#
# The console stays attached on purpose. At startup the app prints one line carrying its own
# version, its build date and the engine version, and that is the line a bug report needs.

set -eu

MAIN_CLASS=tools.pantheum.gaia.desktop.Launcher

# The directory holding this script, resolved so the launcher works from any working
# directory and survives spaces in the path.
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

# Beside this script, except in the macOS distributions: there gaia-desktop.jar and lib/ live
# inside GAIA Desktop.app (Contents/Java), so the app stands alone, and this script reaches in.
JAR_DIR="$SCRIPT_DIR"
APP_JAVA_DIR="$SCRIPT_DIR/GAIA Desktop.app/Contents/Java"
if [ ! -f "$JAR_DIR/gaia-desktop.jar" ] && [ -f "$APP_JAVA_DIR/gaia-desktop.jar" ]; then
    JAR_DIR="$APP_JAVA_DIR"
fi
JAR="$JAR_DIR/gaia-desktop.jar"

if [ ! -f "$JAR" ]; then
    echo "run.sh: cannot find $JAR" >&2
    echo "        Build it first:  mvn package" >&2
    exit 1
fi

# Every jar in lib/, named one by one rather than left to a "lib/*" wildcard, so that the
# count below is real and a path containing spaces stays one entry.
CLASSPATH="$JAR"
LIB_COUNT=0
for JARFILE in "$JAR_DIR"/lib/*.jar; do
    # No match leaves the pattern itself in $JARFILE, which -e then rejects.
    [ -e "$JARFILE" ] || break
    CLASSPATH="$CLASSPATH:$JARFILE"
    LIB_COUNT=$((LIB_COUNT + 1))
done

# Nothing in lib/ is correct for the single-file build and fatal for the default one, and this
# script cannot tell which jar it was handed — so say so and carry on rather than guess.
if [ "$LIB_COUNT" -eq 0 ]; then
    echo "run.sh: note - no jars in lib/ next to gaia-desktop.jar." >&2
    echo "        Expected only for the single-file build (mvn -Pfat package). The default" >&2
    echo "        build needs them; if startup fails, rebuild with 'mvn package'." >&2
fi

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA=java
else
    echo "run.sh: no Java found." >&2
    echo "        Install Java 21 or newer, or point JAVA_HOME at it." >&2
    exit 1
fi

# Refuse an old JVM here rather than let it fail with UnsupportedClassVersionError, which
# says nothing about what to install. Both shapes appear in the wild: "1.8.0_392" and "17.0.9".
VERSION=$("$JAVA" -version 2>&1 | sed -n 's/.*version "\([^"]*\)".*/\1/p' | head -n 1)
case "$VERSION" in
    1.*) MAJOR=$(echo "$VERSION" | cut -d. -f2) ;;
    *)   MAJOR=$(echo "$VERSION" | cut -d. -f1) ;;
esac
# A version string this did not understand is not worth failing over — skip the check instead.
if [ -n "$VERSION" ] && echo "$MAJOR" | grep -q '^[0-9][0-9]*$' && [ "$MAJOR" -lt 21 ]; then
    echo "run.sh: found Java $VERSION, but GAIA Desktop needs Java 21 or newer." >&2
    echo "        Install a newer JVM, or point JAVA_HOME at one you already have." >&2
    exit 1
fi

# Newer JDKs warn — and later releases will refuse — when code loads native libraries without
# permission, and JavaFX loads its own (glass, prism, webkit) from the jars in lib/. JavaFX is on
# the class path here, so it is part of the unnamed module and ALL-UNNAMED covers it. Like
# run.bat, skipped only when the version above could not be read.
NATIVE_OPTS=
if echo "$MAJOR" | grep -q '^[0-9][0-9]*$'; then
    NATIVE_OPTS=--enable-native-access=ALL-UNNAMED
fi

# No java.library.path is set here, unlike run.bat. The only native libraries the app needs
# are Zebra's scanner DLLs beside this script, and both the drivers and the scanner controls
# themselves are Windows-only — ScannerService.isSupportedPlatform() builds none of them on
# macOS or Linux, so there is nothing on this platform that would look for them.
#
# GAIA_DESKTOP_OPTS is deliberately unquoted so that several options in it split into
# several arguments.
# shellcheck disable=SC2086
exec "$JAVA" ${GAIA_DESKTOP_OPTS:-} $NATIVE_OPTS -cp "$CLASSPATH" "$MAIN_CLASS" "$@"
