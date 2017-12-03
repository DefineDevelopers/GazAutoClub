package net.webdefine.gazautoclub

import android.app.Application
import net.webdefine.gazautoclub.delegates.DelegatesExt

class App : Application() {
    companion object {
        var instance: App by DelegatesExt.notNullSingleValue()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}