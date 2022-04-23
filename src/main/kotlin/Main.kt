import bot.TGbot
import dev.inmo.tgbotapi.types.ChatId
import orderProcessing.BotMessage
import orderProcessing.OrderDaemon
import orderProcessing.data.*

suspend fun main() {

    TGbot.botDaemonStart()
    OrderDaemon.orderDaemonStart()

//    while (true) {}
//myTest()
}