package admin.arpan.delivery.utils

import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.models.User
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class Preference(var application: Application) {

    var authPreferences: SharedPreferences = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveUser(user : User){
        val data = getGsonParser()?.toJson(user)
        authPreferences.edit().putString("userData", data).apply()
    }

    fun getUser(): User? {
        return getGsonParser()?.fromJson<User>(authPreferences.getString("userData", ""), User::class.java)
    }

    fun saveTokens(tokens: Tokens){
        val data = getGsonParser()?.toJson(tokens)
        authPreferences.edit().putString("tokensData", data).apply()
    }

    fun getTokens(): Tokens? {
        return getGsonParser()?.fromJson<Tokens>(authPreferences.getString("tokensData", ""), Tokens::class.java)
    }

    fun clearAuth() {
        authPreferences.edit().clear().apply()
    }

}