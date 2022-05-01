package orderProcessing

import android.content.SharedPreferences
import android.util.Log
import bot.TGInfoMessage
import com.github.klimatov.webordermonitor.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import orderProcessing.data.Items
import orderProcessing.data.RemainsLocal
import orderProcessing.data.SecurityData
import orderProcessing.data.WebOrder
import orderProcessing.net.NetClient
import kotlin.system.exitProcess

object OrderDaemon {
    private val login = SecurityData.TS_LOGIN
    private val password = SecurityData.TS_PASSWORD
    private val werk = SecurityData.TS_SHOP
    val netClient = NetClient()
    private val processing = Processing()

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

            Log.i("webOrderMonitor", "sharedPreferences READ: $serializedActiveOrders")

            val maxVal = emptyList<Long>().toMutableList()
            processing.activeOrders.forEach { maxVal.add(it.value?.messageId!!) }
            TGInfoMessage.newInfoMsgId =
                maxVal.maxOrNull()// messageID последнего сообщения для инфокнопки
        }


        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (netClient.login(login, password, werk)) {
                Log.i("webOrderMonitor", "Connected to base ${netClient.userInfo}")
            } else {
                Log.e("webOrderMonitor", "Error: ${netClient.error}")
                exitProcess(999)
            }
        }.join()

        scope.launch {
            while (true) {  // основной цикл проверки
                withContext(Dispatchers.Main) {
                    binding.textProcess.text = "Обновляем данные..."
                }

                val orderList =
                    netClient.getWebOrderListSimple("new") //all or new  --- получаем список неподтвержденных
                if (orderList != null) {
                    processing.processInworkOrders(
                        orderList,
                        binding,
                        sharedPreferences
                    )   // --- обрабатываем список неподтвержденных вебок
                }
                withContext(Dispatchers.Main) {
                    binding.textProcess.text = "Ждем следующего обновления..."
                }
                Log.i("webOrderMonitor", "Wait next iteration 30 second")
                delay(30000L)
            }
        }.join()
    }

    suspend fun getItems(orderId: String?): List<Items> {
        val orderItems = netClient.getWebOrderDetail(orderId, "WRQST")
        return orderItems!!.items
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