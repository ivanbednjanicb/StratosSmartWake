@echo off
if "%~1"=="" (
  echo Usage: install-debug.bat path-to-app-debug.apk
  pause
  exit /b 1
)
adb install -r "%~1"
pause
