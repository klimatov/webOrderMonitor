package com.github.klimatov.webordermonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("webOrderMonitor", "ACTION_BOOT_COMPLETED is recived and app is launched ")
            val autoStartIntent = Intent(context, LoginActivity::class.java)
            autoStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(autoStartIntent)
        }
    }
}