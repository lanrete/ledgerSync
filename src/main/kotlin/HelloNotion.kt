import khttp.get
import khttp.post
import org.json.JSONObject
import java.io.FileOutputStream

object HelloNotion {

    private val ledgerDatabase = Config.ledgerDatabase

    private const val databasePrefix = Config.databasePrefix
    private const val pagePrefix = Config.pagePrefix
    private val headerOption = Config.headerOption

    private val accountMap = mutableMapOf<String, String>()

    fun getTransactions(incremental: Boolean, pageSize: Int): List<Transaction> {
        val filterOption = mapOf(
            "property" to "Updated",
            "checkbox" to mapOf("equals" to false)
        )
        val sortOption = listOf(
            mapOf(
                "timestamp" to "created_time",
                "direction" to "ascending"
            )
        )
        val dataMap = mutableMapOf(
            "sorts" to sortOption,
            "page_size" to pageSize,
            "start_cursor" to null,
        )
        if (incremental) {
            dataMap["filter"] = filterOption
        }
        val transactionList = mutableListOf<Transaction>()

        do {
            val postR = post(
                url = "$databasePrefix/$ledgerDatabase/query",
                headers = headerOption,
                data = JSONObject(dataMap).toString(),
            )

            val jsonResponse = postR.jsonObject

            // TODO Add logging function
            // println(jsonResponse)
            val result = jsonResponse.getJSONArray("results")
            val fetchedTransactions = result.map {
                val obj = JSONObject(it.toString())
                val transactionID = obj.getString("id")
                val properties = obj.getJSONObject("properties")
                val amount = properties.getJSONObject("Amount").getDouble("number")
                val currency = properties.getJSONObject("Currency").getJSONObject("select").getString("name")
                val eventDate = properties.getJSONObject("Event Date").getJSONObject("formula").getString("string")
                val from = properties.getJSONObject("From").getJSONArray("relation").getJSONObject(0).getString("id")
                val to = properties.getJSONObject("To").getJSONArray("relation").getJSONObject(0).getString("id")
                val title =
                    properties.getJSONObject("Name").getJSONArray("title").getJSONObject(0).getString("plain_text")

                Transaction(
                    ID = transactionID,
                    title = title,
                    currency = currency,
                    amount = amount,
                    fromAccountID = getAccount(from),
                    toAccountID = getAccount(to),
                    eventDate = eventDate
                )
            }
            transactionList.addAll(fetchedTransactions)
            val hasMore = jsonResponse.getBoolean("has_more")
            if (hasMore) {
                val nextCursor = jsonResponse.getString("next_cursor")
                dataMap["start_cursor"] = nextCursor
            }
        } while (hasMore)

        return transactionList
    }

    private fun getAccount(accountID: String): String {
        if (accountMap.containsKey(accountID)) {
            return accountMap.getValue(accountID)
        }
        val getR = get(
            url = "$pagePrefix/$accountID",
            headers = headerOption
        )

        val property = getR.jsonObject.getJSONObject("properties")
        val accountName = property
            .getJSONObject("Actual Account")
            .getJSONArray("rich_text")
            .getJSONObject(0)
            .getString("plain_text")

        accountMap[accountID] = accountName
        return accountName
    }
}

fun main(args: Array<String>) {
    val incremental = args[0].toBoolean()
    val update = args[1].toBoolean()
    val pageSize = args[2].toInt()
    HelloNotion.getTransactions(incremental = incremental, pageSize = pageSize).forEach {
        // println(it)
        // println()
        val ledger = it.generateLedger()
        FileOutputStream("test.ledger", true).bufferedWriter().use { out ->
            out.appendLine(ledger)
        }
        // println(it.generateLedger())
        if (update) {
            it.updateStatus()
        }
        // println("-------------------")
    }
}