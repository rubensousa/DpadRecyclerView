#!/bin/bash
adb uninstall androidx.test.orchestrator 2> /dev/null
adb uninstall androidx.test.services 2> /dev/null
./gradlew uninstalAll || exit 1
exit 0
