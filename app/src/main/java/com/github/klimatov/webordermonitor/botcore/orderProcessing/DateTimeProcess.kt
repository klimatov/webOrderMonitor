import com.soywiz.klock.DateTime
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class DateTimeProcess {
    fun dateFrom() : String {
        val result = LocalDate.now().minusDays(14)
        return result.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    fun dateFormat(docDate: String): LocalDateTime {
        val docDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        return LocalDateTime.parse(docDate, docDateFormat)
    }
}