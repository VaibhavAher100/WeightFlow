package com.weightflow.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringCatalogParityTest {

    private val placeholderRegex = Regex("""%\d+\$[a-zA-Z]""")

    private fun entries(path: String): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path))
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).associate {
            val node = nodes.item(it)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    private fun keys(path: String): Set<String> = entries(path).keys

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

    @Test
    fun `positional placeholders match between english and german`() {
        // A mismatched %1$s / %2$d set between locales crashes String.format at runtime.
        val en = entries("src/main/res/values/strings.xml")
        val de = entries("src/main/res/values-de/strings.xml")
        val mismatches = en.keys.filter { key ->
            val enArgs = placeholderRegex.findAll(en.getValue(key)).map { it.value }.toSortedSet()
            val deArgs = placeholderRegex.findAll(de[key].orEmpty()).map { it.value }.toSortedSet()
            enArgs != deArgs
        }
        assertTrue("Placeholder arity mismatch in keys: $mismatches", mismatches.isEmpty())
    }
}
