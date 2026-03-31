@echo off
git add .
git commit -m "active-dev update %DATE% %TIME%"
git push origin active-dev
echo Done.
pause