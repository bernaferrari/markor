package com.bernaferrari.remarkor.widget

import android.content.Context
import android.content.Intent
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
 * Todo Widget - Quick access to your todo list.
 * Shows task count and provides single-tap access.
 */
class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "todo")
        }

        // In a real implementation, load actual todo count from DataStore/Preferences
        val pendingTasks = 5
        val completedToday = 3

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color(0xFF34A853)))
                        .padding(16.dp)
                        .clickable(actionStartActivity(intent))
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(40.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
                                .cornerRadius(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_check_box_black_24dp),
                                contentDescription = "Todo",
                                modifier = GlanceModifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(12.dp))
                        Text(
                            text = "My Tasks",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Stats
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "$pendingTasks",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = ColorProvider(Color.White)
                                )
                            )
                            Text(
                                text = "pending",
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$completedToday",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = ColorProvider(Color.White)
                                )
                            )
                            Text(
                                text = "done today",
                                style = TextStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Quick add hint
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(Color.White.copy(alpha = 0.15f)))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_add),
                                contentDescription = null,
                                modifier = GlanceModifier.size(16.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "Tap to add task",
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = ColorProvider(Color.White.copy(alpha = 0.9f))
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
