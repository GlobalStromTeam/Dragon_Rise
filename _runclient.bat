@echo off
setlocal
cd /d "%~dp0"

rem Forge 1.20.1 needs JDK 17; prefer the local ms-17 install
if exist "%USERPROFILE%\.jdks\ms-17.0.17\bin\java.exe" set "JAVA_HOME=%USERPROFILE%\.jdks\ms-17.0.17"
if not exist "%JAVA_HOME%\bin\java.exe" if exist "%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2\bin\java.exe" set "JAVA_HOME=%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [DR] JAVA_HOME=%JAVA_HOME%

echo [DR] gradlew.bat runClient --offline
call gradlew.bat runClient --offline --console=plain
set RC=%ERRORLEVEL%

if not "%RC%"=="0" (
    echo [DR] runClient FAILED, exit=%RC%
    echo [DR] check run\logs\latest.log and run\crash-reports\
) else (
    echo [DR] runClient exited normally
)
pause
