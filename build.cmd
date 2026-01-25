@echo off
REM Set JAVA_HOME to Android Studio JBR (Java 21)
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"

REM Run gradle build
call gradlew.bat :app:assembleDebug --no-daemon --stacktrace

pause
