#!/bin/bash
#
# Copyright 2023 Rúben Sousa
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
moduleName=$1
packageName="com.rubensousa.${moduleName/-/.}.test"
logDir="logs/$moduleName"
instrumentedLogFile="$logDir/test_results.txt"
logcatFile="$logDir/logcat.txt"
screenRecordingDir="storage/emulated/0/recordings/$packageName"

echo "Removing existing logs at $logDir"
rm -R "$logDir" 2> /dev/null
mkdir -p "$logDir"

echo -n > "$instrumentedLogFile"
echo -n > "$logcatFile"

adb logcat -c
adb logcat > "$logcatFile" &


echo "Clearing previous screen recordings at: $screenRecordingDir"
adb shell rm -R "$screenRecordingDir" 2> /dev/null

testServicesClasspath=$(adb shell pm path androidx.test.services)

echo "Running instrumented tests for $moduleName"

# With orchestrator enabled
#adb shell CLASSPATH="$testServicesClasspath" app_process / androidx.test.services.shellexecutor.ShellMain am instrument -r -w -e targetInstrumentation "$packageName"/androidx.test.runner.AndroidJUnitRunner -e useTestStorageService true -e listener com.rubensousa.dpadrecyclerview.testfixtures.recording.TestRecordingListener androidx.test.orchestrator/androidx.test.orchestrator.AndroidTestOrchestrator 2>&1 | tee "$instrumentedLogFile"

# With orchestrator disabled
adb shell CLASSPATH="$testServicesClasspath" app_process / androidx.test.services.shellexecutor.ShellMain am instrument -r -w -e debug false "$packageName"/androidx.test.runner.AndroidJUnitRunner -e useTestStorageService true -e listener com.rubensousa.dpadrecyclerview.testfixtures.recording.TestRecordingListener 2>&1 | tee "$instrumentedLogFile"
testRunCode=$?

if [ $testRunCode -ne 0 ]; then
  echo "error executing instrumented tests: "$testRunCode
  exit $testRunCode
fi

cat "$instrumentedLogFile" | grep "Aborted"
if [ $? -eq 0 ]; then
    echo "Instrumented tests failed to run. Check logcat output"
    exit 1
fi

cat "$instrumentedLogFile" | grep "FAILURES!!!"
if [ $? -eq 0 ]; then
    adb pull "storage/emulated/0/recordings/$packageName" "$logDir/recordings"
    echo "Instrumented tests failed"
    exit 1
fi

echo "Instrumented tests passed"
exit 0