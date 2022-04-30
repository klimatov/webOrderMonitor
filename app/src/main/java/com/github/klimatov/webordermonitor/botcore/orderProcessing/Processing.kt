package orderProcessing

import bot.TGInfoMessage
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
                activeOrders[webOrder.webNum] =
                    OrderDaemon.getOrderList(webOrder.webNum)[0]// добавляем новую вебку в список активных
                activeOrders[webOrder.webNum]?.items =
                    OrderDaemon.getItems(activeOrders[webOrder.webNum]?.orderId) // обновляем список товара (items)
                activeOrders[webOrder.webNum]?.items?.forEach { items ->  // обновляем остатки по каждому товару
                    items.remains = OrderDaemon.getRemains(items.goodCode)
                }
                activeOrders[webOrder.webNum]?.activeTime = TGbot.msgConvert.timeDiff(webOrder.docDate) // время активности
                newOrder(webOrder.webNum)
            }
        }
        println(activeOrders)

        // проверка, исчезли (подтверждены) ли ранее сохраненные вебки?
        // +обновляем таймера в сообщениях активных вебок
        val delOrderList: MutableList<String> = mutableListOf()
        activeOrders.forEach { activeOrder ->
            updateOrderTimer(activeOrder.key) //обновляем таймер в сообщении
            var deleteFlag = true
            inworkOrderList.forEach {
                if (activeOrder.key == it.webNum) deleteFlag = false
            }
            if (deleteFlag) delOrderList.add(activeOrder.key.toString())//delOrder(activeOrder.key)
        }
        // удаляем подтвержденные вебки
        delOrderList.forEach {
            delOrder(it)
        }

        // обновляем инфокнопку
        TGInfoMessage.notConfirmedOrders = activeOrders.count()
        TGInfoMessage.updateInfoMsg()
    }
    suspend fun newOrder(webNum: String?) {
        val messageId: MessageIdentifier = TGbot.botSendMessage(activeOrders[webNum])
        TGInfoMessage.newInfoMsgId = messageId // messageID последнего сообщения для инфокнопки
        activeOrders[webNum]?.messageId = messageId
    }

    suspend fun delOrder(webNum: String?) {
        TGbot.botConfirmMessage(activeOrders[webNum])
        activeOrders.remove(webNum)
    }

    suspend fun updateOrderTimer(webNum: String?){
        TGbot.botTimerUpdate(activeOrders[webNum])
        activeOrders[webNum]?.activeTime = TGbot.msgConvert.timeDiff(activeOrders[webNum]?.docDate) // время активности
    }


}