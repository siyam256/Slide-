package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private enum class SettingsSubScreen {
    MAIN,
    HOW_TO_USE,
    ABOUT,
    PRIVACY_POLICY
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentSubScreen != SettingsSubScreen.MAIN) {
                            IconButton(
                                onClick = { currentSubScreen = SettingsSubScreen.MAIN },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = when (currentSubScreen) {
                                SettingsSubScreen.MAIN -> "Settings"
                                SettingsSubScreen.HOW_TO_USE -> "How to Use"
                                SettingsSubScreen.ABOUT -> "About SlidePrint"
                                SettingsSubScreen.PRIVACY_POLICY -> "Privacy Policy"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentSubScreen) {
                        SettingsSubScreen.MAIN -> {
                            SettingsMainMenu(
                                onNavigate = { currentSubScreen = it },
                                onRateUsClick = {
                                    val url = "https://play.google.com/store/apps/details?id=com.slideprint.app"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            )
                        }

                        SettingsSubScreen.HOW_TO_USE -> HowToUseContent()
                        SettingsSubScreen.ABOUT -> AboutSlidePrintContent()
                        SettingsSubScreen.PRIVACY_POLICY -> PrivacyPolicyContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMainMenu(
    onNavigate: (SettingsSubScreen) -> Unit,
    onRateUsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsItemCard(
            title = "How to Use",
            subtitle = "Step-by-step guide to generating handouts",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { onNavigate(SettingsSubScreen.HOW_TO_USE) },
            testTag = "settings_how_to_use"
        )

        SettingsItemCard(
            title = "Rate Us",
            subtitle = "Support SlidePrint on Google Play Store",
            icon = Icons.Default.Star,
            iconTint = Color(0xFFF59E0B),
            onClick = onRateUsClick,
            testTag = "settings_rate_us"
        )

        SettingsItemCard(
            title = "About SlidePrint",
            subtitle = "App details, version & developer mission",
            icon = Icons.Default.Info,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { onNavigate(SettingsSubScreen.ABOUT) },
            testTag = "settings_about"
        )

        SettingsItemCard(
            title = "Privacy Policy",
            subtitle = "100% offline & zero user data collection",
            icon = Icons.Default.PrivacyTip,
            iconTint = Color(0xFF10B981),
            onClick = { onNavigate(SettingsSubScreen.PRIVACY_POLICY) },
            testTag = "settings_privacy_policy"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Footer branding
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SlidePrint v1.0.2",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Free, Offline, and Privacy-Focused",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HowToUseContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Master SlidePrint in 5 Easy Steps",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        HowToStepItem(
            step = "1",
            title = "Import & Combine PDFs",
            description = "Tap 'Browse Files' to load any slide deck or document. Want multiple presentations in one printout? Simply tap 'Add More PDFs' in the pages screen to merge them."
        )

        HowToStepItem(
            step = "2",
            title = "Select Your Slides",
            description = "Use 'All' to print the full deck or 'None' to clear. Tap any individual slide card to toggle inclusion. Real-time thumbnails help you review content."
        )

        HowToStepItem(
            step = "3",
            title = "Configure Custom Grid Layout",
            description = "Adjust Columns (1 to 5) and Rows (1 to 6) to fit your handouts (e.g., 2×2, 1×3 vertical, 2×3, or 3×3). Select Portrait or Landscape orientation."
        )

        HowToStepItem(
            step = "4",
            title = "Outline Borders & Slide Numbers",
            description = "Toggle 'Draw Slide Outline Borders' for a neat framing line around every slide on white paper, and 'Draw Slide Numbers' to print 'Slide 1, Slide 2...' underneath. Watch the live sheet preview reflect your changes instantly."
        )

        HowToStepItem(
            step = "5",
            title = "Save Ink with Magic Print & Export",
            description = "Choose 'Magic Print' to intelligently convert dark lecture slides into crisp white backgrounds while keeping text and diagrams intact—saving up to 80% ink and toner! Save the PDF, share with friends, or print directly via your phone."
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "✓ Automatic Cleanup: Whenever you exit or finish, SlidePrint clears all temporary cache files to keep your phone storage completely clean.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun HowToStepItem(
    step: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
private fun AboutSlidePrintContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SlidePrint",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Version 1.0.2 (Build 3)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Smart Slide & PDF Handout Generator",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = "About the App",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Text(
            text = "SlidePrint is a free, offline application designed specifically to help students, teachers, and professionals arrange multiple PDF slides onto single sheets, drastically cutting down printing costs and paper usage.\n\nWhether printing university lecture slides, business decks, or study summaries, SlidePrint puts you in full control of layout density, borders, slide numbering, and ink consumption.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
        )

        Text(
            text = "Key Features",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        val features = listOf(
            "100% Free & Completely Offline",
            "Zero User Data Collection — Absolute Privacy",
            "Magic Print Dark-to-Light Ink Saver Technology",
            "Multi-PDF Merging & Custom N-up Grids (1 to 30 slides/sheet)",
            "Automatic Slide Outline Borders & Slide Numbering",
            "Direct Android Print Spooler & PDF Export Integration",
            "Automated Cache Cleaning to Protect Phone Storage"
        )

        features.forEach { feat ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "•",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = feat,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "SlidePrint — Smart Slide & PDF Handout Generator\nLast Updated: October 15, 2026",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        val policyText = """
Your privacy matters. SlidePrint is a free, offline application that does not collect user information or personal data.

1. Overview
SlidePrint is a free, offline application designed to help students and teachers arrange multiple PDF slides on a single page, save paper and ink, and reduce printing costs.

We respect your privacy. SlidePrint does not collect any user information or personal data.

2. Information Collection
SlidePrint does not collect any information from its users. No personal information or other user data is collected, stored, or transmitted by the app.

3. Offline Usage
SlidePrint works offline. No internet connection is required to use its core features.

4. No Login or Registration
SlidePrint does not require any account, login, sign-up, or registration. You can use the app without providing any information.

5. File Access and Permissions
SlidePrint does not require broad storage permissions. Users can select PDF files through Android's system file picker.

6. No Advertisements
SlidePrint is completely free and contains no advertisements.

7. Data Sharing
SlidePrint does not collect user information; therefore, it does not share, sell, or distribute user data to any third party.

8. Purpose of the App
SlidePrint is developed to help students, teachers, and other users create compact PDF print layouts conveniently while saving paper and printing costs.

9. Changes to This Policy
If this Privacy Policy changes in the future, the updated version will be published on this page.

10. Contact
For questions about SlidePrint, please contact the app developer through the support contact provided on the app's store listing.

SlidePrint
Free, Offline, and Privacy-Focused.
        """.trimIndent()

        Text(
            text = policyText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 19.sp
            )
        )
    }
}
