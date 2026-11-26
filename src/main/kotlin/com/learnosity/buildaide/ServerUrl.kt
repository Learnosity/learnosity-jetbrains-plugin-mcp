package com.learnosity.buildaide

import java.net.URI

const val MCP_PATH = "/build/build-aide"

/** Whether [value] is a Build Aide MCP URL on any region. */
fun isServerUrl(value: String?): Boolean {
    val uri = value?.let { runCatching { URI(it) }.getOrNull() } ?: return false
    val origin = "${uri.scheme}://${uri.rawAuthority}"
    return Region.entries.any { it.origin == origin } && uri.rawPath == MCP_PATH
}
