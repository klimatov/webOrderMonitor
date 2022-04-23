package orderProcessing

import dev.inmo.tgbotapi.extensions.utils.formatting.*
import dev.inmo.tgbotapi.types.MessageEntity.textsources.TextSourcesList
import dev.inmo.tgbotapi.types.MessageEntity.textsources.bold
import dev.inmo.tgbotapi.types.MessageEntity.textsources.italic
import dev.inmo.tgbotapi.types.MessageEntity.textsources.plus
import orderProcessing.data.WebOrder

class BotMessage {

    fun orderMessage(webOrder: WebOrder?): TextSourcesList {
        val resultMessage = buildEntities {
            regularln("✉№${webOrder?.webNum}/${webOrder?.orderId}")
            regularln("${webOrder?.ordType} ")
            underlineln("${webOrder?.docDate}")
            regularln("${if (webOrder?.paid == "Y") "\uD83D\uDCB0Онлайн оплата" else "\uD83E\uDDFEНе оплачен"} \uD83D\uDCB5${webOrder?.docSum} руб.")
            regular("${webOrder?.fioCustomer} ")
            phone("+${webOrder?.phone}")
            webOrder?.items?.forEach {
                linkln("\n\n\uD83D\uDD35${it.goodCode} ${it.name}", "https://${it.eshopUrl}\n")
                regularln("\uD83D\uDCB0Цена: ${it.amount} \uD83D\uDED2Кол-во: ${it.quantity}")
                bold("\uD83E\uDDF3Остатки:")
                it.remains.forEach { remains ->
                    bold(" ${remains.storageKind.toString()} - ${remains.quantity.toString()} шт.")
                }
            }
        }
        return resultMessage
    }

    fun inworkMessage (webOrder: WebOrder?):TextSourcesList {
        val resultMessage = buildEntities {
            codeln("⭕\uD83D\uDEE0Собираем !!!")
        }.plus(orderMessage(webOrder))
        return resultMessage
    }

    fun completeMessage (webOrder: WebOrder?):TextSourcesList {
        val resultMessage = buildEntities {
            boldln("✅Подтверждена!")
        }.plus(italic(orderMessage(webOrder)))
        return resultMessage
    }
}