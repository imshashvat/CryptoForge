@echo off
setlocal
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

set "IDEA_MVN=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
if exist "%IDEA_MVN%" (
    call "%IDEA_MVN%" %*
    exit /b %ERRORLEVEL%
)

for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
    if exist "%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
        call "%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

echo [ERROR] Maven not found. Please ensure Maven or IntelliJ IDEA is installed.
exit /b 1
