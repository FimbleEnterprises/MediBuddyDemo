@ECHO OFF

REM This file must be present.  It contains the adb shell commands to 
REM pull the database files into this directory.
adb\adb shell < copydbcommands.txt

REM No execute the pulls to the current working directory
adb\adb pull /mnt/sdcard/mileagetracking.db mileagetracking.db
echo copied 1st
adb\adb pull /mnt/sdcard/mileagetracking.db-shm mileagetracking.db-shm
echo copied 2nd
adb\adb pull /mnt/sdcard/mileagetracking.db-wal mileagetracking.db-wal
echo copied 3rd

REM Open the working directory that should now have the db files.
%SystemRoot%\explorer.exe %CD%

pause

