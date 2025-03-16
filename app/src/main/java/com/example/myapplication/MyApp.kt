package com.example.myapplication

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import leakcanary.LeakCanary
import leakcanary.LeakCanary.config

class MyApp : Application(), DefaultLifecycleObserver {
    
    override fun onCreate() {
        super<Application>.onCreate()
        setupLeakCanary()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private fun setupLeakCanary() {
        // En modo debug, LeakCanary se inicializa automáticamente
        // Solo configuramos opciones adicionales si es necesario
        LeakCanary.config = config.copy(
            retainedVisibleThreshold = 3,
            referenceMatchers = config.referenceMatchers
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        // Forzar un GC cuando la app va a background para ayudar a detectar leaks
        System.gc()
        System.runFinalization()
    }

    companion object {
        private var instance: MyApp? = null

        fun getInstance(): MyApp = instance ?: throw IllegalStateException("Application not created")
    }
} 