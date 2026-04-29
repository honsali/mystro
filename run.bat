@echo off
setlocal

cd /d "%~dp0"
  mvn exec:java -Dexec.args="--subjects ilia --doctrines valens dorotheus ptolemy" %*

endlocal
