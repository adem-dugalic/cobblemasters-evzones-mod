@echo off
cd /d "%~dp0"
call gradlew.bat build > build_out.txt 2>&1
