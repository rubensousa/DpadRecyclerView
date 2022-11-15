package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rubensousa.dpadrecyclerview.DpadRecyclerViewHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        DpadRecyclerViewHelper.enableNewPivotLayoutManager(true)
        setContentView(R.layout.activity_main)
    }

}
