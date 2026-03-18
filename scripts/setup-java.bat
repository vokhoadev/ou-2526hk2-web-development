@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

echo ============================================================
echo   Auto-configure Java for Spring Boot Project
echo   Required: Java 23+
echo ============================================================
echo.

set "REQUIRED_MAJOR=23"
set "FOUND_JAVA="

REM --- 1. Check if current JAVA_HOME is already correct ---
if defined JAVA_HOME (
    for /f "tokens=*" %%v in ('"%JAVA_HOME%\bin\java" -version 2^>^&1') do (
        echo %%v | findstr /C:"%REQUIRED_MAJOR%" >nul && (
            set "FOUND_JAVA=%JAVA_HOME%"
        )
        goto :check_current_done
    )
)
:check_current_done
if defined FOUND_JAVA (
    echo [OK] JAVA_HOME already points to Java %REQUIRED_MAJOR%: %JAVA_HOME%
    goto :verify
)

echo [INFO] Searching for Java %REQUIRED_MAJOR% installations...
echo.

REM --- 2. Search common installation paths ---
set "SEARCH_DIRS="
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\Java""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\Eclipse Adoptium""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\Microsoft""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\Amazon Corretto""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\Zulu""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\BellSoft""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles%\SapMachine""
set "SEARCH_DIRS=%SEARCH_DIRS% "%USERPROFILE%\.jdks""
set "SEARCH_DIRS=%SEARCH_DIRS% "%ProgramFiles(x86)%\Java""
set "SEARCH_DIRS=%SEARCH_DIRS% "%LOCALAPPDATA%\Programs\Eclipse Adoptium""

set "CANDIDATE_COUNT=0"

for %%D in (%SEARCH_DIRS%) do (
    if exist %%D (
        for /d %%J in (%%~D\*%REQUIRED_MAJOR%*) do (
            if exist "%%J\bin\java.exe" (
                set /a CANDIDATE_COUNT+=1
                set "CANDIDATE_!CANDIDATE_COUNT!=%%J"
                echo   [!CANDIDATE_COUNT!] %%J
            )
        )
    )
)

REM --- 3. Also search via 'where java' ---
for /f "tokens=*" %%p in ('where java 2^>nul') do (
    set "JAVA_PATH=%%~dpp"
    set "JAVA_PATH=!JAVA_PATH:~0,-5!"
    if exist "!JAVA_PATH!\bin\java.exe" (
        "!JAVA_PATH!\bin\java" -version 2>&1 | findstr /C:"%REQUIRED_MAJOR%" >nul && (
            set /a CANDIDATE_COUNT+=1
            set "CANDIDATE_!CANDIDATE_COUNT!=!JAVA_PATH!"
            echo   [!CANDIDATE_COUNT!] !JAVA_PATH! (from PATH)
        )
    )
)

echo.

if %CANDIDATE_COUNT% EQU 0 (
    echo [ERROR] Java %REQUIRED_MAJOR% not found on this system!
    echo.
    echo Please install Java %REQUIRED_MAJOR% from one of:
    echo   - Eclipse Temurin: https://adoptium.net/
    echo   - Oracle JDK:      https://www.oracle.com/java/technologies/downloads/
    echo   - Amazon Corretto: https://aws.amazon.com/corretto/
    echo   - Microsoft JDK:   https://learn.microsoft.com/en-us/java/openjdk/download
    echo.
    pause
    exit /b 1
)

if %CANDIDATE_COUNT% EQU 1 (
    set "FOUND_JAVA=!CANDIDATE_1!"
    echo [INFO] Found 1 installation, selecting automatically.
) else (
    echo Found %CANDIDATE_COUNT% Java installations.
    set /p "CHOICE=Select [1-%CANDIDATE_COUNT%]: "
    set "FOUND_JAVA=!CANDIDATE_%CHOICE%!"
)

if not defined FOUND_JAVA (
    echo [ERROR] Invalid selection.
    pause
    exit /b 1
)

REM --- 4. Set JAVA_HOME for current session ---
set "JAVA_HOME=%FOUND_JAVA%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo.
echo [OK] JAVA_HOME set to: %JAVA_HOME%

REM --- 5. Ask to set permanently ---
echo.
set /p "PERMANENT=Set JAVA_HOME permanently for this user? (y/n): "
if /i "%PERMANENT%"=="y" (
    setx JAVA_HOME "%FOUND_JAVA%"
    echo [OK] JAVA_HOME set permanently. Restart terminal to take effect.
)

:verify
echo.
echo ============================================================
echo   Verification
echo ============================================================
echo.
echo --- java -version ---
"%JAVA_HOME%\bin\java" -version 2>&1
echo.

where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo --- mvn -version ---
    call mvn -version
) else (
    echo [WARN] Maven (mvn) not found in PATH.
    echo   If using Maven Wrapper, run: .\mvnw spring-boot:run
)

echo.
echo ============================================================
echo   Done! You can now run:
echo     mvn spring-boot:run
echo   or:
echo     .\mvnw spring-boot:run
echo ============================================================
echo.
pause