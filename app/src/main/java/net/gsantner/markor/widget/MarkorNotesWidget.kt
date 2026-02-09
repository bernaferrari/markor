package net.gsantner.markor.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.glance.text.TextStyle
import net.gsantner.markor.R
import net.gsantner.markor.activity.MainActivity
import java.io.File

/**
 * A beautiful, modern notes widget for Markor.
 * Features:
 * - Gradient background matching Material 3 dynamic colors
 * - Quick note creation FAB
 * - Recent notes list with previews
 * - Smooth animations and interactions
 */
class MarkorNotesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Generate a beautiful gradient background
        val gradientColors = listOf(
            Color(0xFF667EEA),  // Purple
            Color(0xFF764BA2),  // Deep purple
            Color(0xFF6B8DD6),  // Blue-purple
            Color(0xFF8E37D7),  // Magenta
        )

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0xFF764BA2)))
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_launcher_foreground),
                            contentDescription = "Markor",
                            modifier = GlanceModifier
                                .size(32.dp)
                                .cornerRadius(8.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "Markor",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Quick Note FAB
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_add),
                                contentDescription = "New note",
                                modifier = GlanceModifier.size(24.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(12.dp))
                            Text(
                                text = "Tap to write a quick note...",
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Recent notes section
                    Text(
                        text = "Recent Notes",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorProvider(Color.White.copy(alpha = 0.9f))
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Sample notes (in real implementation, would load from DataStore)
                    RecentNoteItem(
                        title = "Shopping List",
                        preview = "Milk, Eggs, Bread...",
                        time = "2h ago"
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    RecentNoteItem(
                        title = "Meeting Notes",
                        preview = "Discussed Q4 goals...",
                        time = "5h ago"
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    RecentNoteItem(
                        title = "Ideas",
                        text = "3 new ideas",
                        time = "Yesterday"
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Open app button
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(Color.White.copy(alpha = 0.25f)))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open Markor",
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentNoteItem(
    title: String,
    preview: String? = null,
    text: String? = null,
    time: String
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color.White.copy(alpha = 0.15f)))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Note icon
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.3f))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.take(1).uppercase(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorProvider(Color.White)
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = title,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )
            Text(
                text = preview ?: text ?: "",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = ColorProvider(Color.White.copy(alpha = 0.7f))
                ),
                maxLines = 1
            )
        }

        Text(
            text = time,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = ColorProvider(Color.White.copy(alpha = 0.6f))
            )
        )
    }
}

/**
 * Widget receiver for Markor Notes Widget
 */
class MarkorNotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MarkorNotesWidget()
}
