@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

set "BASE_URL=http://localhost:8090"
set "JSON_FILE=%~dp0test-api.json"
set PASS=0
set FAIL=0
set TOTAL=0

echo ============================================================
echo   Running API Tests from: %JSON_FILE%
echo ============================================================
echo.

REM --- Check dependencies: curl and jq ---
where curl >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] curl khong tim thay!
    echo   curl thuong co san tren Windows 10+.
    echo   Neu chua co, tai tai: https://curl.se/windows/
    echo.
    pause
    exit /b 1
)

where jq >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] jq khong tim thay! jq can thiet de doc file JSON.
    echo.
    echo   Cai dat jq bang MOT trong cac cach sau:
    echo.
    echo   Cach 1 - winget ^(Windows 10+^):
    echo     winget install jqlang.jq
    echo.
    echo   Cach 2 - Chocolatey:
    echo     choco install jq
    echo.
    echo   Cach 3 - Tai thu cong:
    echo     https://github.com/jqlang/jq/releases
    echo     Tai file jq-windows-amd64.exe, doi ten thanh jq.exe
    echo     Bo vao thu muc co trong PATH ^(vd: C:\Windows^)
    echo.
    echo   Sau khi cai xong, mo lai terminal va chay lai script nay.
    echo.
    pause
    exit /b 1
)

if not exist "%JSON_FILE%" (
    echo [ERROR] Khong tim thay file: %JSON_FILE%
    echo   Dam bao file test-api.json nam cung thu muc voi script nay.
    echo.
    pause
    exit /b 1
)

echo [OK] curl: found
echo [OK] jq:   found
echo [OK] JSON: %JSON_FILE%
echo.

for /f %%n in ('jq length "%JSON_FILE%"') do set "COUNT=%%n"

set /a LAST=%COUNT%-1

for /L %%i in (0,1,%LAST%) do (
    for /f "delims=" %%a in ('jq -r ".[%%i].name" "%JSON_FILE%"') do set "NAME=%%a"
    for /f "delims=" %%a in ('jq -r ".[%%i].method" "%JSON_FILE%"') do set "METHOD=%%a"
    for /f "delims=" %%a in ('jq -r ".[%%i].path" "%JSON_FILE%"') do set "API_PATH=%%a"
    for /f "delims=" %%a in ('jq -c ".[%%i].body" "%JSON_FILE%"') do set "BODY=%%a"
    for /f "delims=" %%a in ('jq -r ".[%%i].expect" "%JSON_FILE%"') do set "EXPECT=%%a"
    set /a TOTAL+=1

    if "!BODY!"=="null" (
        for /f %%s in ('curl -s -o NUL -w "%%{http_code}" -X !METHOD! "!BASE_URL!!API_PATH!"') do set "HTTP_CODE=%%s"
    ) else (
        for /f %%s in ('curl -s -o NUL -w "%%{http_code}" -X !METHOD! "!BASE_URL!!API_PATH!" -H "Content-Type: application/json" -d "!BODY!"') do set "HTTP_CODE=%%s"
    )

    if "!HTTP_CODE!"=="!EXPECT!" (
        echo [PASS] #!TOTAL! !METHOD! !API_PATH! - !NAME! ^(HTTP !HTTP_CODE!^)
        set /a PASS+=1
    ) else (
        echo [FAIL] #!TOTAL! !METHOD! !API_PATH! - !NAME! ^(HTTP !HTTP_CODE!, expected !EXPECT!^)
        set /a FAIL+=1
    )
)

echo.
echo ============================================================
echo   Results: %PASS% passed, %FAIL% failed, %TOTAL% total
echo ============================================================
pause