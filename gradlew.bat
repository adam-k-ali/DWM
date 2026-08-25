@echo off
REM Repo-root Gradle shim — this monorepo has two sibling Gradle builds.
echo This repository is a monorepo with separate Gradle wrappers:
echo.
echo   dwm\gradlew ^<task^>          # The Doctor Who Mod (Fabric)
echo   screenplay\gradlew ^<task^>   # Screenplay harness (Fabric)
echo.
echo See README.md and AGENTS.md.
exit /b 1
