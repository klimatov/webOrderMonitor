package bot

import android.util.Log
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.MessageIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import orderProcessing.BotMessage
import orderProcessing.data.SecurityData
import orderProcessing.data.WebOrder

object TGbot {
    private val botToken: String = SecurityData.TELEGRAM_BOT_TOKEN
    val targetChatId = ChatId(SecurityData.TELEGRAM_CHAT_ID)
    val bot = telegramBot(botToken)
    val msgConvert = BotMessage()

    suspend fun botDaemonStart() {
        val scope = CoroutineScope(Dispatchers.IO)

        bot.buildBehaviourWithLongPolling(scope) {
            onCommand("status") {
                sendTextMessage(it.chat, "Bot online")
            }
            Log.i("webOrderMonitor", "Bot started: ${getMe()}")
        }.start()
    }

    suspend fun botSendMessage(webOrder: WebOrder?): MessageIdentifier {
        try {
            return bot.sendMessage(
                targetChatId,
                msgConvert.inworkMessage(webOrder),
                disableWebPagePreview = true
            ).messageId
        } catch (e: Exception) {
            Log.e("webOrderMonitor", "Exception: ${e.message}")
            return 0
        }
    }

    suspend fun botConfirmMessage(webOrder: WebOrder?) {
        try {
            bot.editMessageText(
                targetChatId,
                webOrder?.messageId ?: 0,
                msgConvert.completeMessage(webOrder),
                disableWebPagePreview = true
            )
        } catch (e: Exception) {
            Log.e("webOrderMonitor", "Exception: ${e.message}")
        }
    }

    suspend fun botTimerUpdate(webOrder: WebOrder?) {
        try {
            if (webOrder?.activeTime != msgConvert.timeDiff(webOrder?.docDate)) {
                bot.editMessageText(
                    targetChatId,
                    webOrder?.messageId ?: 0,
                    msgConvert.inworkMessage(webOrder),
                    disableWebPagePreview = true,
                    replyMarkup = if (webOrder?.messageId == TGInfoMessage.currentInfoMsgId) TGInfoMessage.currentInfoMsg else null
                )
            }
        } catch (e: Exception) {
            Log.e("webOrderMonitor", "Exception: ${e.message}")
        }
    }
}