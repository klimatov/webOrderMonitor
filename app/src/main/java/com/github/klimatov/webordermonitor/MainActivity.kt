package com.github.klimatov.webordermonitor

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import bot.TGbot
import com.github.klimatov.webordermonitor.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.*
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
            mainScope.cancel()
            OrderDaemon.scope.cancel()
            TGbot.scope.cancel()

            finishAndRemoveTask()
            exitProcess(0)
        }

        binding.relogButton.setOnClickListener {
            mainScope.cancel()
            OrderDaemon.scope.cancel()
            TGbot.scope.cancel()

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)

            finishAfterTransition()
        }
    }

    private val mainScope = CoroutineScope(Dispatchers.Default).launch {
        TGbot.botDaemonStart()
        OrderDaemon.orderDaemonStart(binding, sharedPreferences)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("webOrderMonitor", "onDestroy")
        mainScope.cancel()
        OrderDaemon.scope.cancel()
        TGbot.scope.cancel()
    }
}