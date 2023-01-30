#!/bin/bash
adb uninstall androidx.test.orchestrator 2> /dev/null
adb uninstall androidx.test.services 2> /dev/null
adb install -r artifacts/orchestrator-1.4.2.apk || exit 1
adb install -r artifacts/test-services-1.4.2.apk || exit 1
./gradlew uninstalAll || exit 1
./gradlew installDebugAndroidTest || exit 1
echo "Test APKs installed"
exit 0