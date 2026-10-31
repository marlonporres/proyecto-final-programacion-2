@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar-demostracion.ps1" %*
exit /b %ERRORLEVEL%
