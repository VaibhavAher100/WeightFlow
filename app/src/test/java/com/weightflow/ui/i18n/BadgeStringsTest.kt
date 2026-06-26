package com.weightflow.ui.i18n

import com.weightflow.domain.Badge
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeStringsTest {

    @Test
    fun `every badge has a name and description resource`() {
        Badge.entries.forEach { badge ->
            val res = BadgeStrings.resFor(badge)
            assertTrue("Missing name res for $badge", res.nameRes != 0)
            assertTrue("Missing desc res for $badge", res.descRes != 0)
        }
    }
}
