package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.example.model.ColorPrintMode
import com.example.model.LayoutConfig
import com.example.model.PageLayoutPreset
import com.example.model.PageOrientation
import com.example.model.SlideItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object PdfEngine {

    // Standard A4 dimensions in PostScript points (72 points/inch)
    const val A4_WIDTH_POINTS = 595
    const val A4_HEIGHT_POINTS = 842

    /**
     * Creates a realistic 8-page lecture slide presentation PDF for instant testing.
     */
    suspend fun generateSamplePresentation(context: Context): File = withContext(Dispatchers.IO) {
        val sampleFile = File(context.cacheDir, "Sample_Lecture_Slides.pdf")
        if (sampleFile.exists()) {
            sampleFile.delete()
        }

        val pdfDocument = PdfDocument()
        val slideWidth = 960
        val slideHeight = 540 // 16:9 widescreen ratio

        val slidesData = listOf(
            SampleSlideData(
                title = "Deep Learning & Neural Networks",
                subtitle = "Lecture 04: Multi-Layer Perceptrons & Optimization",
                author = "Prof. S. Rahman • CS Department",
                isDark = true,
                bgColor = Color.rgb(20, 83, 45), // Deep emerald
                elements = listOf(
                    "• Feedforward Architecture",
                    "• Backpropagation Algorithm",
                    "• Gradient Descent & Learning Rates",
                    "• Regularization & Dropout"
                )
            ),
            SampleSlideData(
                title = "Lecture Outline & Objectives",
                subtitle = "What we will cover in this 90-minute session",
                author = "Slide 2 of 8",
                isDark = false,
                bgColor = Color.rgb(255, 255, 255),
                elements = listOf(
                    "1. Historical Context of Artificial Neurons",
                    "2. Activation Functions: Sigmoid, ReLU, GELU",
                    "3. Loss Landscapes & Convergence Bounds",
                    "4. Hands-on PyTorch Implementation"
                )
            ),
            SampleSlideData(
                title = "Dark Slide: Loss Computation",
                subtitle = "Categorical Cross-Entropy Formula",
                author = "Slide 3 of 8 (Dark Mode Slide)",
                isDark = true,
                bgColor = Color.rgb(15, 23, 42), // Pitch navy/black
                elements = listOf(
                    "Formula: L = - ∑ y_i * log(p_i)",
                    "Softmax: p_i = exp(z_i) / ∑ exp(z_j)",
                    "Warning: High toner consumption if printed in B&W!",
                    "Magic Print converts this background to crisp white!"
                )
            ),
            SampleSlideData(
                title = "Activation Function Benchmark",
                subtitle = "Empirical evaluation across 50 epochs",
                author = "Slide 4 of 8",
                isDark = false,
                bgColor = Color.rgb(250, 250, 250),
                elements = listOf(
                    "• ReLU: Fastest convergence, zero saturation on positive input",
                    "• LeakyReLU: Solves dying neuron dilemma",
                    "• GELU: Smooth stochastic gating, optimal for Transformers",
                    "• Swish: Beta-scaled self-gating"
                )
            ),
            SampleSlideData(
                title = "Key Principle: Overfitting",
                subtitle = "Memorization vs Generalization",
                author = "Slide 5 of 8 (High Contrast Dark Slide)",
                isDark = true,
                bgColor = Color.rgb(24, 24, 27), // Deep zinc charcoal
                elements = listOf(
                    "\"When a measure becomes a target, it ceases to be a good measure.\"",
                    "— Goodhart's Law",
                    "Solution: Heavy data augmentation and L2 weight decay",
                    "Test Accuracy: 94.8% on unseen benchmark holdout"
                )
            ),
            SampleSlideData(
                title = "Model Architecture Topology",
                subtitle = "Layer-by-Layer Dimension Flow",
                author = "Slide 6 of 8",
                isDark = false,
                bgColor = Color.rgb(255, 255, 255),
                elements = listOf(
                    "[Input: 784] -> [Dense: 512 + BatchNorm + ReLU]",
                    "[Dropout: 0.2] -> [Dense: 256 + ReLU]",
                    "[Dense: 64] -> [Output Softmax: 10 Classes]",
                    "Total Trainable Parameters: 549,386"
                )
            ),
            SampleSlideData(
                title = "Optimization Strategies",
                subtitle = "AdamW vs SGD with Nesterov Momentum",
                author = "Slide 7 of 8",
                isDark = false,
                bgColor = Color.rgb(245, 247, 246),
                elements = listOf(
                    "• Learning rate schedule: Cosine Annealing with Warmup",
                    "• Weight decay decoupled from gradient updates",
                    "• Gradient clipping: max_norm = 1.0",
                    "• Mixed Precision (FP16/BF16) enables 2.4x speedup"
                )
            ),
            SampleSlideData(
                title = "Summary & Problem Set 3",
                subtitle = "Due next Tuesday at 11:59 PM",
                author = "Slide 8 of 8",
                isDark = true,
                bgColor = Color.rgb(18, 53, 36),
                elements = listOf(
                    "✓ Implemented backprop from scratch",
                    "✓ Verified gradient calculation numerically",
                    "✓ Next Class: Convolutional Feature Extractors",
                    "Thank you! Questions and Office Hours at 4 PM"
                )
            )
        )

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 34f
            isFakeBoldText = true
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
        }
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        slidesData.forEachIndexed { index, slide ->
            val pageInfo = PdfDocument.PageInfo.Builder(slideWidth, slideHeight, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Fill Background
            canvas.drawColor(slide.bgColor)

            val textColor = if (slide.isDark) Color.WHITE else Color.rgb(20, 20, 20)
            val subColor = if (slide.isDark) Color.rgb(200, 230, 210) else Color.rgb(80, 80, 80)
            val accentColor = if (slide.isDark) Color.rgb(74, 222, 128) else Color.rgb(21, 128, 61)

            // Header Banner Bar
            boxPaint.color = accentColor
            boxPaint.style = Paint.Style.FILL
            canvas.drawRect(50f, 40f, 70f, 100f, boxPaint)

            // Title
            titlePaint.color = textColor
            canvas.drawText(slide.title, 85f, 75f, titlePaint)

            // Subtitle
            subPaint.color = subColor
            canvas.drawText(slide.subtitle, 85f, 105f, subPaint)

            // Horizontal separator
            boxPaint.color = if (slide.isDark) Color.rgb(50, 70, 60) else Color.rgb(220, 225, 220)
            boxPaint.style = Paint.Style.STROKE
            boxPaint.strokeWidth = 2f
            canvas.drawLine(50f, 130f, slideWidth - 50f, 130f, boxPaint)

            // Slide content box
            val contentBoxBg = if (slide.isDark) Color.argb(40, 255, 255, 255) else Color.argb(40, 21, 128, 61)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = contentBoxBg
            }
            canvas.drawRoundRect(RectF(50f, 155f, slideWidth - 50f, slideHeight - 75f), 16f, 16f, fillPaint)

            // Bullet points
            bodyPaint.color = textColor
            var yPos = 205f
            slide.elements.forEach { bullet ->
                canvas.drawText(bullet, 80f, yPos, bodyPaint)
                yPos += 55f
            }

            // Footer
            footerPaint.color = subColor
            canvas.drawText(slide.author, 50f, slideHeight - 35f, footerPaint)
            val pageNumStr = "Slide ${index + 1} of ${slidesData.size}"
            val pageNumWidth = footerPaint.measureText(pageNumStr)
            canvas.drawText(pageNumStr, slideWidth - 50f - pageNumWidth, slideHeight - 35f, footerPaint)

            pdfDocument.finishPage(page)
        }

        FileOutputStream(sampleFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        sampleFile
    }

    private data class SampleSlideData(
        val title: String,
        val subtitle: String,
        val author: String,
        val isDark: Boolean,
        val bgColor: Int,
        val elements: List<String>
    )

    /**
     * Extracts thumbnails and detects slide brightness for all pages of a PDF.
     */
    suspend fun extractSlideThumbnails(
        context: Context,
        pdfFile: File,
        onProgress: (Int, Int) -> Unit
    ): List<SlideItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SlideItem>()
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount

        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val thumbWidth = 360
            val aspectRatio = page.height.toFloat() / max(1, page.width)
            val thumbHeight = max(100, (thumbWidth * aspectRatio).toInt())

            val bitmap = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.ARGB_8888)
            // Render onto white background by default
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Analyze average luminance across grid samples
            val avgLuminance = calculateAverageLuminance(bitmap)
            val isDark = avgLuminance < 120

            result.add(
                SlideItem(
                    index = i,
                    pageNumber = i + 1,
                    isSelected = true,
                    thumbnail = bitmap,
                    isDarkSlide = isDark,
                    averageLuminance = avgLuminance
                )
            )
            onProgress(i + 1, pageCount)
        }

        renderer.close()
        pfd.close()
        result
    }

    private fun calculateAverageLuminance(bitmap: Bitmap): Int {
        var totalLum = 0L
        var count = 0
        val step = max(1, min(bitmap.width, bitmap.height) / 25)

        for (y in 0 until bitmap.height step step) {
            for (x in 0 until bitmap.width step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                totalLum += lum
                count++
            }
        }
        return if (count > 0) (totalLum / count).toInt() else 255
    }

    /**
     * Applies Color, Grayscale, or Magic Print (smart dark inversion) to a slide bitmap.
     */
    fun processBitmapColorMode(
        source: Bitmap,
        mode: ColorPrintMode,
        isDarkSlide: Boolean
    ): Bitmap {
        return when (mode) {
            ColorPrintMode.COLOR -> source

            ColorPrintMode.GRAYSCALE -> {
                val dest = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(dest)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                    val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
                    colorFilter = ColorMatrixColorFilter(grayMatrix)
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
                dest
            }

            ColorPrintMode.MAGIC_PRINT -> {
                val dest = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(dest)

                if (isDarkSlide) {
                    // Smart Invert: Dark background becomes white, light text becomes dark black!
                    // Invert matrix: -1 for RGB with +255 offset, then convert to grayscale for laser printer crispness
                    val invertMatrix = ColorMatrix(
                        floatArrayOf(
                            -1f,  0f,  0f,  0f, 255f,
                             0f, -1f,  0f,  0f, 255f,
                             0f,  0f, -1f,  0f, 255f,
                             0f,  0f,  0f,  1f,   0f
                        )
                    )
                    val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
                    invertMatrix.postConcat(grayMatrix)

                    // Boost contrast slightly so text is ultra-sharp on white paper
                    val contrast = 1.15f
                    val translate = (-0.5f * contrast + 0.5f) * 255f
                    val contrastMatrix = ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, translate,
                            0f, contrast, 0f, 0f, translate,
                            0f, 0f, contrast, 0f, translate,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    invertMatrix.postConcat(contrastMatrix)

                    val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = ColorMatrixColorFilter(invertMatrix)
                    }
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(source, 0f, 0f, paint)
                } else {
                    // Light slide: Keep as crisp high-contrast grayscale to save ink while looking pristine
                    val grayMatrix = ColorMatrix().apply { setSaturation(0f) }
                    val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                        colorFilter = ColorMatrixColorFilter(grayMatrix)
                    }
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(source, 0f, 0f, paint)
                }
                dest
            }
        }
    }

    /**
     * Builds the complete multi-page N-up PDF document.
     */
    suspend fun generateNUpPdf(
        context: Context,
        sourcePdfFile: File,
        selectedIndices: List<Int>,
        config: LayoutConfig,
        slidesMetadata: Map<Int, Boolean>, // index -> isDark
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "SlidePrint_${System.currentTimeMillis()}.pdf")
        if (outputFile.exists()) outputFile.delete()

        val pfd = ParcelFileDescriptor.open(sourcePdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)

        val doc = PdfDocument()

        val isLandscape = config.orientation == PageOrientation.LANDSCAPE
        val sheetWidth = if (isLandscape) A4_HEIGHT_POINTS else A4_WIDTH_POINTS
        val sheetHeight = if (isLandscape) A4_WIDTH_POINTS else A4_HEIGHT_POINTS

        val slidesPerPage = config.slidesPerPage
        val totalSlides = selectedIndices.size
        val totalSheetPages = (totalSlides + slidesPerPage - 1) / slidesPerPage

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            color = Color.rgb(180, 185, 180)
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            color = Color.rgb(120, 125, 120)
        }

        val slideNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            color = Color.rgb(90, 95, 90)
        }

        val noteLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = Color.rgb(205, 210, 205)
        }

        val margin = config.marginPaddingDp.toFloat()
        val footerAreaHeight = 22f

        val printableWidth = sheetWidth - (margin * 2)
        val printableHeight = sheetHeight - (margin * 2) - footerAreaHeight

        for (sheetIndex in 0 until totalSheetPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(sheetWidth, sheetHeight, sheetIndex + 1).create()
            val sheetPage = doc.startPage(pageInfo)
            val canvas = sheetPage.canvas

            // Fill sheet with crisp white paper
            canvas.drawColor(Color.WHITE)

            val startSlideIndex = sheetIndex * slidesPerPage
            val endSlideIndex = min(startSlideIndex + slidesPerPage, totalSlides)

            // Symmetrically Centered N-Up Grid (1x1, 1x2, 1x3, 2x2, 2x3, 3x3, etc.)
            val cols = config.columns
            val rows = config.rows
            val cellSpacingH = 12f
            val cellSpacingV = 14f

                // Reserve height for slide caption if enabled
                val slideCaptionHeight = if (config.drawSlideNumbers) 11f else 0f

                val maxCellW = (printableWidth - (cellSpacingH * (cols - 1))) / cols
                val maxCellH = (printableHeight - (cellSpacingV * (rows - 1))) / rows
                val maxSlideH = maxCellH - slideCaptionHeight

                // Standard widescreen slide aspect ratio (16:9)
                val slideAspect = 16f / 9f

                val (fitW, fitH) = if (maxCellW / maxSlideH > slideAspect) {
                    val h = maxSlideH
                    val w = h * slideAspect
                    Pair(w, h)
                } else {
                    val w = maxCellW
                    val h = w / slideAspect
                    Pair(w, h)
                }

                val cellTotalH = fitH + slideCaptionHeight

                // Symmetrically center the entire grid of slides horizontally & vertically on the sheet
                val totalGridW = (cols * fitW) + ((cols - 1) * cellSpacingH)
                val totalGridH = (rows * cellTotalH) + ((rows - 1) * cellSpacingV)

                val gridStartX = margin + (printableWidth - totalGridW) / 2f
                val gridStartY = margin + (printableHeight - totalGridH) / 2f

                var currentSlot = 0
                for (slidePos in startSlideIndex until endSlideIndex) {
                    val originalPageIndex = selectedIndices[slidePos]
                    val c = currentSlot % cols
                    val r = currentSlot / cols

                    val slideX = gridStartX + c * (fitW + cellSpacingH)
                    val slideY = gridStartY + r * (cellTotalH + cellSpacingV)
                    val drawRect = RectF(slideX, slideY, slideX + fitW, slideY + fitH)

                    val originalPage = renderer.openPage(originalPageIndex)
                    val renderScale = 2.4f
                    val bmpW = (fitW * renderScale).toInt()
                    val bmpH = (fitH * renderScale).toInt()
                    val rawBitmap = Bitmap.createBitmap(max(10, bmpW), max(10, bmpH), Bitmap.Config.ARGB_8888)
                    val rawCanvas = Canvas(rawBitmap)
                    rawCanvas.drawColor(Color.WHITE)
                    originalPage.render(rawBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    originalPage.close()

                    val isDark = slidesMetadata[originalPageIndex] ?: false
                    val processedBitmap = processBitmapColorMode(rawBitmap, config.colorMode, isDark)

                    canvas.drawBitmap(processedBitmap, null, drawRect, null)
                    if (config.drawSlideBorders) {
                        canvas.drawRect(drawRect, borderPaint)
                    }
                    if (config.drawSlideNumbers) {
                        val numText = "Slide ${originalPageIndex + 1}"
                        val numWidth = slideNumPaint.measureText(numText)
                        val numX = slideX + (fitW - numWidth) / 2f
                        val numY = slideY + fitH + 9f
                        canvas.drawText(numText, numX, numY, slideNumPaint)
                    }
                    currentSlot++
                }

            // Sheet Footer ("Page X of Y • SlidePrint")
            val footerText = "Sheet ${sheetIndex + 1} of $totalSheetPages  •  SlidePrint"
            val footerWidth = footerPaint.measureText(footerText)
            canvas.drawText(
                footerText,
                (sheetWidth - footerWidth) / 2f,
                sheetHeight - margin / 2f,
                footerPaint
            )

            doc.finishPage(sheetPage)
            onProgress((sheetIndex + 1).toFloat() / totalSheetPages)
        }

        FileOutputStream(outputFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        renderer.close()
        pfd.close()

        outputFile
    }

    /**
     * Renders preview images of the generated final PDF document pages.
     */
    suspend fun renderPdfPagePreviews(pdfFile: File, maxPages: Int = 12): List<Bitmap> = withContext(Dispatchers.IO) {
        val previews = mutableListOf<Bitmap>()
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val count = min(renderer.pageCount, maxPages)

        for (i in 0 until count) {
            val page = renderer.openPage(i)
            val previewW = 420
            val previewH = (previewW * (page.height.toFloat() / page.width)).toInt()

            val bitmap = Bitmap.createBitmap(previewW, previewH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            previews.add(bitmap)
        }

        renderer.close()
        pfd.close()
        previews
    }

    /**
     * Merges two PDF files into a single combined PDF document in cache.
     */
    suspend fun mergePdfs(context: Context, primaryFile: File, secondaryFile: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "merged_${System.currentTimeMillis()}.pdf")
        val doc = PdfDocument()
        var targetPageNumber = 1

        for (file in listOf(primaryFile, secondaryFile)) {
            if (!file.exists()) continue
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            for (pageIdx in 0 until renderer.pageCount) {
                val page = renderer.openPage(pageIdx)
                val width = page.width
                val height = page.height
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, targetPageNumber++).create()
                val docPage = doc.startPage(pageInfo)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                docPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                doc.finishPage(docPage)
                page.close()
                bitmap.recycle()
            }
            renderer.close()
            pfd.close()
        }

        FileOutputStream(outputFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        outputFile
    }

    /**
     * Cleans up all accumulated temporary, preview, and generated files from cache directory.
     */
    fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                try {
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else if (file.isFile) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
