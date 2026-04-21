@echo off
setlocal

cd /d "%~dp0"
  mvn exec:java -Dexec.args="--names ilia reda marwa" %*

endlocal
