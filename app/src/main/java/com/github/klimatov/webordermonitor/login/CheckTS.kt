package com.github.klimatov.webordermonitor.login

class CheckTS {
    fun checkTs(user: String, password: String, shop: String): Boolean {
        if ((user.isEmpty()) || (password.isEmpty()) || (shop.isEmpty())) return false
        else {
            return true
        }
    }
}