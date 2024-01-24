@echo off
title L2J4Team GameServer Console
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -Dfile.encoding=UTF8 -server -XX:+UseCodeCacheFlushing -XX:+OptimizeStringConcat -XX:+UseG1GC -XX:+TieredCompilation -XX:+UseCompressedOops -XX:SurvivorRatio=8 -XX:NewRatio=4 -Xmx2G -cp ./libs/*; com.l2j4team.gameserver.GameServer
REM -------------------------------------
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin have restarted, please wait.
echo.
goto start
:error
echo.
echo Server have terminated abnormaly.
echo.
:end
echo.
echo Server terminated.
echo.
pause
