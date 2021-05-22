object Config {
    private val secret: String = System.getenv("Notion_Secret")
    private val notionVersion: String = System.getenv("Notion_Version")
    val ledgerDatabase: String = System.getenv("Notion_Ledger")

    const val databasePrefix = "https://api.notion.com/v1/databases"
    const val pagePrefix = "https://api.notion.com/v1/pages"
    val headerOption = mapOf(
        "Authorization" to "Bearer $secret",
        "Notion-Version" to notionVersion,
        "Content-Type" to "application/json",
    )
}