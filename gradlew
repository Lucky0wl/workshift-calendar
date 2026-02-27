#!/bin/sh
#
# Gradle start up script for POSIX (minimal for CI)
#
APP_HOME=$( cd "${0%/*}" > /dev/null && pwd ) || exit 1
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java -Dfile.encoding=UTF-8 -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
