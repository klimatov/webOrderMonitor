package com.github.klimatov.webordermonitor

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.klimatov.webordermonitor.databinding.ActivityLoginBinding
import com.github.klimatov.webordermonitor.login.CheckTG
import com.github.klimatov.webordermonitor.login.CheckTS
import com.github.klimatov.webordermonitor.login.SavedSettings
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("webOrderMonitor_settings", 0)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SavedSettings.readSettings(sharedPreferences)
        SavedSettings.setBindings(binding)

        binding.tsCheckButton.setOnClickListener { doCheckTs() }

        binding.tgCheckButton.setOnClickListener { doCheckTg() }

        binding.buttonOpenDec.setOnClickListener {
            binding.shopOpening.text = hourDec(binding.shopOpening.text.toString().toInt()).toString()
        }

        binding.buttonOpenInc.setOnClickListener {
            binding.shopOpening.text = hourInc(binding.shopOpening.text.toString().toInt()).toString()
        }

        binding.buttonCloseDec.setOnClickListener {
            binding.shopClosing.text = hourDec(binding.shopClosing.text.toString().toInt()).toString()
        }

        binding.buttonCloseInc.setOnClickListener {
            binding.shopClosing.text = hourInc(binding.shopClosing.text.toString().toInt()).toString()
        }

        binding.exitButton.setOnClickListener {
            finishAndRemoveTask()
            exitProcess(0)
        }

        binding.startButton.setOnClickListener {
            if ((doCheckTs()) && (doCheckTg())) {
                Toast.makeText(this, "Записываем и стартуем", Toast.LENGTH_LONG).show()
                SavedSettings.getBindings(binding)
                SavedSettings.writeSettings(sharedPreferences)

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finishAfterTransition()
            } else Toast.makeText(this, "Некорректные данные", Toast.LENGTH_LONG).show()
        }
    }

    private fun hourDec(hour: Int): Int = if (hour - 1 < 0) hour else hour - 1

    private fun hourInc(hour: Int): Int = if (hour + 1 > 23) hour else hour + 1


    private fun doCheckTg(): Boolean {
        val botToken = binding.tgBotToken.text.toString()
        val chatId = binding.tgChatId.text.toString()
        if (CheckTG().checkTg(botToken, chatId)) {
            binding.tgCheckStatus.text = getString(R.string.user_data_ok)
            binding.tgCheckStatus.setTextColor(getColor(R.color.green))
            return true
        } else {
            binding.tgCheckStatus.text = getString(R.string.user_data_error)
            binding.tgCheckStatus.setTextColor(getColor(R.color.red))
            return false
        }
    }

    private fun doCheckTs(): Boolean {
        val user = binding.tsUser.text.toString()
        val password = binding.tsPassword.text.toString()
        val shop = binding.tsShop.text.toString()

        if (CheckTS().checkTs(user, password, shop)) {
            binding.tsCheckStatus.text = getString(R.string.user_data_ok)
            binding.tsCheckStatus.setTextColor(getColor(R.color.green))
            return true
        } else {
            binding.tsCheckStatus.text = getString(R.string.user_data_error)
            binding.tsCheckStatus.setTextColor(getColor(R.color.red))
            return false
        }
    }
}