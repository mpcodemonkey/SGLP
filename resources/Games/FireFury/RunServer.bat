@ECHO OFF
:Input
SET /P serverPort=Input port number for the server: 
echo %serverPort%|findstr /xr "[1-9][0-9]*$" >nul && (
  GOTO End
) || (
  GOTO Error
)

:Error
ECHO You did not enter a valid port number!
GOTO Input

:End
java game/theGame/GameServer %serverPort%
pause
