package orderProcessing

import android.content.SharedPreferences
import android.util.Log
import bot.TGInfoMessage
import bot.TGbot
import com.github.klimatov.webordermonitor.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import orderProcessing.data.Items
import orderProcessing.data.RemainsLocal
import orderProcessing.data.SecurityData
import orderProcessing.data.WebOrder
import orderProcessing.net.NetClient
import org.threeten.bp.LocalDateTime

object OrderDaemon {
    private val login = SecurityData.TS_LOGIN
    private val password = SecurityData.TS_PASSWORD
    private val werk = SecurityData.TS_SHOP
    val netClient = NetClient()
    private val processing = Processing()
    val appStartTime: LocalDateTime = LocalDateTime.now()
    var loginTime: LocalDateTime = LocalDateTime.now()

    suspend fun orderDaemonStart(
        binding: ActivityMainBinding,
        sharedPreferences: SharedPreferences
    ) {

        withContext(Dispatchers.Main) {
            binding.textProcess.text = "Запускаем..."
        }

        // считываем данные из SharedPerferences
        val serializedActiveOrders = sharedPreferences.getString("ACTIVE_ORDERS", null)
        if (serializedActiveOrders != null) {
            val type = object : TypeToken<MutableMap<String?, WebOrder?>>() {}.type
            processing.activeOrders =
                Gson().fromJson(serializedActiveOrders, type)
            Log.i("webOrderMonitor", "sharedPreferences activeOrders READ: $serializedActiveOrders")

//            val maxVal = emptyList<Long>().toMutableList()
//            processing.activeOrders.forEach { maxVal.add(it.value?.messageId!!) }
//            TGInfoMessage.newInfoMsgId =
//                maxVal.maxOrNull()// messageID последнего сообщения для инфокнопки
        }

        val currentInfoMsgId = sharedPreferences.getString("CURRENT_INFO_MESSAGE_ID", null)
        if (currentInfoMsgId != null) {
            TGInfoMessage.currentInfoMsgId = currentInfoMsgId.toLong()
            TGInfoMessage.newInfoMsgId = TGInfoMessage.currentInfoMsgId
            Log.i("webOrderMonitor", "sharedPreferences currentInfoMsgId READ: $currentInfoMsgId")
        }

        val dayConfirmedCount = sharedPreferences.getString("DAY_CONFIRMED_COUNT", null)
        if (dayConfirmedCount != null) {
            TGbot.dayConfirmedCount = dayConfirmedCount.toInt()
            Log.i("webOrderMonitor", "sharedPreferences dayConfirmedCount READ: $dayConfirmedCount")
        }

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            login()
            while (true) {  // основной цикл проверки
                withContext(Dispatchers.Main) {
                    binding.textProcess.text = "Обновляем данные..."
                }

                // проверяем открыт ли магазин, триггерим звук уведомлений
                if (TGbot.msgConvert.shopInWork()!=TGbot.msgNotification) {
                    TGbot.msgNotification = !TGbot.msgNotification
                    TGbot.botSendInfoMessage()
                    // если магазин закрылся, то сбрасываем счетчик собранных за день и записываем в настройки
                    if (!TGbot.msgNotification) {
                        TGbot.dayConfirmedCount = 0
                        val serializedDayConfirmedCount = Gson().toJson(TGbot.dayConfirmedCount)
                        sharedPreferences.edit()
                            .putString("DAY_CONFIRMED_COUNT", serializedDayConfirmedCount).apply()
                        Log.i(
                            "webOrderMonitor",
                            "sharedPreferences dayConfirmedCount SAVE: ${TGbot.dayConfirmedCount}"
                        )
                    }
                }
                Log.d("webOrderMonitor", "Shop open: ${TGbot.msgConvert.shopInWork()}")

                val orderList =
                    netClient.getWebOrderListSimple("new") //all or new  --- получаем список неподтвержденных

                when (netClient.errorCode) {
                    200 -> processing.processInworkOrders(orderList, binding, sharedPreferences)
                    401 -> login()
                    else -> Log.e("webOrderMonitor", "Error ${netClient.errorCode} ${netClient.error}")
                }

                withContext(Dispatchers.Main) {
                    binding.textError.text = "Код ответа сервера: ${netClient.errorCode.toString()}"
                    binding.textProcess.text = "Ждем следующего обновления..."
                    binding.textDBversion.text = "Версия базы на сервере: ${netClient.remoteDbVersion}"
                }

                Log.i("webOrderMonitor", "Wait next iteration 30 second")
                delay(30000L)
            }
        }.join()
    }

    suspend fun login() {
            if (netClient.login(login, password, werk)) {
                Log.i("webOrderMonitor", "Connected to base ${netClient.userInfo}")
                loginTime = LocalDateTime.now()
                netClient.getDBVersion()
            } else {
                Log.e("webOrderMonitor", "Login failed with Error: ${netClient.error}")
                //exitProcess(999)
            }
    }

    suspend fun getItems(orderId: String?): List<Items> {
        val orderItems = netClient.getWebOrderDetail(orderId, "WRQST")
        if (orderItems == null) return emptyList() else return orderItems.items
    }

    suspend fun getRemains(goodCode: String?): List<RemainsLocal> {
        val remains = netClient.localRemains(goodCode)
        if (remains == null) return emptyList() else return remains
    }

    suspend fun getOrderList(webNum: String?): List<WebOrder> {
        val orderList = netClient.getWebOrderList("new", webNum)
        if (orderList == null) return emptyList() else return orderList.webOrders
    }
}