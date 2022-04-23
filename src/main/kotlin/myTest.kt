import bot.TGbot
import orderProcessing.BotMessage
import orderProcessing.data.*

suspend fun myTest() {
    var msg = BotMessage()
    val testMsg: WebOrder = WebOrder(
        orderId = "6403776",
        ordType = "Самовывоз ЛС",
        isLegalEntity = "N",
        fioCustomer = "Климатов Павел",
        paid = "N",
        phone = "79620672745",
        orderType = "WRQST",
        reasonCode = null,
        collector = Collector(username = "dmn", hrCode = 0),
        docDate = "19.04.2022 13:32:02",
        docSum = 35,
        docStatus = "WRQST_CRTD",
        webNum = "278641382",
        items = listOf(
            Items(
                goodCode = "71587764",
                name = "Робот-пылесос Hyundai H-VCRQ90",
                amount = 14999,
                quantity = 1,
                itemNo = 10,
                barCode = "5050370323037",
                kit = null,
                routeIsNeeded = "Y",
                eshopUrl = "www.eldorado.ru/cat/detail/71587764/?utm_a=A181",
                itemId = 14891880,
                parentiTemNo = null,
                shelf = null,
                params = listOf(
                    Params(
                        ogItemId = 14897426,
                        fieldValue = null,
                        fieldDdescr = "Серийный #",
                        fieldMask = null,
                        fieldNeeded = null,
                        fieldName = "SERIALNUM"
                    )
                ),
                incomplet = null,
                remains = listOf(
                    RemainsLocal(
                        storageKind = "0090",
                        quantity = 3,
                        goodCode = "71587764",
                        priority = 3
                    ),
                    RemainsLocal(
                        storageKind = "0333",
                        quantity = 5,
                        goodCode = "71587764",
                        priority = 3
                    )
                )
            ), Items(
                goodCode = "71587764",
                name = "Робот-пылесос Hyundai H-VCRQ90",
                amount = 14999,
                quantity = 1,
                itemNo = 10,
                barCode = "5050370323037",
                kit = null,
                routeIsNeeded = "Y",
                eshopUrl = "www.eldorado.ru/cat/detail/71587764/?utm_a=A181",
                itemId = 14891880,
                parentiTemNo = null,
                shelf = null,
                params = listOf(
                    Params(
                        ogItemId = 14897426,
                        fieldValue = null,
                        fieldDdescr = "Серийный #",
                        fieldMask = null,
                        fieldNeeded = null,
                        fieldName = "SERIALNUM"
                    )
                ),
                incomplet = null,
                remains = listOf(
                    RemainsLocal(
                        storageKind = "0090",
                        quantity = 3,
                        goodCode = "71587764",
                        priority = 3
                    ),
                    RemainsLocal(
                        storageKind = "0333",
                        quantity = 5,
                        goodCode = "71587764",
                        priority = 3
                    )
                )
            )
        )
    )
//    println(msg.orderMessage(testMsg))
    TGbot.botSendMessage(testMsg)


}