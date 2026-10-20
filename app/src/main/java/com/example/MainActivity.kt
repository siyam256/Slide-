package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppStep
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SlidePrintTopBar
import com.example.ui.screens.ColorModeScreen
import com.example.ui.screens.ExportPreviewScreen
import com.example.ui.screens.LayoutSelectionScreen
import com.example.ui.screens.PageSelectionScreen
import com.example.ui.screens.SourceSelectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PrintHelper
import com.example.viewmodel.SlidePrintViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val viewModel: SlidePrintViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                SlidePrintApp(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanupCache()
    }
}

@Composable
fun SlidePrintApp(viewModel: SlidePrintViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Activity Result Launcher to pick PDF file
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadPdfFromUri(uri)
        }
    }

    // Activity Result Launcher to append / merge additional PDF files
    val appendDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.appendPdfFromUri(uri)
        }
    }

    // Activity Result Launcher to save the generated PDF
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destUri: Uri? ->
        if (destUri != null) {
            val sourceFile = uiState.generatedPdf
            if (sourceFile != null && sourceFile.exists()) {
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(destUri)?.use { out ->
                                FileInputStream(sourceFile).use { input ->
                                    input.copyTo(out)
                                }
                            }
                        }
                        snackbarHostState.showSnackbar("PDF saved successfully! Cache cleaned.")
                        viewModel.cleanupCache()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to save PDF: ${e.message}")
                    }
                }
            }
        }
    }

    // Handle Back Press across steps
    BackHandler(enabled = true) {
        when (uiState.currentStep) {
            AppStep.EXPORT -> viewModel.resetToStart()
            AppStep.COLOR -> viewModel.goToStep(AppStep.LAYOUT)
            AppStep.LAYOUT -> viewModel.goToStep(AppStep.PAGES)
            AppStep.PAGES -> viewModel.goToStep(AppStep.SOURCE)
            AppStep.SOURCE -> {
                viewModel.cleanupCache()
                (context as? ComponentActivity)?.finish()
            }
        }
    }

    // Display error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SlidePrintTopBar(
                currentStep = uiState.currentStep,
                onBackClick = {
                    when (uiState.currentStep) {
                        AppStep.EXPORT -> viewModel.resetToStart()
                        AppStep.COLOR -> viewModel.goToStep(AppStep.LAYOUT)
                        AppStep.LAYOUT -> viewModel.goToStep(AppStep.PAGES)
                        AppStep.PAGES -> viewModel.resetToStart()
                        AppStep.SOURCE -> {}
                    }
                },
                onStepClick = { step ->
                    if (step.ordinal < uiState.currentStep.ordinal) {
                        viewModel.goToStep(step)
                    }
                },
                onSettingsClick = {
                    showSettingsDialog = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> width / 3 } + fadeOut())
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "StepAnimation"
        ) { step ->
            when (step) {
                AppStep.SOURCE -> {
                    SourceSelectionScreen(
                        isLoading = uiState.isLoading,
                        loadingMessage = uiState.loadingMessage,
                        onBrowseClick = {
                            openDocumentLauncher.launch(arrayOf("application/pdf"))
                        }
                    )
                }

                AppStep.PAGES -> {
                    PageSelectionScreen(
                        fileName = uiState.pdfFileName,
                        fileSize = uiState.pdfFileSize,
                        slides = uiState.slides,
                        selectedIndices = uiState.selectedIndices,
                        onToggleSlide = { viewModel.toggleSlideSelection(it) },
                        onSelectAll = { viewModel.selectAllSlides() },
                        onClearAll = { viewModel.clearAllSlides() },
                        onAddMorePdfsClick = {
                            appendDocumentLauncher.launch(arrayOf("application/pdf"))
                        },
                        onNextClick = { viewModel.goToStep(AppStep.LAYOUT) }
                    )
                }

                AppStep.LAYOUT -> {
                    LayoutSelectionScreen(
                        config = uiState.layoutConfig,
                        selectedSlidesCount = uiState.selectedIndices.size,
                        onSetOrientation = { viewModel.setPageOrientation(it) },
                        onSetCustomGrid = { cols, rows -> viewModel.setCustomGrid(cols, rows) },
                        onToggleBorders = { viewModel.toggleSlideBorders(it) },
                        onToggleSlideNumbers = { viewModel.toggleSlideNumbers(it) },
                        onBackClick = { viewModel.goToStep(AppStep.PAGES) },
                        onNextClick = { viewModel.goToStep(AppStep.COLOR) }
                    )
                }

                AppStep.COLOR -> {
                    ColorModeScreen(
                        currentMode = uiState.layoutConfig.colorMode,
                        isGenerating = uiState.isGenerating,
                        generationProgress = uiState.generationProgress,
                        loadingMessage = uiState.loadingMessage,
                        sampleOriginal = uiState.magicSampleOriginal,
                        sampleProcessed = uiState.magicSampleProcessed,
                        isDarkSlideSample = uiState.isMagicSampleDark,
                        onSelectMode = { viewModel.setColorPrintMode(it) },
                        onBackClick = { viewModel.goToStep(AppStep.LAYOUT) },
                        onGenerateClick = { viewModel.generateNUpPdf() }
                    )
                }

                AppStep.EXPORT -> {
                    ExportPreviewScreen(
                        generatedPdf = uiState.generatedPdf,
                        previews = uiState.generatedPreviews,
                        config = uiState.layoutConfig,
                        totalSlidesSelected = uiState.selectedIndices.size,
                        onShareClick = {
                            val file = uiState.generatedPdf
                            if (file != null && file.exists()) {
                                val shareIntent = PrintHelper.createShareIntent(context, file)
                                context.startActivity(Intent.createChooser(shareIntent, "Share Handout PDF"))
                            } else {
                                Toast.makeText(context, "No generated PDF found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSaveClick = {
                            val defaultName = "Handout_${uiState.layoutConfig.preset.name.lowercase()}_${System.currentTimeMillis()}.pdf"
                            createDocumentLauncher.launch(defaultName)
                        },
                        onPrintClick = {
                            val file = uiState.generatedPdf
                            if (file != null && file.exists()) {
                                PrintHelper.printPdf(context, file, "SlidePrint_${uiState.layoutConfig.preset.name}")
                            } else {
                                Toast.makeText(context, "No generated PDF found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEditLayoutClick = {
                            viewModel.goToStep(AppStep.LAYOUT)
                        },
                        onStartOverClick = {
                            viewModel.resetToStart()
                        }
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
}
