package orderProcessing

import dev.inmo.tgbotapi.extensions.utils.formatting.*
import dev.inmo.tgbotapi.types.MessageEntity.textsources.TextSourcesList
import dev.inmo.tgbotapi.types.MessageEntity.textsources.italic
import orderProcessing.data.WebOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class BotMessage {

    fun orderMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            regularln("#️⃣№${webOrder?.webNum}/${webOrder?.orderId}")
            regularln("${webOrder?.ordType} ")
            underlineln("\uD83D\uDCC6${webOrder?.docDate}")
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
            codeln("⭕\uD83D\uDEE0Собираем ${timeDiff(webOrder?.docDate)} минут!")
        }.plus(orderMessage(webOrder))
        return resultMessage
    }

    fun completeMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            boldln("✅Подтверждена за ${timeDiff(webOrder?.docDate)} минут!")
        }.plus(italic(orderMessage(webOrder)))
        return resultMessage
    }

    fun timeDiff (docDate: String?, lateDate: LocalDateTime = LocalDateTime.now()): Long {
        if (docDate==null) return 0
        val docDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val docDateFormatting = LocalDateTime.parse(docDate, docDateFormat)
        return docDateFormatting.until(lateDate, ChronoUnit.MINUTES)
    }
}