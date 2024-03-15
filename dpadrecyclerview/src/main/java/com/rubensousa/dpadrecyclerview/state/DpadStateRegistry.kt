/*
 * Copyright 2024 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.state

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

private const val SAVED_STATE_PROVIDER_KEY = "#DpadStateRegistry"
private const val SCROLL_STATE_KEY = "#ScrollStates"
private const val VIEW_HOLDER_STATE_KEY = "#ViewHolderStates"

/**
 * Saves and restores scroll states and ViewHolder states.
 * Check [DpadScrollState] and [DpadViewHolderState] for more information.
 *
 * This class is lifecycle-aware and will contribute to
 * [Activity.onSaveInstanceState] or [Fragment.onSaveInstanceState] via [SavedStateRegistryOwner],
 * unless [setSaveInstanceStateEnabled] is used to disable this behavior.
 */
class DpadStateRegistry(registryOwner: SavedStateRegistryOwner) {

    private val scrollState = DpadScrollState()
    private val viewHolderState = DpadViewHolderState()
    private val savedStateProvider = SavedStateRegistry.SavedStateProvider { saveState() }
    private var saveInstanceStateEnabled = true
    private var observer: LifecycleEventObserver? = null

    init {
        if (registryOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            register(registryOwner)
        } else {
            observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) {
                    register(registryOwner)
                    observer?.let { registryOwner.lifecycle.removeObserver(it) }
                    observer = null
                }
            }.also {
                registryOwner.lifecycle.addObserver(it)
            }
        }
    }

    /**
     * Control whether state should be saved via [Activity.onSaveInstanceState]
     * or [Fragment.onSaveInstanceState], false otherwise. Default is true.
     *
     * @param enabled true if state should be persisted, false otherwise.
     */
    fun setSaveInstanceStateEnabled(enabled: Boolean) {
        saveInstanceStateEnabled = enabled
    }

    /**
     * @return [DpadScrollState] to save and restore scroll states of [DpadRecyclerView]
     */
    fun getScrollState(): DpadScrollState {
        return scrollState
    }

    /**
     * @return [DpadViewHolderState] to save and restore view states
     * of ViewHolders part of a  [DpadRecyclerView]
     */
    fun getViewHolderState(): DpadViewHolderState {
        return viewHolderState
    }

    private fun register(registryOwner: SavedStateRegistryOwner) {
        val registry = registryOwner.savedStateRegistry
        registry.registerSavedStateProvider(SAVED_STATE_PROVIDER_KEY, savedStateProvider)
        registry.consumeRestoredStateForKey(SAVED_STATE_PROVIDER_KEY)?.let { bundle ->
            bundle.getBundle(SCROLL_STATE_KEY)?.let { scrollStateBundle ->
                scrollState.restoreState(scrollStateBundle)
            }
            bundle.getBundle(VIEW_HOLDER_STATE_KEY)?.let { viewStateBundle ->
                viewHolderState.restoreState(viewStateBundle)
            }
        }
    }

    private fun saveState(): Bundle {
        if (!saveInstanceStateEnabled) {
            return Bundle()
        }
        val output = Bundle()
        val viewHolderStateBundle = viewHolderState.saveState()
        val scrollStateBundle = scrollState.saveState()
        output.putBundle(SCROLL_STATE_KEY, scrollStateBundle)
        output.putBundle(VIEW_HOLDER_STATE_KEY, viewHolderStateBundle)
        return output
    }

}
