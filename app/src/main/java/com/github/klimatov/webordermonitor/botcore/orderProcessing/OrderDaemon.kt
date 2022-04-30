package orderProcessing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    suspend fun orderDaemonStart() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (netClient.login(login, password, werk)) {
                println("Connected to base ${netClient.userInfo}")
            } else {
                println(netClient.error)
                exitProcess(999)
            }
        }.join()

        scope.launch {
            while (true) {  // основной цикл проверки
                val orderList = netClient.getWebOrderListSimple("new") //all or new  --- получаем список неподтвержденных
                if (orderList != null) {
                    processing.processInworkOrders(orderList)   // --- обрабатываем список неподтвержденных вебок
                }
                println("tick")
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
        if (orderList==null) return emptyList() else return orderList.webOrders
    }
}