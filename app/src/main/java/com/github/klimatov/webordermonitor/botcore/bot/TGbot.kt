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
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.MessageEntity.textsources.bold
import dev.inmo.tgbotapi.types.MessageIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import orderProcessing.BotMessage
import orderProcessing.OrderDaemon
import orderProcessing.data.ListWebOrder
import orderProcessing.data.SecurityData
import orderProcessing.data.WebOrder
import orderProcessing.data.WebOrderDetail

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
                sendTextMessage(
                    it.chat,
                    "Bot online, remote DB version: ${OrderDaemon.netClient.remoteDbVersion}"
                )
            }

            onCommandWithArgs("web") { message, args ->
                if (args.isNotEmpty()) {
                    args.forEach {
                        if ((it.count() == 9) && (it.toIntOrNull() != null)) {

                            try {
                                val orderPoor = OrderDaemon.netClient.getWebOrderList(
                                    "all",
                                    it
                                )?.webOrders?.last()
                                val orderDetail =
                                    OrderDaemon.netClient.getWebOrderDetail(
                                        orderPoor?.orderId,
                                        "WRQST"
                                    )

                                sendTextMessage(
                                    message.chat,
                                    "${orderText(orderPoor)}\n${docText(orderDetail)}",
                                    disableWebPagePreview = true
                                )
                            } catch (e: Exception) {
                                sendTextMessage(
                                    message.chat,
                                    "Ошибка получения данных ${e.message}"
                                )
                                Log.e("webOrderMonitor", "Exception: ${e.message}")
                            }

                        } else sendTextMessage(message.chat, "Некорректный номер заявки $it")
                    }
                }
            }

            onCommandWithArgs("doc") { message, args ->
                if (args.isNotEmpty()) {
                    args.forEach {
                        if ((it.count() == 7) && (it.toIntOrNull() != null)) {

                            try {
                                val orderDetail =
                                    OrderDaemon.netClient.getWebOrderDetail(
                                        it,
                                        "WRQST"
                                    )
                                sendTextMessage(
                                    message.chat,
                                    docText(orderDetail),
                                    disableWebPagePreview = true
                                )
                            } catch (e: Exception) {
                                sendTextMessage(
                                    message.chat,
                                    "Ошибка получения данных ${e.message}"
                                )
                                Log.e("webOrderMonitor", "Exception: ${e.message}")
                            }

                        } else sendTextMessage(message.chat, "Некорректный номер заявки $it")
                    }
                    //sendTextMessage(message.chat, args.size.toString())
                }
            }

            onDataCallbackQuery {
                Log.d(
                    "webOrderMonitor",
                    "Callback: ${it.data} from ${it.user.firstName} ${it.user.lastName} ${it.user.username?.usernameWithoutAt}"
                )
                answer(
                    it, msgConvert.popupMessage(), showAlert = true
                )
            }

            Log.i("webOrderMonitor", "Bot started: ${getMe()}")
        }.start()
    }

    private fun orderText(orderPoor: WebOrder?) =
        "Веб заявка №${orderPoor?.webNum}\n" +
                "Заказ №${orderPoor?.orderId}\n" +
                "Статус заявки: ${orderPoor?.status?.title}\n" +
                "Описание: ${orderPoor?.status?.description}\n"

    private fun docText(orderDetail: WebOrderDetail?) =
        "Веб заявка №${orderDetail?.webOrder?.webNum}\n" +
                "Заказ №${orderDetail?.webOrder?.orderId}\n" +
                "Тип: ${orderDetail?.webOrder?.ordType}\n" +
                "Юр.лицо: ${orderDetail?.webOrder?.isLegalEntity}\n" +
                "ФИО: ${orderDetail?.webOrder?.fioCustomer}\n" +
                "Оплачена: ${orderDetail?.webOrder?.paid}\n" +
                "Дата заказа: ${orderDetail?.webOrder?.docDate}\n" +
                "Сумма: ${orderDetail?.webOrder?.docSum}\n" +
                "Статус заявки: ${orderDetail?.webOrder?.docStatus}\n" +
                "Validate: ${orderDetail?.validate?.validate}\n" +
                "Message: ${orderDetail?.validate?.message}\n" +
                orderDetail?.items?.joinToString { item ->
                    "НН: ${item.goodCode}\n" +
                            "Наименование: ${item.name}\n" +
                            "Цена: ${item.amount}\n" +
                            "ШК: ${item.barCode}\n" +
                            "Ссылка: ${item.eshopUrl}"
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