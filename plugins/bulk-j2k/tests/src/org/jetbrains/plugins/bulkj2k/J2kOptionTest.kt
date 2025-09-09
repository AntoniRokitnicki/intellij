package org.jetbrains.plugins.bulkj2k

import org.jetbrains.kotlin.j2k.ConverterSettings
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Basic tests for [J2kOption] reflection helpers.
 */
class J2kOptionTest {
    @Test
    fun `available includes basic mode and applies setting`() {
        val available = J2kOption.available()
        assertTrue(J2kOption.BASIC_MODE in available)

        val settings = ConverterSettings.defaultSettings.copy()
        J2kOption.apply(settings, mapOf(J2kOption.BASIC_MODE to true))
        assertTrue(settings.basicMode)
    }
}
