import khttp.patch
import org.json.JSONObject

data class Transaction(
    private val ID: String,
    private val title: String,
    private val currency: String,
    private val fromAccountID: String,
    private val toAccountID: String,
    private val amount: Double,
    private val eventDate: String
) {
    private val headerOption = Config.headerOption
    private val pagePrefix = Config.pagePrefix

    override fun toString(): String {
        return "Spent $amount $currency from $fromAccountID to $toAccountID on $eventDate for $title"
    }

    fun generateLedger(): String {
        val sb = StringBuilder()
        sb.append("$eventDate * $title\n")
        sb.append("  $toAccountID  $currency $amount\n")
        sb.append("  $fromAccountID\n")
        return sb.toString()
    }

    fun updateStatus() {
        val transactionID = ID
        println("Updating the status of $ID")
        val updateMap = mapOf("properties" to mapOf("Updated" to mapOf("checkbox" to true)))
        val patchR = patch(
            url = "$pagePrefix/$transactionID",
            headers = headerOption,
            data = JSONObject(updateMap).toString()
        )
        if (patchR.statusCode == 200) {
            println("Update Completed")
        }
    }
}
