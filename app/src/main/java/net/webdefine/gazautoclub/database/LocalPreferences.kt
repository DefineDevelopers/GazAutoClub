package net.webdefine.gazautoclub.database

import android.content.SharedPreferences
import net.webdefine.gazautoclub.App

object LocalPreferences {
    val PREFS_FILENAME = "net.webdefine.gazautoclub.prefs"
    private var prefs: SharedPreferences

    init {
        this.prefs = App.instance.getSharedPreferences(PREFS_FILENAME, 0)
    }

    fun addString(key: String, value: String?) {
        this.prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defValue: String): String {
        return this.prefs.getString(key, defValue)
    }

    fun checkIfContain(key: String): Boolean {
        return this.prefs.contains(key)
    }

    fun delete(key: String) {
        this.prefs.edit().remove(key).apply()
    }
}