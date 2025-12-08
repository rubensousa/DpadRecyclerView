/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.sample.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rubensousa.dpadrecyclerview.sample.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("App only available for Android TV")
                .setMessage("This app should be installed on Android TV instead, since it showcases a programming library for that platform")
                .setCancelable(false)
                .setPositiveButton("OK, I UNDERSTAND") { _, _ -> finish() }
                .show()
            return
        }
        setContentView(R.layout.activity_main)
    }
}