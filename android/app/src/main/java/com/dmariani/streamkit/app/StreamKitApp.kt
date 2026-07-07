package com.dmariani.streamkit.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point that bootstraps Hilt's dependency graph for the app.
 */
@HiltAndroidApp
class StreamKitApp : Application()
