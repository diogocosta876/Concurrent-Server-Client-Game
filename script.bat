@echo off
REM Starting Server
REM start cmd /k gradlew runServer

REM Asking How many Users to start
set /p n=Enter the number of times to run gradlew runUser:
echo Running gradlew %n% times...

REM Starting each User
for /l %%i in (1,1,%n%) do (
  echo Running gradlew for the %%i time...
  start cmd /k gradlew runUser
)
echo Done.