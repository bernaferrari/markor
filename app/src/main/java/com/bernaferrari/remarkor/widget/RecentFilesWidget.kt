package com.bernaferrari.remarkor.widget

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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bernaferrari.remarkor.R
import com.bernaferrari.remarkor.activity.MainActivity

/**
 * Recent Files Widget - Shows your most recently accessed notes.
 * Provides quick access to your recent work.
 */
class RecentFilesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Sample data - in production would load from DataStore
        val recentFiles = listOf(
            RecentFile("Shopping List", "2h ago", Color(0xFFFFAFA3)),
            RecentFile("Meeting Notes", "5h ago", Color(0xFFCBF0F8)),
            RecentFile("Project Ideas", "Yesterday", Color(0xFFAECBFA)),
            RecentFile("Recipe", "2 days ago", Color(0xFFFDCFE8)),
        )

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF2D2D2D)))
                        .padding(16.dp)
                        .clickable(actionStartActivity(intent))
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
                                .size(28.dp)
                                .cornerRadius(8.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = "Recent Notes",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Recent files list
                    recentFiles.forEach { file ->
                        RecentFileRow(file = file)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Open app button
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(Color.White.copy(alpha = 0.1f)))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View All",
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = ColorProvider(Color.White.copy(alpha = 0.8f))
                            )
                        )
                    }
                }
            }
        }
    }
}

data class RecentFile(
    val name: String,
    val time: String,
    val color: Color
)

@Composable
private fun RecentFileRow(file: RecentFile) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .background(ColorProvider(file.color))
                .cornerRadius(4.dp)
        ) {}

        Spacer(modifier = GlanceModifier.width(12.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = file.name,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = ColorProvider(Color.White)
                ),
                maxLines = 1
            )
        }

        Text(
            text = file.time,
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = ColorProvider(Color.White.copy(alpha = 0.5f))
            )
        )
    }
}

class RecentFilesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentFilesWidget()
}
