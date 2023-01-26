#!/bin/bash
adb uninstall androidx.test.orchestrator 2> /dev/null
adb uninstall androidx.test.services 2> /dev/null
adb install -r artifacts/orchestrator-1.4.2.apk
adb install -r artifacts/test-services-1.4.2.apk
testServicesClasspath=$(adb shell pm path androidx.test.services)
./gradlew installDebugAndroidTest
adb logcat -c
adb logcat > instrumented_test_logs.txt &
adb shell CLASSPATH="$testServicesClasspath" app_process / androidx.test.services.shellexecutor.ShellMain am instrument -r -w -e targetInstrumentation com.rubensousa.dpadrecyclerview.test/androidx.test.runner.AndroidJUnitRunner -e useTestStorageService true androidx.test.orchestrator/androidx.test.orchestrator.AndroidTestOrchestrator