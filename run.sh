#!/usr/bin/sh
FX_PATH="/path/to/javafx-sdk/lib"
JAVA_PATH="/path/to/jdk/bin/java"
JAR=/path/to/PersonalDailyPlanner.jar
cd "$(dirname "$0")" || exit
$JAVA_PATH --module-path $FX_PATH --add-modules javafx.controls,javafx.graphics,javafx.swing,javafx.media -jar $JAR

