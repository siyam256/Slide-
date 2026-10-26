package com.example.model

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

/**
 * Represents a user-selected input document (PDF or converted image) in the file staging screen.
 */
data class SelectedDocument(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val uri: Uri? = null,
    val file: File,
    val isPdf: Boolean = true,
    val pageCount: Int = 1,
    val thumbnail: Bitmap? = null
)

/**
 * Represents an individual slide from the imported PDF.
 */
data class SlideItem(
    val index: Int,
    val pageNumber: Int,
    val isSelected: Boolean = true,
    val thumbnail: Bitmap? = null,
    val isDarkSlide: Boolean = false,
    val averageLuminance: Int = 255
)

/**
 * Orientation for the output PDF sheet.
 */
enum class PageOrientation(val label: String) {
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

/**
 * Presets for standard N-up slide layouts.
 */
enum class PageLayoutPreset(
    val title: String,
    val subtitle: String,
    val columns: Int,
    val rows: Int,
    val defaultOrientation: PageOrientation,
    val hasNotesLines: Boolean = false
) {
    ONE_UP(
        title = "1-Up Full Page",
        subtitle = "1 slide per page, centered",
        columns = 1,
        rows = 1,
        defaultOrientation = PageOrientation.LANDSCAPE
    ),
    TWO_UP_VERTICAL(
        title = "2-Up Stacked (1×2)",
        subtitle = "2 slides stacked vertically in center",
        columns = 1,
        rows = 2,
        defaultOrientation = PageOrientation.PORTRAIT
    ),
    THREE_UP_VERTICAL(
        title = "3-Up Stacked (1×3)",
        subtitle = "3 slides stacked vertically in center",
        columns = 1,
        rows = 3,
        defaultOrientation = PageOrientation.PORTRAIT,
        hasNotesLines = false
    ),
    FOUR_UP_GRID(
        title = "4-Up Grid (2×2)",
        subtitle = "Most popular for study handouts",
        columns = 2,
        rows = 2,
        defaultOrientation = PageOrientation.PORTRAIT
    ),
    SIX_UP_GRID(
        title = "6-Up Compact (2×3)",
        subtitle = "6 slides per sheet, great paper saver",
        columns = 2,
        rows = 3,
        defaultOrientation = PageOrientation.PORTRAIT
    ),
    NINE_UP_GRID(
        title = "9-Up Cheat Sheet (3×3)",
        subtitle = "9 slides per sheet for quick overview",
        columns = 3,
        rows = 3,
        defaultOrientation = PageOrientation.PORTRAIT
    ),
    THREE_UP_NOTES(
        title = "3-Up Handout with Notes",
        subtitle = "3 slides with side ruled lines for note taking",
        columns = 1,
        rows = 3,
        defaultOrientation = PageOrientation.PORTRAIT,
        hasNotesLines = true
    ),
    CUSTOM(
        title = "Custom Grid",
        subtitle = "Customize columns, rows and orientation",
        columns = 2,
        rows = 2,
        defaultOrientation = PageOrientation.PORTRAIT
    )
}

/**
 * Color and ink processing mode.
 */
enum class ColorPrintMode(val title: String, val description: String) {
    COLOR(
        title = "Original Color",
        description = "Preserves all presentation colors as in original slides"
    ),
    GRAYSCALE(
        title = "Black & White (Grayscale)",
        description = "Converts all slides to standard printer grayscale"
    ),
    MAGIC_PRINT(
        title = "Magic Print (Smart Toner Saver)",
        description = "Inverts dark/black slides to clean white background with crisp dark text to save ink and look stunning"
    )
}

/**
 * Complete layout configuration selected by the user.
 */
data class LayoutConfig(
    val preset: PageLayoutPreset = PageLayoutPreset.FOUR_UP_GRID,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val columns: Int = 2,
    val rows: Int = 2,
    val drawSlideBorders: Boolean = true,
    val drawSlideNumbers: Boolean = true,
    val drawNotesLines: Boolean = false,
    val marginPaddingDp: Int = 18,
    val colorMode: ColorPrintMode = ColorPrintMode.MAGIC_PRINT
) {
    val slidesPerPage: Int
        get() = if (drawNotesLines) 3 else columns * rows
}

/**
 * Wizard step navigation enum.
 */
enum class AppStep(val title: String) {
    SOURCE("Choose PDF"),
    FILES("Select PDFs"),
    PAGES("Select Pages"),
    LAYOUT("Layout & Grid"),
    COLOR("Print Mode"),
    EXPORT("Preview & Export")
}
