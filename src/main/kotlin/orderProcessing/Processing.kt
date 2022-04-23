package orderProcessing

import bot.TGbot
import dev.inmo.tgbotapi.types.MessageIdentifier
import orderProcessing.data.WebOrder
import orderProcessing.data.WebOrderSimply

class Processing() {
    var activeOrders: MutableMap<String?, WebOrder?> = mutableMapOf() //иниц. список активных вебок

    suspend fun processInworkOrders(inworkOrderList: List<WebOrderSimply>) {
        // проверка, появились ли новые вебки, отсутствующие в списке активных?
        inworkOrderList.forEach { webOrder ->
            if (!activeOrders.containsKey(webOrder.webNum)) {
                activeOrders[webOrder.webNum] = OrderDaemon.getOrderList(webOrder.webNum)[0]// добавляем новую вебку в список активных
                activeOrders[webOrder.webNum]?.items = OrderDaemon.getItems(activeOrders[webOrder.webNum]?.orderId) // обновляем список товара (items)
                activeOrders[webOrder.webNum]?.items?.forEach { items ->  // обновляем остатки по каждому товару
                    items.remains = OrderDaemon.getRemains(items.goodCode)
                }
                newOrder(webOrder.webNum)
            }
        }
        println(activeOrders)

        // проверка, исчезли (подтверждены) ли ранее сохраненные вебки?
        var delOrderList: MutableList<String> = mutableListOf()
        activeOrders.forEach{activeOrder ->
            var deleteFlag = true
            inworkOrderList.forEach {
                if (activeOrder.key == it.webNum) deleteFlag = false
            }
            if (deleteFlag) delOrderList.add(activeOrder.key.toString())//delOrder(activeOrder.key)
        }
        // удаляем подтвержденные
        delOrderList.forEach {
            delOrder(it)
        }
    }

    suspend fun newOrder (webNum: String?) {
        val messageId: MessageIdentifier = TGbot.botSendMessage(activeOrders[webNum])
        activeOrders[webNum]?.messageId = messageId
    }

    suspend fun delOrder (webNum: String?) {
        TGbot.botEditMessage(activeOrders[webNum])
        activeOrders.remove(webNum)
    }


}