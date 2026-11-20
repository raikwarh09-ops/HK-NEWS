@echo off
title HK NEWS PC Studio Pro Launcher
echo Launching HK NEWS PC Studio Pro...
cd /d "%~dp0"

where msedge >nul 2>nul
if %errorlevel%==0 (
  start "" msedge --app="%~dp0HK_NEWS_PC_Studio.html" --window-size=1366,820
  exit /b 0
)

where chrome >nul 2>nul
if %errorlevel%==0 (
  start "" chrome --app="%~dp0HK_NEWS_PC_Studio.html" --window-size=1366,820
  exit /b 0
)

start "" "%~dp0HK_NEWS_PC_Studio.html"
