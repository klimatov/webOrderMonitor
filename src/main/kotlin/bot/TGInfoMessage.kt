package bot

import dev.inmo.tgbotapi.extensions.api.edit.ReplyMarkup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.reply_markup
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup

object TGInfoMessage {
    var currentInfoMsgId: Long? = null
    var newInfoMsgId: Long? = null
    var currentInfoMsg: InlineKeyboardMarkup? = null
    var notConfirmedOrders: Int = 0  //активных не подтвержденных
    var dayConfirmedCount: Int = 0  //подтверждено за день

    suspend fun updateInfoMsg() {
        if (newInfoMsgId != null) {

            if (currentInfoMsgId != newInfoMsgId) {
                delInfoMsg()
                currentInfoMsgId = newInfoMsgId
            }

            val infoMsg = inlineKeyboard {
                row {
                    dataButton(TGbot.msgConvert.infoMessage(), " ")
                }
            }

            if (infoMsg != currentInfoMsg) {
                try {
                    currentInfoMsg = TGbot.bot.editMessageReplyMarkup(
                        TGbot.targetChatId,
                        currentInfoMsgId!!,
                        replyMarkup = infoMsg
                    ).reply_markup
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }
    }

    suspend fun delInfoMsg() {
        try {
            TGbot.bot.editMessageReplyMarkup(TGbot.targetChatId, currentInfoMsgId!!, replyMarkup = null)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}