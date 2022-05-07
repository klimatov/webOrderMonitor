package com.github.klimatov.webordermonitor

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
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("webOrderMonitor", 0)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)

        CoroutineScope(Dispatchers.IO).launch {
            TGbot.botDaemonStart()
            OrderDaemon.orderDaemonStart(binding, sharedPreferences)
        }.start()
    }
}