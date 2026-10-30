@echo off
rem La politica se aplica solo a este proceso; no modifica la configuracion de Windows.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0demostracion.ps1" %*
exit /b %ERRORLEVEL%
