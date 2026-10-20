package com.example.ui.screens

import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LayoutConfig
import com.example.model.PageOrientation

@Composable
fun LayoutSelectionScreen(
    config: LayoutConfig,
    selectedSlidesCount: Int,
    onSetOrientation: (PageOrientation) -> Unit,
    onSetCustomGrid: (cols: Int, rows: Int) -> Unit,
    onToggleBorders: (Boolean) -> Unit,
    onToggleSlideNumbers: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slidesPerPage = config.columns * config.rows
    val totalSheets = if (slidesPerPage > 0) (selectedSlidesCount + slidesPerPage - 1) / slidesPerPage else 1

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${config.columns} × ${config.rows} Layout ($slidesPerPage slides / sheet)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Orientation: ${config.orientation.label}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$totalSheets Sheet${if (totalSheets > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Manual Grid Dimensions & Live Demo Preview Card
            item {
                Text(
                    text = "Page Layout & Dimensions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Orientation Selection
                        Text(
                            text = "Page Orientation",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OrientationButton(
                                label = "Portrait (Vertical)",
                                icon = Icons.Default.StayCurrentPortrait,
                                isSelected = config.orientation == PageOrientation.PORTRAIT,
                                onClick = { onSetOrientation(PageOrientation.PORTRAIT) },
                                modifier = Modifier.weight(1f)
                            )
                            OrientationButton(
                                label = "Landscape (Horizontal)",
                                icon = Icons.Default.StayCurrentLandscape,
                                isSelected = config.orientation == PageOrientation.LANDSCAPE,
                                onClick = { onSetOrientation(PageOrientation.LANDSCAPE) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Grid Columns & Rows Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepperControl(
                                title = "Columns (Across)",
                                value = config.columns,
                                minValue = 1,
                                maxValue = 5,
                                onValueChange = { onSetCustomGrid(it, config.rows) },
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            StepperControl(
                                title = "Rows (Down)",
                                value = config.rows,
                                minValue = 1,
                                maxValue = 6,
                                onValueChange = { onSetCustomGrid(config.columns, it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Slide Options (Borders & Numbers) - Placed compactly near the top
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Slide Options",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CropSquare,
                                                contentDescription = null,
                                                tint = if (config.drawSlideBorders) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Draw Slide Outline Borders",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Switch(
                                            checked = config.drawSlideBorders,
                                            onCheckedChange = onToggleBorders,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .scale(0.85f)
                                                .testTag("switch_slide_borders")
                                        )
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FormatListNumbered,
                                                contentDescription = null,
                                                tint = if (config.drawSlideNumbers) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Draw Slide Numbers",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Switch(
                                            checked = config.drawSlideNumbers,
                                            onCheckedChange = onToggleSlideNumbers,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .scale(0.85f)
                                                .testTag("switch_slide_numbers")
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Live Demo Sheet Canvas Preview
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Demo Sheet Preview",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Centered ${config.columns} × ${config.rows} Grid",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val sheetAspect = if (config.orientation == PageOrientation.PORTRAIT) 595f / 842f else 842f / 595f
                            Box(
                                modifier = Modifier
                                    .height(165.dp)
                                    .aspectRatio(sheetAspect)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(6.dp)
                            ) {
                                CustomSheetLivePreview(
                                    cols = config.columns,
                                    rows = config.rows,
                                    hasBorders = config.drawSlideBorders,
                                    hasNumbers = config.drawSlideNumbers
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fits ${config.columns * config.rows} slides centered per sheet",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Bar (Compact & Sleek)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back", fontSize = 13.sp)
                }

                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("next_print_mode_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Next: Print Mode",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrientationButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepperControl(
    title: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = { if (value > minValue) onValueChange(value - 1) },
                enabled = value > minValue,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = { if (value < maxValue) onValueChange(value + 1) },
                enabled = value < maxValue,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun CustomSheetLivePreview(
    cols: Int,
    rows: Int,
    hasBorders: Boolean,
    hasNumbers: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val sheetW = size.width
        val sheetH = size.height

        val pad = 4f
        val printableW = sheetW - pad * 2
        val printableH = sheetH - pad * 2

        val spaceX = 4f
        val spaceY = 4f

        // Reserve space for "Slide X" caption under each slide when hasNumbers is enabled
        val numberCaptionH = if (hasNumbers) max(10f, sheetH * 0.06f) else 0f

        val maxCellW = (printableW - (spaceX * (cols - 1))) / cols
        val maxCellH = (printableH - (spaceY * (rows - 1))) / rows
        val availSlideH = maxCellH - numberCaptionH
        val slideAspect = 16f / 9f

        val (fitW, fitH) = if (maxCellW / availSlideH > slideAspect) {
            val h = availSlideH
            val w = h * slideAspect
            Pair(w, h)
        } else {
            val w = maxCellW
            val h = w / slideAspect
            Pair(w, h)
        }

        val cellTotalH = fitH + numberCaptionH
        val totalGridW = cols * fitW + (cols - 1) * spaceX
        val totalGridH = rows * cellTotalH + (rows - 1) * spaceY

        // Symmetrically center the entire grid of slides horizontally & vertically
        val startX = pad + (printableW - totalGridW) / 2f
        val startY = pad + (printableH - totalGridH) / 2f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = startX + c * (fitW + spaceX)
                val y = startY + r * (cellTotalH + spaceY)

                // Draw slide card body
                drawRoundRect(
                    color = Color(0xFF15803D), // Solid emerald green
                    topLeft = Offset(x, y),
                    size = Size(fitW, fitH),
                    cornerRadius = CornerRadius(2.5f, 2.5f)
                )

                // Miniature slide title bar
                val titleH = max(1.5f, fitH * 0.16f)
                val titleW = fitW * 0.58f
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.92f),
                    topLeft = Offset(x + fitW * 0.08f, y + fitH * 0.14f),
                    size = Size(titleW, titleH),
                    cornerRadius = CornerRadius(0.8f, 0.8f)
                )

                // Miniature slide body lines
                val lineH = max(1f, fitH * 0.09f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.72f),
                    topLeft = Offset(x + fitW * 0.08f, y + fitH * 0.42f),
                    size = Size(fitW * 0.82f, lineH),
                    cornerRadius = CornerRadius(0.5f, 0.5f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.72f),
                    topLeft = Offset(x + fitW * 0.08f, y + fitH * 0.62f),
                    size = Size(fitW * 0.64f, lineH),
                    cornerRadius = CornerRadius(0.5f, 0.5f)
                )

                // 1. Prominent Slide Framing Borders (clearly visible when toggled)
                if (hasBorders) {
                    drawRoundRect(
                        color = Color(0xFF0F172A), // Crisp deep black outline border
                        topLeft = Offset(x, y),
                        size = Size(fitW, fitH),
                        cornerRadius = CornerRadius(2.5f, 2.5f),
                        style = Stroke(width = max(1.6f, fitW * 0.026f))
                    )
                }

                // 2. Prominent Slide Numbers (clearly visible when toggled)
                if (hasNumbers) {
                    val slideIndex = r * cols + c + 1
                    val numText = "Slide $slideIndex"
                    val textSizePx = max(6.5f, min(12f, fitH * 0.20f))

                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(235, 15, 23, 42)
                        textSize = textSizePx
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        numText,
                        x + fitW / 2f,
                        y + fitH + textSizePx + 1.5f,
                        textPaint
                    )
                }
            }
        }
    }
}
