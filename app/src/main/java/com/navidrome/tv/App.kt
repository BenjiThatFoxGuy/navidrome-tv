package com.navidrome.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.navidrome.tv.di.ServiceLocator

class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { ServiceLocator.okHttpClient }
        .crossfade(220)
        .allowHardware(false)
        .build()
}
