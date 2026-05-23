@echo off
if not exist "bin" mkdir bin
dir /s /B src\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "ojdbc11-21.5.0.0.jar;libraries\*;lib\*" @sources.txt
echo Build finished with error level %errorlevel%
