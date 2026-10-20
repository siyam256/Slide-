package com.example

import com.example.model.ColorPrintMode
import com.example.model.LayoutConfig
import com.example.model.PageOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testManualGridSlidesPerPage() {
        val config1x3 = LayoutConfig(
            columns = 1,
            rows = 3,
            orientation = PageOrientation.PORTRAIT
        )
        assertEquals(3, config1x3.slidesPerPage)

        val config2x2 = LayoutConfig(
            columns = 2,
            rows = 2,
            orientation = PageOrientation.PORTRAIT
        )
        assertEquals(4, config2x2.slidesPerPage)

        val config2x3 = LayoutConfig(
            columns = 2,
            rows = 3,
            orientation = PageOrientation.PORTRAIT
        )
        assertEquals(6, config2x3.slidesPerPage)

        val config3x3 = LayoutConfig(
            columns = 3,
            rows = 3,
            orientation = PageOrientation.LANDSCAPE
        )
        assertEquals(9, config3x3.slidesPerPage)
    }

    @Test
    fun testSheetsCountDerivation() {
        val slidesCount = 10
        val slidesPerPage = 4
        val totalSheets = (slidesCount + slidesPerPage - 1) / slidesPerPage
        assertEquals(3, totalSheets)

        val slidesCountExact = 8
        val totalSheetsExact = (slidesCountExact + slidesPerPage - 1) / slidesPerPage
        assertEquals(2, totalSheetsExact)

        val nineSlidesInThreeGrid = (9 + 3 - 1) / 3
        assertEquals(3, nineSlidesInThreeGrid)
    }

    @Test
    fun testColorPrintModes() {
        val magic = ColorPrintMode.MAGIC_PRINT
        assertTrue(magic.title.contains("Magic Print"))

        val bw = ColorPrintMode.GRAYSCALE
        assertTrue(bw.title.contains("Grayscale"))

        val color = ColorPrintMode.COLOR
        assertTrue(color.title.contains("Color"))
    }
}
