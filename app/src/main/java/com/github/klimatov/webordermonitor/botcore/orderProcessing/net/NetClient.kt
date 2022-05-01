package orderProcessing.net

import com.google.gson.Gson
import com.google.gson.JsonArray
import orderProcessing.data.*
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


class NetClient {
    //private var baseURL: String
    private var token = ""
    private var shop = ""
    lateinit var userInfo: UserInfo
    var error = ""
    val dbVersion = "16"
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault())
    val gmt = SimpleDateFormat("Z").format(calendar.time)
//    var timeZone = TimeZone.getTimeZone("GMT+07:00")
//    val gmt = timeZone.rawOffset.toString()

    //хэшилка пароля для запроса
    fun md5Hash(str: String): String {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        val bytes = str.toByteArray(Charsets.UTF_8)
        val bigInteger = BigInteger(1, instance.digest(bytes))
        val format = String.format("%032x", *Arrays.copyOf<Any>(arrayOf<Any>(bigInteger), 1))
        return format
    }

    fun login(login: String, password: String, werk: String): Boolean {
        this.shop = werk

        val values =
            hashMapOf("login" to login, "password" to md5Hash(password), "type" to "realme RMX2180", "version" to "11")
        try {
            val response = RetrofitInstance.eldoApi.login(werk, dbVersion, dbVersion, values)?.execute()


            if (response?.isSuccessful()!!) {
                val responseJson = Gson().fromJson(response.body(), Auth::class.java)
                val sb = StringBuilder()
                sb.append("Bearer ")
                sb.append(responseJson.token)
                this.token = sb.toString()
                this.userInfo = responseJson.userInfo!!
                return true
            } else {
                this.error = Gson().fromJson(response.errorBody()?.string(), Error::class.java).error.toString()
                return false
            }
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return false
        }
    }

    fun getDBVersion(): Int? {
        try {
            val response = RetrofitInstance.eldoApi.getDBVersion(shop)?.execute()
            val responseJson = Gson().fromJson(response?.body(), DbVersion::class.java)
            return responseJson.version!!
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return 0
        }
    }

    fun getWebOrderCount(str: String?): Int? {
        try {
            val response = RetrofitInstance.eldoApi.getWebOrderCount(
                shop,
                dbVersion,
                dbVersion,
                token,
                "01.01.2022",
                "01.01.2030",
                str // INWORK ASSEMBLY ISSUED
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), OrderCount::class.java)
            return responseJson.quantity!!
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return 0
        }
    }

    fun localRemains(id: String?): List<RemainsLocal> {
        try {
            val response = RetrofitInstance.eldoApi.localRemains(
                shop,
                dbVersion,
                dbVersion,
                token,
                id
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), Remains::class.java)
            return responseJson.remainsLocal
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return emptyList()
        }
    }

    fun localStorage(): List<LocalStorageDtos> {
        try {
            val response = RetrofitInstance.eldoApi.localStorage(
                shop,
                dbVersion,
                dbVersion,
                token
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), Storage::class.java)
            return responseJson.localStorageDtos
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return emptyList()
        }
    }

    fun mainRemains(jsonArray: JsonArray?): List<RemainsLocal> {
        val hashMap = hashMapOf("goodCodeList" to jsonArray)
        try {
            val response = RetrofitInstance.eldoApi.mainRemains(
                shop,
                dbVersion,
                dbVersion,
                token,
                hashMap
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), mainRemains::class.java)
            return responseJson.remains
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return emptyList()
        }
    }

    fun getWebOrderListSimple(selection: String): List<WebOrderSimply> {
        val str = when (selection) {
            "all" -> "WRQST_CRTD,PWRQT_DLVD,WRQST_ACPT"
            else -> "WRQST_CRTD"
        }
        val hashMap: HashMap<Any?, Any?> = hashMapOf(
            "orderType" to "WRQST",
            "dateFrom" to "18.03.2022",
            "dateTo" to "01.01.2030",
            "docStatus" to str,
            "skip" to 0,
            "limit" to 99
        )
        try {
            val response = RetrofitInstance.eldoApi.getWebOrderListSimple(
                shop,
                dbVersion,
                dbVersion,
                token,
                hashMap
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), ListWebOrderSimply::class.java)
            return responseJson.webOrderSimply
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return emptyList()
        }
    }

    fun getWebOrderList(selection: String, webNum: String?): ListWebOrder? {
        val str = when (selection) {
            "all" -> "WRQST_CRTD,PWRQT_DLVD,WRQST_ACPT"
            else -> "WRQST_CRTD"
        }
        val hashMap: HashMap<Any?, Any?> = hashMapOf(
            "orderType" to "WRQST",
            "dateFrom" to "18.03.2022",
            "dateTo" to "01.01.2030",
            "docStatus" to str,
            "skip" to 0,
            "phone" to "",
            "webNum" to webNum,
            "sort" to "asc",
            "limit" to 99
        )
        try {
            val response = RetrofitInstance.eldoApi.getWebOrderList(
                shop,
                dbVersion,
                dbVersion,
                gmt,
                token,
                hashMap
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), ListWebOrder::class.java)
            return responseJson
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return ListWebOrder()
        }
    }

    fun getWebOrderDetail(orderId: String?, type: String?): WebOrderDetail? {
        try {
            val response = RetrofitInstance.eldoApi.getWebOrderDetail(
                shop,
                dbVersion,
                dbVersion,
                token,
                orderId,
                type // WRQST ?
            )?.execute()
            val responseJson = Gson().fromJson(response?.body(), WebOrderDetail::class.java)
            return responseJson
        } catch (e: Exception) {
            println(e.message)
            this.error = e.message.toString()
            return WebOrderDetail()
        }
    }
}