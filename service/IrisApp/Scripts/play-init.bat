if exist ..\RUNNING_PID (
  set /p THEPID=<..\RUNNING_PID
  taskkill /pid %THEPID%
  if exist ..\RUNNING_PID (
    del ..\RUNNING_PID
  )
)

