package com.weightflow.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringCatalogParityTest {

    private fun keys(path: String): Set<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path))
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length)
            .map { nodes.item(it).attributes.getNamedItem("name").nodeValue }
            .toSet()
    }

    @Test
    fun `english and german catalogs have identical keys`() {
        val en = keys("src/main/res/values/strings.xml")
        val de = keys("src/main/res/values-de/strings.xml")
        val missingInDe = en - de
        val extraInDe = de - en
        assertTrue("Keys missing in German: $missingInDe", missingInDe.isEmpty())
        assertTrue("Extra keys in German: $extraInDe", extraInDe.isEmpty())
        assertEquals(en, de)
    }
}
