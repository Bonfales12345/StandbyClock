package com.standby.clock.services

import android.content.pm.ActivityInfo
import android.os.Build
import android.service.dreams.DreamService
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.standby.clock.ui.MainScreen
import com.standby.clock.ui.MainViewModel
import com.standby.clock.ui.theme.StandByClockTheme

class StandByDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private var orientationListener: OrientationEventListener? = null

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        } catch (e: Throwable) {

        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = true
        isFullscreen = true
        isScreenBright = true

        try {

            val lp = window.attributes
            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = lp
        } catch (e: Throwable) {

        }

        try {

            orientationListener = object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return
                    try {
                        val lp = window.attributes
                        if (orientation in 60..120) {
                            if (lp.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                                lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                                window.attributes = lp
                            }
                        } else if (orientation in 240..300) {
                            if (lp.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                window.attributes = lp
                            }
                        }
                    } catch (e: Throwable) {}
                }
            }
            if (orientationListener?.canDetectOrientation() == true) {
                orientationListener?.enable()
            }
        } catch (e: Throwable) {}

        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (e: Throwable) {

        }

        try {

            window.decorView.setViewTreeLifecycleOwner(this)
            window.decorView.setViewTreeViewModelStoreOwner(this)
            window.decorView.setViewTreeSavedStateRegistryOwner(this)

            val viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )[MainViewModel::class.java]

            val composeView = ComposeView(this).apply {
                setContent {
                    StandByClockTheme {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }

            setContentView(composeView)
        } catch (e: Throwable) {

        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        try {

            val lp = window.attributes
            if (lp.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE &&
                lp.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                window.attributes = lp
            }
        } catch (e: Throwable) {}

        try {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        } catch (e: Throwable) {

        }
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        try {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        } catch (e: Throwable) {

        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            orientationListener?.disable()
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            store.clear()
        } catch (e: Throwable) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                store.clear()
            }
        } catch (e: Throwable) {

        }
    }
}
