package com.rubensousa.dpadrecyclerview.compose

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Similar to [Modifier.clickable], but triggers a sound effect on click.
 * Workaround for: https://issuetracker.google.com/issues/268268856
 */
@Composable
fun Modifier.dpadClickable(action: () -> Unit): Modifier {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    return then(Modifier.clickable {
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
        action()
    })
}