package com.learnosity.buildaide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerUrlTest {
    @Test
    fun `each region has its own URL, with no parameters`() {
        assertEquals("https://mcp.learnosity.com/build/build-aide", Region.AMERICA.serverUrl)
        assertEquals("https://mcp-eu.learnosity.com/build/build-aide", Region.EUROPE.serverUrl)
        assertEquals("https://mcp-au.learnosity.com/build/build-aide", Region.AUSTRALIA.serverUrl)
    }

    @Test
    fun `an unset or unknown region is America`() {
        assertEquals(Region.AMERICA, Region.of(null))
        assertEquals(Region.AMERICA, Region.of("mars"))
        assertEquals(Region.AMERICA, Region.of("au"))
        assertEquals(Region.AUSTRALIA, Region.of("AUSTRALIA"))
    }

    @Test
    fun `recognises a Build Aide URL on any region`() {
        Region.entries.forEach { assertTrue(isServerUrl(it.serverUrl), it.serverUrl) }
    }

    @Test
    fun `does not claim other servers or paths`() {
        listOf(
            "https://mcp.dev.learnosity.com/build/build-aide",
            "https://mcp.learnosity.com/build",
            "http://mcp.learnosity.com/build/build-aide",
            "https://example.com/build/build-aide",
            "not a url",
            null,
        ).forEach { assertFalse(isServerUrl(it), it.toString()) }
    }
}
