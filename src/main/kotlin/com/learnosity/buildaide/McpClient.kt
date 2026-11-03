package com.learnosity.buildaide

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import java.nio.file.Files
import java.nio.file.Path

/** A JetBrains AI client that reads MCP servers from a user-level `~/<configDir>/mcp/mcp.json`. */
enum class McpClient(
    val displayName: String,
    val settingsPath: String,
    val stateKey: String,
    private val pluginId: String,
    private val configDir: String,
) {
    AI_ASSISTANT(
        "AI Assistant",
        "Settings | Tools | AI Assistant | Model Context Protocol (MCP)",
        "learnosity.buildAide.registration",
        "com.intellij.ml.llm",
        ".ai",
    ),
    JUNIE(
        "Junie",
        "Settings | Tools | Junie | MCP Settings",
        "learnosity.buildAide.registration.junie",
        "org.jetbrains.junie",
        ".junie",
    );

    val mcpJson get() = McpJsonFile(configRoot().resolve("mcp").resolve("mcp.json"))

    /** Its plugin is enabled or its config directory exists. */
    fun isInUse(): Boolean =
        PluginManagerCore.isLoaded(PluginId.getId(pluginId)) || Files.isDirectory(configRoot())

    private fun configRoot(): Path = Path.of(System.getProperty("user.home"), configDir)
}
