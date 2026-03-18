@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

echo ============================================================
echo   Auto-configure JAVA_HOME for Spring Boot Project
echo   Required: Java 23+
echo ============================================================
echo.

set "REQUIRED_MAJOR=23"
set "FOUND_JAVA="

REM --- 1. Check if current JAVA_HOME is already correct ---
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        for /f "tokens=3" %%v in ('"%JAVA_HOME%\bin\java" -version 2^>^&1 ^| findstr /i "version"') do (
            set "RAW_VER=%%~v"
        )
        for /f "tokens=1 delims=." %%m in ("!RAW_VER!") do set "CUR_MAJOR=%%m"
        if !CUR_MAJOR! GEQ %REQUIRED_MAJOR% (
            set "FOUND_JAVA=%JAVA_HOME%"
        )
    )
)
if defined FOUND_JAVA (
    echo [OK] JAVA_HOME da dung Java !CUR_MAJOR!: %JAVA_HOME%
    goto :verify
)

echo [INFO] JAVA_HOME hien tai khong dung hoac chua set. Dang tim Java %REQUIRED_MAJOR%+...
echo.

REM --- 2. Search common JDK installation paths ---
set "BEST_JAVA="
set "BEST_MAJOR=0"

set "SEARCH_DIRS="
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Java""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Eclipse Adoptium""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Eclipse Foundation""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Microsoft""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Amazon Corretto""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\Zulu""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\BellSoft""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles%\SapMachine""
set "SEARCH_DIRS=!SEARCH_DIRS! "%USERPROFILE%\.jdks""
set "SEARCH_DIRS=!SEARCH_DIRS! "%ProgramFiles(x86)%\Java""
set "SEARCH_DIRS=!SEARCH_DIRS! "%LOCALAPPDATA%\Programs\Eclipse Adoptium""

for %%D in (!SEARCH_DIRS!) do (
    if exist %%D (
        for /d %%J in (%%~D\*) do (
            if exist "%%J\bin\java.exe" (
                set "THIS_VER="
                for /f "tokens=3" %%v in ('"%%J\bin\java" -version 2^>^&1 ^| findstr /i "version"') do (
                    set "THIS_VER=%%~v"
                )
                if defined THIS_VER (
                    for /f "tokens=1 delims=." %%m in ("!THIS_VER!") do set "THIS_MAJOR=%%m"
                    if !THIS_MAJOR! GEQ %REQUIRED_MAJOR% (
                        echo   [FOUND] Java !THIS_MAJOR! at %%J
                        if !THIS_MAJOR! GTR !BEST_MAJOR! (
                            set "BEST_MAJOR=!THIS_MAJOR!"
                            set "BEST_JAVA=%%J"
                        )
                    )
                )
            )
        )
    )
)

REM --- 3. Also check java in PATH ---
for /f "tokens=*" %%p in ('where java 2^>nul') do (
    set "JAVA_BIN_DIR=%%~dpp"
    set "JAVA_BIN_DIR=!JAVA_BIN_DIR:~0,-1!"
    for %%X in ("!JAVA_BIN_DIR!") do set "PATH_JDK=%%~dpX"
    set "PATH_JDK=!PATH_JDK:~0,-1!"
    if exist "!PATH_JDK!\bin\java.exe" (
        set "THIS_VER="
        for /f "tokens=3" %%v in ('"!PATH_JDK!\bin\java" -version 2^>^&1 ^| findstr /i "version"') do (
            set "THIS_VER=%%~v"
        )
        if defined THIS_VER (
            for /f "tokens=1 delims=." %%m in ("!THIS_VER!") do set "THIS_MAJOR=%%m"
            if !THIS_MAJOR! GEQ %REQUIRED_MAJOR% (
                echo   [FOUND] Java !THIS_MAJOR! at !PATH_JDK! (from PATH)
                if !THIS_MAJOR! GTR !BEST_MAJOR! (
                    set "BEST_MAJOR=!THIS_MAJOR!"
                    set "BEST_JAVA=!PATH_JDK!"
                )
            )
        )
    )
)

echo.

if not defined BEST_JAVA (
    echo [ERROR] Khong tim thay Java %REQUIRED_MAJOR%+ tren may!
    echo.
    echo Hay cai dat Java %REQUIRED_MAJOR% tu mot trong cac link sau:
    echo   - Oracle JDK:      https://www.oracle.com/java/technologies/downloads/
    echo   - Eclipse Temurin:  https://adoptium.net/
    echo   - Amazon Corretto:  https://aws.amazon.com/corretto/
    echo   - Microsoft JDK:    https://learn.microsoft.com/en-us/java/openjdk/download
    echo.
    pause
    exit /b 1
)

REM --- 4. Auto-set JAVA_HOME permanently ---
set "FOUND_JAVA=!BEST_JAVA!"
set "JAVA_HOME=!FOUND_JAVA!"
set "PATH=!JAVA_HOME!\bin;!PATH!"

setx JAVA_HOME "!FOUND_JAVA!" >nul 2>&1
echo [OK] JAVA_HOME = !FOUND_JAVA!
echo [OK] Da luu JAVA_HOME vinh vien. Mo lai terminal de ap dung.

:verify
echo.
echo ============================================================
echo   Kiem tra
echo ============================================================
echo.
echo --- java -version ---
"!JAVA_HOME!\bin\java" -version 2>&1
echo.

REM Check Maven Wrapper in project (go up from scripts/ to project root)
set "PROJECT_DIR=%~dp0.."
if exist "!PROJECT_DIR!\mvnw.cmd" (
    echo --- mvnw -version ---
    pushd "!PROJECT_DIR!"
    call mvnw.cmd --version 2>&1
    popd
) else (
    where mvn >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        echo --- mvn -version ---
        call mvn -version
    ) else (
        echo [WARN] Maven khong tim thay trong PATH.
    )
)

echo.
echo ============================================================
echo   Hoan tat! Chay lenh sau de khoi dong project:
echo     cd ..
echo     .\mvnw.cmd spring-boot:run
echo ============================================================
echo.
pause
