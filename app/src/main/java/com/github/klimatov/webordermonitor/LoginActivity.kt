package com.github.klimatov.webordermonitor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.klimatov.webordermonitor.databinding.ActivityLoginBinding
import com.github.klimatov.webordermonitor.login.CheckTG
import com.github.klimatov.webordermonitor.login.CheckTS
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tsCheckButton.setOnClickListener { doCheckTs() }

        binding.tgCheckButton.setOnClickListener { doCheckTg() }

        binding.exitButton.setOnClickListener {
            finishAndRemoveTask()
            exitProcess(0)
        }

        binding.startButton.setOnClickListener {
            if ((doCheckTs()) && (doCheckTg())) {
                Toast.makeText(this, "${binding.tsUser.text}", Toast.LENGTH_LONG).show()
            }
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finishAfterTransition()
        }
    }

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