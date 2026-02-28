@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  echo [ERROR] Missing Maven Wrapper JAR: %WRAPPER_JAR%
  exit /b 1
)

if not exist "%WRAPPER_PROPERTIES%" (
  echo [ERROR] Missing Maven Wrapper properties: %WRAPPER_PROPERTIES%
  exit /b 1
)

set JAVA_EXE=java
for /f "delims=" %%i in ('where java 2^>NUL') do (
  set JAVA_EXE=%%i
  goto run
)

:run
"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
