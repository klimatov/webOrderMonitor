package bot

import android.util.Log
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.answers.answerInlineQuery
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.MessageEntity.textsources.bold
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
    var msgNotification = msgConvert.shopInWork()
    var dayConfirmedCount: Int = 0  //подтверждено за день

    suspend fun botDaemonStart() {
        val scope = CoroutineScope(Dispatchers.IO)

        bot.buildBehaviourWithLongPolling(scope) {
            onCommand("status") {
                sendTextMessage(it.chat, "Bot online")
            }

            onDataCallbackQuery {
                Log.d("webOrderMonitor", "Callback: ${it.data} from ${it.user.firstName} ${it.user.lastName} ${it.user.username?.usernameWithoutAt}")
                answer(
                    it, msgConvert.popupMessage(), showAlert = true
                )
            }

            Log.i("webOrderMonitor", "Bot started: ${getMe()}")
        }.start()
    }

    suspend fun botSendMessage(webOrder: WebOrder?): Long? {
        try {
            return bot.sendMessage(
                targetChatId,
                msgConvert.inworkMessage(webOrder),
                disableWebPagePreview = true,
                disableNotification = !msgNotification
            ).messageId
        } catch (e: Exception) {
            Log.e("webOrderMonitor", "Exception: ${e.message}")
            return TGInfoMessage.currentInfoMsgId ?: 0
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

    suspend fun botSendInfoMessage() {
        try {
            bot.sendMessage(
                targetChatId,
                msgConvert.notificationMessage(msgNotification),
                disableWebPagePreview = true,
                disableNotification = !msgNotification
            ).messageId
        } catch (e: Exception) {
            Log.e("webOrderMonitor", "Exception: ${e.message}")
        }
    }
}