@echo off
mkdir stratos-smartwake-logs 2>nul
adb pull /sdcard/StratosSmartWake/logs stratos-smartwake-logs
pause
