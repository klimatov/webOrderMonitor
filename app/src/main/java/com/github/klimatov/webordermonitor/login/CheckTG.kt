package com.github.klimatov.webordermonitor.login

class CheckTG {

    fun checkTg(botToken: String, chatId: String): Boolean {
        if ((botToken.isEmpty()) || (chatId.isEmpty())) return false
        else {
            return true
        }
    }
}