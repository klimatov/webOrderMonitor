import bot.TGbot
import orderProcessing.OrderDaemon

suspend fun main() {

    TGbot.botDaemonStart()
    OrderDaemon.orderDaemonStart()

}