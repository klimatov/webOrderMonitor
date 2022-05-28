package com.github.klimatov.webordermonitor.login

import android.content.SharedPreferences
import com.github.klimatov.webordermonitor.databinding.ActivityLoginBinding

object SavedSettings {
    var login: String = ""
    var password: String = ""
    var shop: String = ""
    var shopOpening: String = "10"
    var shopClosing: String = "21"
    var tgBotToken: String = ""
    var tgChatId: String = ""
    var autoLogin: String = "true"

    fun readSettings(sharedPreferences: SharedPreferences) {
        login = sharedPreferences.getString("TS_LOGIN", "").toString()
        password = sharedPreferences.getString("TS_PASSWORD", "").toString()
        shop = sharedPreferences.getString("TS_SHOP", "").toString()
        shopOpening = sharedPreferences.getString("SHOP_OPENING", "10").toString()
        shopClosing = sharedPreferences.getString("SHOP_CLOSING", "21").toString()
        tgBotToken = sharedPreferences.getString("TELEGRAM_BOT_TOKEN", "").toString()
        tgChatId = sharedPreferences.getString("TELEGRAM_CHAT_ID", "").toString()
        autoLogin = sharedPreferences.getString("AUTO_LOGIN", "true").toString()
    }

    fun writeSettings(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().putString("TS_LOGIN", login).apply()
        sharedPreferences.edit().putString("TS_PASSWORD", password).apply()
        sharedPreferences.edit().putString("TS_SHOP", shop).apply()
        sharedPreferences.edit().putString("SHOP_OPENING", shopOpening).apply()
        sharedPreferences.edit().putString("SHOP_CLOSING", shopClosing).apply()
        sharedPreferences.edit().putString("TELEGRAM_BOT_TOKEN", tgBotToken).apply()
        sharedPreferences.edit().putString("TELEGRAM_CHAT_ID", tgChatId).apply()
        sharedPreferences.edit().putString("AUTO_LOGIN", autoLogin).apply()
    }

    fun setBindings(binding: ActivityLoginBinding) {
        binding.tsUser.setText(login)
        binding.tsPassword.setText(password)
        binding.tsShop.setText(shop)
        binding.shopOpening.setText(shopOpening)
        binding.shopClosing.setText(shopClosing)
        binding.tgBotToken.setText(tgBotToken)
        binding.tgChatId.setText(tgChatId)
        binding.autoLogin.isChecked = autoLogin.toBoolean()
    }

    fun getBindings(binding:ActivityLoginBinding) {
        login = binding.tsUser.text.toString()
        password = binding.tsUser.text.toString()
        shop = binding.tsShop.text.toString()
        shopOpening = binding.shopOpening.text.toString()
        shopClosing = binding.shopClosing.text.toString()
        tgBotToken = binding.tgBotToken.text.toString()
        tgChatId = binding.tgChatId.text.toString()
        autoLogin = binding.autoLogin.isChecked.toString()
    }
}