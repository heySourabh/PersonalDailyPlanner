@echo off
set FX_PATH=path/to/fx/lib
set JAVA_PATH=path/to/bin/java
set JAR=PersonalDailyPlanner.jar
start /B %JAVA_PATH% --module-path %FX_PATH%  --add-modules javafx.controls,javafx.media,javafx.web,javafx.swing  --enable-native-access=javafx.graphics,javafx.media,javafx.web -jar %JAR%

