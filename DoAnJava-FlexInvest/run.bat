@echo off
cd /d "%~dp0"

where ant >nul 2>&1
if %ERRORLEVEL% == 0 (
    ant run
    goto end
)

echo Ant not found, compiling manually...

if not exist build\classes mkdir build\classes

dir /s /b src\*.java > sources.txt

javac -encoding UTF-8 ^
  -cp "libraries\flatlaf-3.4.1.jar;ojdbc11-21.5.0.0.jar;libraries\itextpdf-5.5.13.3.jar" ^
  -d build\classes ^
  @sources.txt

del sources.txt

if %ERRORLEVEL% neq 0 (
    echo Compile failed!
    pause
    exit /b 1
)

java -cp "build\classes;libraries\flatlaf-3.4.1.jar;ojdbc11-21.5.0.0.jar;libraries\itextpdf-5.5.13.3.jar" ^
  doanjava.DoAnJava

:end
