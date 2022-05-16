package orderProcessing

import DateTimeProcess
import bot.TGInfoMessage
import bot.TGbot
import dev.inmo.tgbotapi.extensions.utils.formatting.*
import dev.inmo.tgbotapi.types.MessageEntity.textsources.TextSourcesList
import dev.inmo.tgbotapi.types.MessageEntity.textsources.italic
import orderProcessing.data.SecurityData.SHOP_CLOSING
import orderProcessing.data.SecurityData.SHOP_OPENING
import orderProcessing.data.WebOrder
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.time.temporal.ChronoUnit

class BotMessage {

    fun orderMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            regularln("#️⃣${webOrder?.webNum}/${webOrder?.orderId}")
            regular("${webOrder?.ordType} ")
            if (webOrder?.isLegalEntity == "Y") bold("СЧЁТ КОНТРАГЕНТА")
            underlineln("\n\uD83D\uDCC6${DateTimeProcess().replaceDateTime(webOrder?.docDate?:"")}")
            regularln("${if (webOrder?.paid == "Y") "\uD83D\uDCB0Онлайн оплата" else "\uD83E\uDDFEНе оплачен"} \uD83D\uDCB5${webOrder?.docSum} руб.")
            regular("\uD83D\uDC68${webOrder?.fioCustomer} ")
            phone("+${webOrder?.phone}")
            webOrder?.items?.forEach {
                linkln("\n\n\uD83D\uDD35${it.goodCode} ${it.name}", "https://${it.eshopUrl}\n")
                regular("\uD83D\uDCB0Цена: ${it.amount} ")
                if ((it.quantity ?: 0) > 1) regular("\uD83E\uDDF3Кол-во: ${it.quantity}")
                bold("\n\uD83D\uDED2Остатки:")
                it.remains.forEach { remains ->
                    bold(" ${remains.storageKind.toString()} - ${remains.quantity.toString()} шт.")
                }
            }
        }
        return resultMessage
    }

    fun inworkMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            codeln("⭕\uD83D\uDEE0Собираем ${minutesEnding(timeDiff(webOrder?.docDate))}!")
        }.plus(orderMessage(webOrder))
        return resultMessage
    }

    fun completeMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            boldln("✅Подтверждена за ${minutesEnding(timeDiff(webOrder?.docDate))}!")
        }.plus(italic(orderMessage(webOrder)))
        return resultMessage
    }

    fun timeDiff(docDate: String?, lateDate: LocalDateTime = LocalDateTime.now()): Long {
        if (docDate == null) return 0
        val docDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val docDateFormatting = LocalDateTime.parse(docDate, docDateFormat)
        return docDateFormatting.until(lateDate, ChronoUnit.MINUTES)
    }

    fun timeNow(time: LocalTime = LocalTime.now()): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        return formatter.format(time)
    }

    fun shopInWork(time: LocalTime = LocalTime.now()): Boolean {
        return time.hour in SHOP_OPENING until SHOP_CLOSING
    }

    fun minutesEnding(minute: Long): String {
        return "${minute} " + minute.let {
            if (it % 100 in 11..14) {
                "минут"
            } else {
                when ((it % 10).toInt()) {
                    1 -> "минуту"
                    2, 3, 4 -> "минуты"
                    else -> "минут"//0, 5, 6, 7, 8, 9
                }
            }
        }
    }

    fun orderEnding(order: Int): String {
        return "${order} " + order.let {
            if (it % 100 in 11..14) {
                "заявок"
            } else {
                when ((it % 10)) {
                    1 -> "заявка"
                    2, 3, 4 -> "заявки"
                    else -> "заявок"//0, 5, 6, 7, 8, 9
                }
            }
        }
    }

    fun infoMessage(): String {
        val resTxt: String
        if (TGInfoMessage.notConfirmedOrders == 0) {
            resTxt = "✅ Все заявки подтверждены ${timeNow()}"
        } else {
            resTxt =
                "⭕\uD83D\uDEE0 В подборе ${orderEnding(TGInfoMessage.notConfirmedOrders)} ${timeNow()}"
        }
        return resTxt
    }

    fun notificationMessage(notification: Boolean): String {
        if (notification) {
            return "\uD83D\uDD08 Магазин открыт, включаем уведомления!"
        } else {
            return "\uD83D\uDD07 Магазин закрыт, отключаем уведомления!\n" +
                    "\uD83D\uDE2B Сегодня за день упало ${orderEnding(TGbot.dayConfirmedCount)}"
        }
    }

    fun popupMessage(): String {
        return "Сегодня упало ${orderEnding(TGbot.dayConfirmedCount)}"
    }
}