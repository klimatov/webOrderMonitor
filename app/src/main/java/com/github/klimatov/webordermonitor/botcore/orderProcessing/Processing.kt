package orderProcessing


import android.content.SharedPreferences
import android.util.Log
import bot.TGInfoMessage
import bot.TGbot
import com.github.klimatov.webordermonitor.databinding.ActivityMainBinding
import com.google.gson.Gson
import dev.inmo.tgbotapi.types.MessageIdentifier
import orderProcessing.data.WebOrder
import orderProcessing.data.WebOrderSimply


class Processing {
    var activeOrders: MutableMap<String?, WebOrder?> = mutableMapOf() //иниц. список активных вебок

    suspend fun processInworkOrders(
        inworkOrderList: List<WebOrderSimply>,
        binding: ActivityMainBinding,
        sharedPreferences: SharedPreferences
    ) {
        var newFlag = false
        // проверка, появились ли новые вебки, отсутствующие в списке активных?
        inworkOrderList.forEach { webOrder ->
            if (!activeOrders.containsKey(webOrder.webNum)) {
                val newOrder = OrderDaemon.getOrderList(webOrder.webNum)
                if (newOrder.isNotEmpty()) {
                    activeOrders[webOrder.webNum] =
                        newOrder[0]// добавляем новую вебку в список активных
                    TGbot.dayConfirmedCount++ // увеличиваем на 1 счетчик собранных за день
                    activeOrders[webOrder.webNum]?.items =
                        OrderDaemon.getItems(activeOrders[webOrder.webNum]?.orderId) // обновляем список товара (items)
                    activeOrders[webOrder.webNum]?.items?.forEach { items ->  // обновляем остатки по каждому товару
                        items.remains = OrderDaemon.getRemains(items.goodCode)
                    }
                    activeOrders[webOrder.webNum]?.activeTime =
                        TGbot.msgConvert.timeDiff(webOrder.docDate) // время активности
                    newOrder(webOrder.webNum)
                    newFlag = true
                }
            }
        }
        var msg = ""
        activeOrders.forEach { msg += " ${it.key}, ${it.value?.docDate} |" }
        Log.i("webOrderMonitor", "Active orders: (${activeOrders.count()} pc.) $msg")

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
        TGInfoMessage.updateInfoMsg(binding)

        // записываем активные в SharedPerferences если были изменения +-
        if (newFlag || delOrderList.count() > 0) {
            val serializedActiveOrders = Gson().toJson(activeOrders)
            sharedPreferences.edit().putString("ACTIVE_ORDERS", serializedActiveOrders).apply()

            sharedPreferences.edit()
                .putString("CURRENT_INFO_MESSAGE_ID", TGInfoMessage.currentInfoMsgId.toString()).apply()

            sharedPreferences.edit()
                .putString("DAY_CONFIRMED_COUNT", TGbot.dayConfirmedCount.toString()).apply()

            Log.i("webOrderMonitor", "sharedPreferences activeOrders SAVE: $serializedActiveOrders")
            Log.i("webOrderMonitor", "sharedPreferences currentInfoMsgId SAVE: ${TGInfoMessage.currentInfoMsgId}")
            Log.i("webOrderMonitor", "sharedPreferences dayConfirmedCount SAVE: ${TGbot.dayConfirmedCount}")
        }
    }

    suspend fun newOrder(webNum: String?) {
        val messageId: Long? = TGbot.botSendMessage(activeOrders[webNum])
        TGInfoMessage.newInfoMsgId = messageId // messageID последнего сообщения для инфокнопки
        activeOrders[webNum]?.messageId = messageId
    }

    suspend fun delOrder(webNum: String?) {
        TGbot.botConfirmMessage(activeOrders[webNum])
        activeOrders.remove(webNum)
    }

    suspend fun updateOrderTimer(webNum: String?) {
        TGbot.botTimerUpdate(activeOrders[webNum])
        activeOrders[webNum]?.activeTime =
            TGbot.msgConvert.timeDiff(activeOrders[webNum]?.docDate) // время активности
    }


}