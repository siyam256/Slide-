package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppStep
import com.example.model.ColorPrintMode
import com.example.model.LayoutConfig
import com.example.model.PageLayoutPreset
import com.example.model.PageOrientation
import com.example.model.SlideItem
import com.example.util.PdfEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class SlidePrintUiState(
    val currentStep: AppStep = AppStep.SOURCE,
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val pdfFile: File? = null,
    val pdfFileName: String = "",
    val pdfFileSize: String = "",
    val slides: List<SlideItem> = emptyList(),
    val selectedIndices: Set<Int> = emptySet(),
    val layoutConfig: LayoutConfig = LayoutConfig(),
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,
    val generatedPdf: File? = null,
    val generatedPreviews: List<Bitmap> = emptyList(),
    val errorMessage: String? = null,
    // Preview sample for Magic Print Before/After demonstration
    val magicSampleOriginal: Bitmap? = null,
    val magicSampleProcessed: Bitmap? = null,
    val isMagicSampleDark: Boolean = true
)

class SlidePrintViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SlidePrintUiState())
    val uiState: StateFlow<SlidePrintUiState> = _uiState.asStateFlow()



    fun loadPdfFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Reading PDF file...", errorMessage = null) }
            try {
                val context = getApplication<Application>()
                // Clean up previous session cache files
                PdfEngine.clearAppCache(context)

                val contentResolver = context.contentResolver

                // Extract filename and size
                var fileName = "Document.pdf"
                var fileSizeStr = ""
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) {
                            val sizeBytes = cursor.getLong(sizeIndex)
                            fileSizeStr = formatFileSize(sizeBytes)
                        }
                    }
                }

                // Copy to cache file
                val tempFile = File(context.cacheDir, "imported_${System.currentTimeMillis()}.pdf")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                processLoadedPdf(tempFile, fileName, fileSizeStr)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not open PDF: ${e.message}") }
            }
        }
    }

    fun appendPdfFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Merging additional PDF...", errorMessage = null) }
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver

                var addedName = "Additional.pdf"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        addedName = cursor.getString(nameIndex) ?: addedName
                    }
                }

                val newTempFile = File(context.cacheDir, "imported_add_${System.currentTimeMillis()}.pdf")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(newTempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val currentFile = _uiState.value.pdfFile
                if (currentFile == null || !currentFile.exists()) {
                    processLoadedPdf(newTempFile, addedName, "")
                    return@launch
                }

                val combinedFile = PdfEngine.mergePdfs(context, currentFile, newTempFile)
                val combinedName = "${_uiState.value.pdfFileName} + $addedName"
                val combinedSize = formatFileSize(combinedFile.length())

                processLoadedPdf(combinedFile, combinedName, combinedSize)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not add PDF: ${e.message}") }
            }
        }
    }

    private suspend fun processLoadedPdf(file: File, fileName: String, fileSize: String) {
        val context = getApplication<Application>()
        _uiState.update { it.copy(loadingMessage = "Rendering slide thumbnails & analyzing colors...") }

        val slideItems = PdfEngine.extractSlideThumbnails(context, file) { current, total ->
            _uiState.update { it.copy(loadingMessage = "Analyzing slide $current of $total...") }
        }

        val allIndices = slideItems.map { it.index }.toSet()

        // Find best sample slide for Magic Print Before/After demo
        val darkSlide = slideItems.firstOrNull { it.isDarkSlide } ?: slideItems.firstOrNull()
        val sampleOriginal = darkSlide?.thumbnail
        val isDark = darkSlide?.isDarkSlide ?: false
        val sampleProcessed = sampleOriginal?.let {
            PdfEngine.processBitmapColorMode(it, ColorPrintMode.MAGIC_PRINT, isDark)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                loadingMessage = "",
                pdfFile = file,
                pdfFileName = fileName,
                pdfFileSize = if (fileSize.isNotEmpty()) fileSize else "${slideItems.size} pages",
                slides = slideItems,
                selectedIndices = allIndices,
                magicSampleOriginal = sampleOriginal,
                magicSampleProcessed = sampleProcessed,
                isMagicSampleDark = isDark,
                currentStep = AppStep.PAGES,
                errorMessage = null
            )
        }
    }

    fun toggleSlideSelection(index: Int) {
        _uiState.update { state ->
            val updated = state.selectedIndices.toMutableSet()
            if (updated.contains(index)) {
                updated.remove(index)
            } else {
                updated.add(index)
            }
            state.copy(selectedIndices = updated)
        }
    }

    fun selectAllSlides() {
        _uiState.update { state ->
            state.copy(selectedIndices = state.slides.map { it.index }.toSet())
        }
    }

    fun clearAllSlides() {
        _uiState.update { state ->
            state.copy(selectedIndices = emptySet())
        }
    }

    fun invertSlideSelection() {
        _uiState.update { state ->
            val all = state.slides.map { it.index }.toSet()
            state.copy(selectedIndices = all - state.selectedIndices)
        }
    }

    fun selectEvenSlides() {
        _uiState.update { state ->
            state.copy(selectedIndices = state.slides.filter { it.pageNumber % 2 == 0 }.map { it.index }.toSet())
        }
    }

    fun selectOddSlides() {
        _uiState.update { state ->
            state.copy(selectedIndices = state.slides.filter { it.pageNumber % 2 != 0 }.map { it.index }.toSet())
        }
    }



    fun setPageOrientation(orientation: PageOrientation) {
        _uiState.update { state ->
            state.copy(layoutConfig = state.layoutConfig.copy(orientation = orientation))
        }
    }

    fun setCustomGrid(cols: Int, rows: Int) {
        _uiState.update { state ->
            state.copy(
                layoutConfig = state.layoutConfig.copy(
                    preset = PageLayoutPreset.CUSTOM,
                    columns = cols.coerceIn(1, 5),
                    rows = rows.coerceIn(1, 6),
                    drawNotesLines = false
                )
            )
        }
    }

    fun toggleSlideBorders(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(layoutConfig = state.layoutConfig.copy(drawSlideBorders = enabled))
        }
    }

    fun toggleSlideNumbers(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(layoutConfig = state.layoutConfig.copy(drawSlideNumbers = enabled))
        }
    }

    fun setColorPrintMode(mode: ColorPrintMode) {
        _uiState.update { state ->
            val newConfig = state.layoutConfig.copy(colorMode = mode)
            // Re-render sample processed bitmap for demonstration
            val processedSample = state.magicSampleOriginal?.let {
                PdfEngine.processBitmapColorMode(it, mode, state.isMagicSampleDark)
            }
            state.copy(
                layoutConfig = newConfig,
                magicSampleProcessed = processedSample
            )
        }
    }

    fun goToStep(step: AppStep) {
        _uiState.update { it.copy(currentStep = step, errorMessage = null) }
    }

    fun generateNUpPdf() {
        val state = _uiState.value
        val sourceFile = state.pdfFile ?: return
        val selected = state.selectedIndices.sorted()
        if (selected.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least 1 slide to generate handouts.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    generationProgress = 0.05f,
                    loadingMessage = "Composing N-up layout...",
                    errorMessage = null
                )
            }
            try {
                val context = getApplication<Application>()
                val darkMap = state.slides.associate { it.index to it.isDarkSlide }

                val generatedFile = PdfEngine.generateNUpPdf(
                    context = context,
                    sourcePdfFile = sourceFile,
                    selectedIndices = selected,
                    config = state.layoutConfig,
                    slidesMetadata = darkMap
                ) { progress ->
                    _uiState.update {
                        it.copy(
                            generationProgress = 0.1f + (progress * 0.7f),
                            loadingMessage = "Rendering sheet ${(progress * 100).toInt()}%..."
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        generationProgress = 0.85f,
                        loadingMessage = "Generating output previews..."
                    )
                }

                val previews = PdfEngine.renderPdfPagePreviews(generatedFile)

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationProgress = 1f,
                        generatedPdf = generatedFile,
                        generatedPreviews = previews,
                        currentStep = AppStep.EXPORT
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Generation failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetToStart() {
        cleanupCache()
        _uiState.update {
            SlidePrintUiState()
        }
    }

    fun cleanupCache() {
        try {
            val context = getApplication<Application>()
            PdfEngine.clearAppCache(context)
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        cleanupCache()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return ""
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(kb)
    }
}
