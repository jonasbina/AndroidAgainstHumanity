package com.jonasbina.cardsagainsthumanity.model

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GHBNews", Context.MODE_PRIVATE)
    fun loadBoolean(name: String, default:Boolean = false): Boolean {
        return sharedPreferences.getBoolean(name, default)
    }

    fun saveBoolean(b:Boolean, name: String){
        sharedPreferences.edit().putBoolean(name, b).apply()
    }
    fun loadInt(name: String, default:Int): Int {
        return sharedPreferences.getInt(name, default)
    }

    fun saveInt(int: Int, name: String){
        sharedPreferences.edit().putInt(name, int).apply()
    }
}
