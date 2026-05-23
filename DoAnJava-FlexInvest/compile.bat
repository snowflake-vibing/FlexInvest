@echo off
mkdir bin 2>nul
dir /s /b src\*.java > sources_local.txt
javac -encoding UTF-8 -d bin -cp "ojdbc11-21.5.0.0.jar;libraries\flatlaf-3.4.1.jar" @sources_local.txt
xcopy /s /e /y src\Resources bin\Resources\
