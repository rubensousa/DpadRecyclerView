#!/bin/bash
adb uninstall androidx.test.orchestrator 2> /dev/null
adb uninstall androidx.test.services 2> /dev/null
adb install -r artifacts/orchestrator-1.4.2.apk || exit 1
adb install -r artifacts/test-services-1.4.2.apk || exit 1
testServicesClasspath=$(adb shell pm path androidx.test.services)
./gradlew uninstalAll || exit 1
./gradlew installDebugAndroidTest || exit 1

adb logcat -c
adb logcat > logcat.txt &

instrumentedLogFile=instrumentation_logs.txt
adb shell CLASSPATH="$testServicesClasspath" app_process / androidx.test.services.shellexecutor.ShellMain am instrument -r -w -e targetInstrumentation com.rubensousa.dpadrecyclerview.test/androidx.test.runner.AndroidJUnitRunner -e useTestStorageService true -e class com.rubensousa.dpadrecyclerview.test.tests.layout.VerticalColumnTest androidx.test.orchestrator/androidx.test.orchestrator.AndroidTestOrchestrator 2>&1 | tee "$instrumentedLogFile"
testRunCode=$?

if [ $testRunCode -ne 0 ]; then
  echo "error executing instrumented tests: "$testRunCode
  exit $testRunCode
fi

cat "$instrumentedLogFile" | grep "FAILURES!!!"
if [ $? -eq 0 ]; then
    echo "Instrumented tests failed"
    exit 1
fi

echo "Instrumented tests passed"
exit 0