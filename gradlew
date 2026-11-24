#!/bin/sh
# Gradle wrapper script
set -e

APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Use the app UID's java
JAVA_CMD="java"

exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.BootstrapMainStarter "$@"
