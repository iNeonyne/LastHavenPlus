package com.lasthaven.launcher

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rx.preferenceDataStore
import androidx.datastore.rx.RxDataStore
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.prefs.PreferencesFactory

class SettingsStore(private val context: Context) {

    private val Context.dataStore: RxDataStore<Preferences> by preferenceDataStore("settings")

    // Keys
    private val GAME_URL_KEY = preferencesKey<String>("game_url")
    private val FULLSCREEN_KEY = preferencesKey<Boolean>("fullscreen")
    private val ORIENTATION_KEY = preferencesKey<String>("orientation")
    private val FIRST_LAUNCH_KEY = preferencesKey<Boolean>("first_launch")

    // Defaults
    private const val DEFAULT_GAME_URL = "https://lasthavens.com/play"
    private const val DEFAULT_FULLSCREEN = true
    private const val DEFAULT_ORIENTATION = "landscape"
    private const val DEFAULT_FIRST_LAUNCH = true

    // Orientation options
    enum class Orientation(val value: String, val labelRes: Int) {
        PORTRAIT("portrait", R.string.orientation_portrait),
        LANDSCAPE("landscape", R.string.orientation_landscape),
        AUTO("auto", R.string.orientation_auto)
    }

    // Game URL
    val gameUrl: Flowable<String> = context.dataStore.data
        .map { it[GAME_URL_KEY] ?: DEFAULT_GAME_URL }
        .distinctUntilChanged()

    suspend fun setGameUrl(url: String) {
        context.dataStore.edit { it[GAME_URL_KEY] = url }
    }

    // Fullscreen
    val fullscreen: Flowable<Boolean> = context.dataStore.data
        .map { it[FULLSCREEN_KEY] ?: DEFAULT_FULLSCREEN }
        .distinctUntilChanged()

    suspend fun setFullscreen(enabled: Boolean) {
        context.dataStore.edit { it[FULLSCREEN_KEY] = enabled }
    }

    // Orientation
    val orientation: Flowable<Orientation> = context.dataStore.data
        .map { Orientation.valueOf((it[ORIENTATION_KEY] ?: DEFAULT_ORIENTATION).uppercase()) }
        .distinctUntilChanged()

    suspend fun setOrientation(orientation: Orientation) {
        context.dataStore.edit { it[ORIENTATION_KEY] = orientation.value }
    }

    // First launch
    val isFirstLaunch: Flowable<Boolean> = context.dataStore.data
        .map { it[FIRST_LAUNCH_KEY] ?: DEFAULT_FIRST_LAUNCH }
        .distinctUntilChanged()

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { it[FIRST_LAUNCH_KEY] = false }
    }

    // Get current values synchronously (for initialization)
    suspend fun getGameUrl(): String = context.dataStore.data
        .firstOrNull()?.get(GAME_URL_KEY) ?: DEFAULT_GAME_URL

    suspend fun getFullscreen(): Boolean = context.dataStore.data
        .firstOrNull()?.get(FULLSCREEN_KEY) ?: DEFAULT_FULLSCREEN

    suspend fun getOrientation(): Orientation = Orientation.valueOf(
        (context.dataStore.data.firstOrNull()?.get(ORIENTATION_KEY) ?: DEFAULT_ORIENTATION).uppercase()
    )
}
