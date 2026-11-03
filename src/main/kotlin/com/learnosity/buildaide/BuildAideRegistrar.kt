package com.learnosity.buildaide

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import java.io.IOException

private const val SERVER_NAME = "learnosity-build-aide"
private const val SERVER_URL = "https://mcp.learnosity.com/build/build-aide"
private const val ADDED = "added"
private const val ALREADY_PRESENT = "already-present"
private const val NOTIFICATION_GROUP = "Learnosity Build Aide"
private const val TITLE = "Learnosity Build Aide MCP"
private const val MCP_SETTINGS_NAME = "Model Context Protocol (MCP)"
private const val SAMPLE_PROMPT =
    "Using the Learnosity Build Aide MCP, how do I sign an Items API request in Node.js?"

/** Adds the server once to each AI client in use, and removes it on uninstall where it added it. */
@Service
class BuildAideRegistrar {

    private val properties get() = PropertiesComponent.getInstance()

    @Synchronized
    fun registerOnce(project: Project) {
        val added = McpClient.entries
            .filter { properties.getValue(it.stateKey) == null && it.isInUse() }
            .filter { register(project, it) }
        if (added.isNotEmpty()) notifyAdded(project, added)
    }

    @Synchronized
    fun unregister() {
        McpClient.entries.forEach(::unregister)
    }

    private fun register(project: Project, client: McpClient): Boolean {
        val added = try {
            client.mcpJson.addRemoteServer(SERVER_NAME, SERVER_URL)
        } catch (e: InvalidMcpJsonException) {
            reportFailure(project, client, e.message.orEmpty())
            return false
        } catch (e: IOException) {
            reportFailure(project, client, "Could not write the ${client.displayName} MCP configuration: ${e.message}")
            return false
        }

        properties.setValue(client.stateKey, if (added) ADDED else ALREADY_PRESENT)
        return added
    }

    private fun unregister(client: McpClient) {
        val addedByUs = properties.getValue(client.stateKey) == ADDED
        properties.unsetValue(client.stateKey)
        if (!addedByUs) return

        try {
            client.mcpJson.removeServer(SERVER_NAME)
        } catch (e: InvalidMcpJsonException) {
            thisLogger().warn("Left $SERVER_NAME in ${client.displayName}: ${e.message}")
        } catch (e: IOException) {
            thisLogger().warn("Could not remove $SERVER_NAME from ${client.displayName}: ${e.message}")
        }
    }

    private fun reportFailure(project: Project, client: McpClient, reason: String) {
        thisLogger().warn(reason)
        createNotification(
            "${StringUtil.escapeXmlEntities(reason)}. Add the server in ${client.settingsPath} with the URL $SERVER_URL.",
            NotificationType.WARNING,
        ).notify(project)
    }

    private fun notifyAdded(project: Project, clients: List<McpClient>) {
        val names = clients.joinToString(" and ") { it.displayName }
        if (McpClient.AI_ASSISTANT !in clients) {
            return createNotification("Added to $names. Ask, for example: <i>$SAMPLE_PROMPT</i>", NotificationType.INFORMATION)
                .notify(project)
        }

        createNotification(
            "Added to $names. Enable it in AI Assistant's MCP settings, then ask, for example: <i>$SAMPLE_PROMPT</i>",
            NotificationType.INFORMATION,
        )
            .addAction(NotificationAction.createSimpleExpiring("Open MCP settings") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, MCP_SETTINGS_NAME)
            })
            .notify(project)
    }

    private fun createNotification(content: String, type: NotificationType) =
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(TITLE, content, type)
}
