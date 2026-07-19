package com.bernaferrari.remarkor.ui.icons

// Generated from Google Material Symbols Rounded's Kotlin vector endpoint.
// The FILL axis is explicit: FILL=1 for Filled and FILL=0 for Outlined.
// opsz=24, wght=400, GRAD=0, ROND=50.
// Source: https://fonts.gstatic.com/render/v1/Material+Symbols+Rounded/24dp/<name>.kt

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
internal val filledFormatStrikethrough: ImageVector
  get() {
    if (_filledFormatStrikethrough != null) {
      return _filledFormatStrikethrough!!
    }
    _filledFormatStrikethrough =
      ImageVector.Builder(
          name = "format_strikethrough",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(3f, 14f)
            quadTo(2.58f, 14f, 2.29f, 13.71f)
            quadTo(2f, 13.43f, 2f, 13f)
            reflectiveQuadTo(2.29f, 12.29f)
            reflectiveQuadTo(3f, 12f)
            horizontalLineTo(21f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 13f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(21f, 14f)
            horizontalLineTo(3f)
            close()
            moveToRelative(7.5f, -4f)
            verticalLineTo(7f)
            horizontalLineToRelative(-4f)
            quadTo(5.88f, 7f, 5.44f, 6.56f)
            reflectiveQuadTo(5f, 5.5f)
            reflectiveQuadTo(5.44f, 4.44f)
            reflectiveQuadTo(6.5f, 4f)
            horizontalLineToRelative(11f)
            quadToRelative(0.63f, 0f, 1.06f, 0.44f)
            reflectiveQuadTo(19f, 5.5f)
            reflectiveQuadTo(18.56f, 6.56f)
            reflectiveQuadTo(17.5f, 7f)
            horizontalLineToRelative(-4f)
            verticalLineToRelative(3f)
            horizontalLineToRelative(-3f)
            close()
            moveToRelative(0f, 6f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(2.5f)
            quadToRelative(0f, 0.63f, -0.44f, 1.06f)
            reflectiveQuadTo(12f, 20f)
            reflectiveQuadTo(10.94f, 19.56f)
            reflectiveQuadTo(10.5f, 18.5f)
            verticalLineTo(16f)
            close()
          }
        }
        .build()
    return _filledFormatStrikethrough!!
  }

internal var _filledFormatStrikethrough: ImageVector? = null
