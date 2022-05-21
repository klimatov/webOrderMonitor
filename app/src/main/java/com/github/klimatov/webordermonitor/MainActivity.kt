package com.github.klimatov.webordermonitor

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import bot.TGbot
import com.github.klimatov.webordermonitor.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import orderProcessing.OrderDaemon
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        sharedPreferences = getSharedPreferences("webOrderMonitor", 0)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        setContentView(R.layout.activity_main)

        mainScope.start()

        binding.exitButton.setOnClickListener {
            finishAndRemoveTask()
            exitProcess(0)
        }

    }

    private val mainScope = CoroutineScope(Dispatchers.Default).launch {
        TGbot.botDaemonStart()
        OrderDaemon.orderDaemonStart(binding, sharedPreferences)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        Log.d("webOrderMonitor", "onDestroy")
    }
}