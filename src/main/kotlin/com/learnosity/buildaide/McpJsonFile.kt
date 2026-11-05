package com.learnosity.buildaide

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

private const val SERVERS_KEY = "mcpServers"
private const val MAX_BYTES = 1L shl 20
private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

class InvalidMcpJsonException(path: Path, cause: Throwable? = null) :
    Exception("$path is not a valid MCP configuration", cause)

/** An `mcp.json` holding `{"mcpServers": {...}}`, edited without disturbing other entries. */
class McpJsonFile(private val path: Path) {

    /** Adds a remote server unless one of that name exists; returns whether the file changed. */
    fun addRemoteServer(name: String, url: String): Boolean {
        val root = read()
        val servers = serversOf(root)
        if (servers.has(name)) return false

        servers.add(name, JsonObject().apply { addProperty("url", url) })
        write(root)
        return true
    }

    /** Removes a server if present; returns whether the file changed. */
    fun removeServer(name: String): Boolean {
        val root = read()
        if (serversOf(root).remove(name) == null) return false

        write(root)
        return true
    }

    private fun serversOf(root: JsonObject): JsonObject {
        val servers = root.get(SERVERS_KEY) ?: JsonObject().also { root.add(SERVERS_KEY, it) }
        if (!servers.isJsonObject) throw InvalidMcpJsonException(path)
        return servers.asJsonObject
    }

    private fun read(): JsonObject {
        if (Files.notExists(path)) return JsonObject()
        if (Files.size(path) > MAX_BYTES) throw InvalidMcpJsonException(path)

        val text = Files.readString(path)
        if (text.isBlank()) return JsonObject()

        val root = try {
            JsonParser.parseString(text)
        } catch (e: JsonParseException) {
            throw InvalidMcpJsonException(path, e)
        }
        if (!root.isJsonObject) throw InvalidMcpJsonException(path)
        return root.asJsonObject
    }

    /** Fails before any write when the file, or the nearest existing directory above it, is not writable. */
    private fun requireWritable() {
        val directory = generateSequence(path.parent) { it.parent }.firstOrNull(Files::isDirectory)
            ?: throw AccessDeniedException(path.toString(), null, "no existing parent directory")
        val blocked = listOf(path, directory).firstOrNull { Files.exists(it) && !Files.isWritable(it) } ?: return
        throw AccessDeniedException(blocked.toString(), null, "not writable")
    }

    private fun write(root: JsonObject) {
        requireWritable()
        Files.createDirectories(path.parent)
        val temp = Files.createTempFile(path.parent, "mcp", ".json")
        Files.writeString(temp, GSON.toJson(root) + "\n")
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
