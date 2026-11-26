package com.learnosity.buildaide

import com.intellij.ide.util.PropertiesComponent

private const val REGION_KEY = "learnosity.buildAide.region"

/** The regional Build Aide servers a user can choose, each served from its own host. */
enum class Region(val label: String, val origin: String) {
    AMERICA("America", "https://mcp.learnosity.com"),
    EUROPE("Europe", "https://mcp-eu.learnosity.com"),
    AUSTRALIA("Australia", "https://mcp-au.learnosity.com");

    val serverUrl get() = "$origin$MCP_PATH"

    companion object {
        fun of(name: String?): Region = entries.firstOrNull { it.name == name } ?: AMERICA
    }
}

/** The chosen region, kept in the IDE's application properties. */
object RegionSettings {
    private val properties get() = PropertiesComponent.getInstance()

    var region: Region
        get() = Region.of(properties.getValue(REGION_KEY))
        set(value) = properties.setValue(REGION_KEY, value.name, Region.AMERICA.name)
}
