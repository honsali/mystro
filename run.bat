@echo off
setlocal

cd /d "%~dp0"

set "MAVEN_OPTS=%MAVEN_OPTS% --enable-native-access=ALL-UNNAMED"

if "%~1"=="" (
    echo Usage: run.bat ^<native-list-alias^> [dd/MM/yyyy]
    exit /b 2
)
if not "%~3"=="" (
    echo Usage: run.bat ^<native-list-alias^> [dd/MM/yyyy]
    exit /b 2
)

if "%~2"=="" (
    mvn -q -Dtest=LocalReadingDumpRunner -Dlocal.reading=true -Dlocal.reading.alias="%~1" test
) else (
    mvn -q -Dtest=LocalZoomDumpRunner -Dlocal.zoom=true -Dlocal.reading.alias="%~1" -Dlocal.zoom.date="%~2" test
)

endlocal
