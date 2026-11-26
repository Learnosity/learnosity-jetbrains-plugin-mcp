package com.learnosity.buildaide

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val NAME = "learnosity-build-aide"
private const val EU_URL = "https://mcp-eu.learnosity.com/build/build-aide"

class McpJsonFileTest {
    private fun configFile(content: String): Path =
        Files.createTempDirectory("mcp-json").resolve("mcp.json").also { it.writeText(content) }

    @Test
    fun `moves an entry still on a Build Aide URL, keeping its other keys`() {
        val file = configFile("""{"mcpServers":{"$NAME":{"url":"https://mcp.learnosity.com/build/build-aide","disabled":false}}}""")

        assertTrue(McpJsonFile(file).updateRemoteServerUrl(NAME, EU_URL, ::isServerUrl))
        assertEquals(
            """{"mcpServers":{"$NAME":{"url":"$EU_URL","disabled":false}}}""",
            file.readText().replace(Regex("\\s"), ""),
        )
    }

    @Test
    fun `leaves an entry pointed elsewhere, already current or missing`() {
        listOf(
            """{"mcpServers":{"$NAME":{"url":"https://example.com/mcp"}}}""",
            """{"mcpServers":{"$NAME":{"url":"$EU_URL"}}}""",
            """{"mcpServers":{}}""",
        ).forEach { content ->
            val file = configFile(content)
            assertFalse(McpJsonFile(file).updateRemoteServerUrl(NAME, EU_URL, ::isServerUrl))
            assertEquals(content, file.readText())
        }
    }
}
