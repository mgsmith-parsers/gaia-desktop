@echo off
rem
rem GAIA Desktop launcher - Windows.
rem
rem Builds the class path itself and launches the app with -cp. That is why the main class is
rem named here - -cp does not read the manifest's Main-Class. It does still follow its
rem Class-Path: a jar on -cp brings the jars its manifest names along with it, searched straight
rem after it, so lib\'s jars load whether or not they are listed below. The script lists them
rem anyway and does not rely on the manifest, so a jar whose manifest is stale, edited or missing
rem entries still starts; "java -jar gaia-desktop.jar" by hand works through the manifest alone.
rem
rem It adapts to what it finds, in this order of preference:
rem
rem   Java     jdk*\ beside this script (a bundled runtime, e.g. jdk-27\), else JAVA_HOME,
rem            else java on the PATH.
rem   JavaFX   javafx-sdk*\lib\ beside this script goes on the module path, and lib\'s own
rem            javafx-*.jar are left off the class path so the toolkit is not loaded twice;
rem            without an SDK, the javafx-*.jar in lib\ are used from the class path.
rem   JavaPOS  The API and its configuration loader always come from lib\'s javapos-*.jar.
rem            Zebra's Scanner SDK, if installed (ZEBRA_HOME, below), adds only its services:
rem            the Jpos*.jar service jars and bin\ go on the class path, and bin\ goes on the
rem            PATH for the CSJPOS*.dll natives. The SDK's own JavaPOS jar,
rem            External\JavaPOS-1.14.0-Dist\javaPOS114.jar, is deliberately left out: its
rem            jpos/res/jpos.properties makes the app leave the configuration alone, and its
rem            jcl.xsd accepts JavaPOS versions only up to 1.10, so every entry in the bundled
rem            jpos.xml (1.12-1.14) is rejected and every open fails with "Service does not
rem            exist in loaded JCL registry". Being on the class path at all is enough for that,
rem            even behind lib\'s jars.
rem
rem The single-file build (mvn -Pfat) has no lib\ to add; everything it needs is already inside
rem the jar on the class path, so the same script runs it.
rem
rem Arguments pass straight through, so the app's own options work:
rem
rem     run.bat
rem     run.bat --language=FR
rem
rem Extra JVM options go in GAIA_DESKTOP_OPTS:
rem
rem     set GAIA_DESKTOP_OPTS=-Xmx1g
rem     run.bat
rem
rem This uses java.exe rather than javaw.exe, so the console window stays open. That is
rem deliberate on Windows in particular: the scanner runs here and nowhere else, and its
rem status, the JavaPOS errors and the startup version line all go to the console. Switch the
rem one word to javaw.exe if a console-free launch matters more than those messages.

setlocal

set "MAIN_CLASS=tools.pantheum.gaia.desktop.Launcher"
set "SCRIPT_DIR=%~dp0"
set "JAR=%SCRIPT_DIR%gaia-desktop.jar"

if not exist "%JAR%" (
    echo run.bat: cannot find "%JAR%" 1>&2
    echo          Build it first:  mvn package 1>&2
    exit /b 1
)

rem ---------------------------------------------------------------- Zebra Scanner SDK

rem Where Zebra's installer puts the JavaPOS part of the Scanner SDK. Set ZEBRA_HOME before
rem running this script to point somewhere else rather than editing the line.
if not defined ZEBRA_HOME set "ZEBRA_HOME=%ProgramFiles%\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\"
if not "%ZEBRA_HOME:~-1%"=="\" set "ZEBRA_HOME=%ZEBRA_HOME%\"
set "ZEBRA="
if exist "%ZEBRA_HOME%" set "ZEBRA=1"

rem ---------------------------------------------------------------- Java and JavaFX

set "JAVA="
set "JAVA_FROM="
for /d %%D in ("%SCRIPT_DIR%jdk*") do if exist "%%~fD\bin\java.exe" (
    set "JAVA=%%~fD\bin\java.exe"
    set "JAVA_FROM=%%~nxD"
)
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
    set "JAVA_FROM=JAVA_HOME"
)
if not defined JAVA (
    set "JAVA=java"
    set "JAVA_FROM=PATH"
)

rem The first line of "java -version" reads   openjdk version "17.0.12" ...   (or "1.8.0_402"
rem on Java 8). "call" in front keeps cmd from stripping the quotes around a path with spaces.
set "JAVA_VERSION="
for /f "tokens=3" %%V in ('call "%JAVA%" -version 2^>^&1 ^| findstr /i /c:" version "') do (
    if not defined JAVA_VERSION set "JAVA_VERSION=%%~V"
)
if not defined JAVA_VERSION (
    echo run.bat: no Java found. 1>&2
    echo          Install Java 21 or newer, or point JAVA_HOME at it. 1>&2
    exit /b 1
)
set "JAVA_MAJOR="
for /f "tokens=1,2 delims=.-_+" %%A in ("%JAVA_VERSION%") do (
    if "%%A"=="1" (set "JAVA_MAJOR=%%B") else (set "JAVA_MAJOR=%%A")
)
rem Always a number from here on, 0 when the version string was not understood: cmd expands
rem %JAVA_MAJOR% before it evaluates any "if", so an empty one would be a syntax error even
rem behind "if defined". A version this did not understand is not worth failing over - 0 skips
rem the check, and the Java 17 options below.
set /a JAVA_MAJOR+=0
if %JAVA_MAJOR% GTR 0 if %JAVA_MAJOR% LSS 21 (
    echo run.bat: found Java %JAVA_VERSION%, but GAIA Desktop needs Java 21 or newer. 1>&2
    exit /b 1
)

set "FX_SDK="
for /d %%D in ("%SCRIPT_DIR%javafx-sdk*") do if exist "%%~fD\lib\" set "FX_SDK=%%~fD\lib"

rem ---------------------------------------------------------------- class path

set "CLASSPATH=%JAR%"
set "LIB_COUNT=0"

rem Zebra's services only - never External\JavaPOS-1.14.0-Dist\javaPOS114.jar; see the top.
if defined ZEBRA (
    call :optional "%ZEBRA_HOME%bin\JposLogger.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceJniScale.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceJniScanner.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceOnScale.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceOnScanner.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceScale.jar"
    call :optional "%ZEBRA_HOME%bin\JposServiceScanner.jar"
    rem The directories themselves, for the resources the services read from the class path.
    rem No trailing backslash: before the closing quote of -cp it would escape the quote.
    call :optional "%ZEBRA_HOME%bin"
)

rem Every jar in lib\, named one by one rather than left to a "lib\*" wildcard, so that the
rem count below is real, a path containing spaces stays one entry, and single jars can be left
rem out. The append runs as a subroutine rather than under delayed expansion, which would eat
rem any "!" in the path.
for %%J in ("%SCRIPT_DIR%lib\*.jar") do call :lib "%%~fJ" "%%~nJ"

rem Nothing in lib\ is correct for the single-file build and fatal for the default one, and this
rem script cannot tell which jar it was handed - so say so and carry on rather than guess.
if "%LIB_COUNT%"=="0" (
    echo run.bat: note - no jars in lib\ next to gaia-desktop.jar. 1>&2
    echo          Expected only for the single-file build ^(mvn -Pfat package^). The default 1>&2
    echo          build needs them; if startup fails, rebuild with "mvn package". 1>&2
)

rem ---------------------------------------------------------------- native libraries

rem Zebra's JavaPOS services load CSJPOSScanner.dll / CSJPOSScanner64.dll with
rem System.loadLibrary, which searches java.library.path and nothing else. By default that is
rem the PATH, so the SDK's bin\ is added to it. DLLs shipped beside this script are named
rem explicitly, ahead of the PATH: setting the property replaces the default search path
rem outright, which is why the PATH is appended rather than dropped. LIBPATH_OPT carries its
rem own quotes and is expanded unquoted below, so that it disappears entirely when no DLL is
rem present instead of handing java an empty "" argument.
if defined ZEBRA set "PATH=%PATH%;%ZEBRA_HOME%bin"
set "NATIVE_DIR=%SCRIPT_DIR:~0,-1%"
set "LIBPATH_OPT="
if exist "%SCRIPT_DIR%*.dll" set LIBPATH_OPT="-Djava.library.path=%NATIVE_DIR%;%PATH%"

rem ---------------------------------------------------------------- JVM options

rem JavaFX from the SDK runs as named modules; from lib\ it is on the class path, where the
rem Launcher shim (the main class) is what lets it start.
set "FX_OPTS="
if defined FX_SDK set FX_OPTS=--module-path "%FX_SDK%" --add-modules javafx.controls,javafx.web,javafx.fxml

rem Newer JDKs warn - and future releases will refuse - when code loads native libraries
rem without permission; JavaFX and the Zebra services both do. The javafx modules can only be
rem named when they are modules, i.e. from the SDK; on the class path ALL-UNNAMED covers them.
rem Skipped only when the version above could not be read (JAVA_MAJOR 0).
set "NATIVE_OPTS="
if %JAVA_MAJOR% GEQ 17 (
    if defined FX_SDK (
        set "NATIVE_OPTS=--enable-native-access=ALL-UNNAMED,javafx.graphics,javafx.web"
    ) else (
        set "NATIVE_OPTS=--enable-native-access=ALL-UNNAMED"
    )
)

rem The JDK's own SAX parser for jpos.xml rather than lib\'s Xerces 2.12.2, which rejects the
rem JAXP property the JavaPOS loader sets and so loads no device entries at all. Proxies and
rem trusted certificates come from Windows' own settings, as a corporate network expects.
set SYSTEM_OPTS=-Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl -Djava.net.useSystemProxies=true -Djavax.net.ssl.trustStoreType=Windows-ROOT

rem One line saying what was picked, since every choice above is made silently otherwise.
set "FX_FROM=lib\"
if defined FX_SDK set "FX_FROM=%FX_SDK%"
set "ZEBRA_FROM=none (Zebra Scanner SDK not installed)"
if defined ZEBRA set "ZEBRA_FROM=%ZEBRA_HOME%"
echo run.bat: Java %JAVA_VERSION% from %JAVA_FROM%; JavaFX from %FX_FROM%; JavaPOS from lib\; Zebra services from %ZEBRA_FROM%

"%JAVA%" %GAIA_DESKTOP_OPTS% %FX_OPTS% %NATIVE_OPTS% %SYSTEM_OPTS% %LIBPATH_OPT% -cp "%CLASSPATH%" "%MAIN_CLASS%" %*
exit /b %ERRORLEVEL%

rem ---------------------------------------------------------------- subroutines

rem A Zebra SDK entry, added only if this installation has it: SDK versions differ in which
rem service jars they ship.
:optional
if exist "%~1" set "CLASSPATH=%CLASSPATH%;%~1"
goto :eof

rem A jar from lib\. %1 is its full path, %2 its name without .jar.
:lib
set "LIB_NAME=%~2"
if defined FX_SDK if /i "%LIB_NAME:~0,7%"=="javafx-"  goto :eof
set "CLASSPATH=%CLASSPATH%;%~1"
set /a LIB_COUNT+=1
goto :eof
