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
internal val filledCheckBox: ImageVector
  get() {
    if (_filledCheckBox != null) {
      return _filledCheckBox!!
    }
    _filledCheckBox =
      ImageVector.Builder(
          name = "check_box",
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
            moveTo(10.6f, 13.4f)
            lineTo(8.45f, 11.25f)
            quadTo(8.18f, 10.98f, 7.75f, 10.98f)
            quadToRelative(-0.42f, 0f, -0.7f, 0.28f)
            quadToRelative(-0.28f, 0.27f, -0.28f, 0.7f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            lineTo(9.9f, 15.5f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineTo(16.95f, 9.85f)
            quadToRelative(0.28f, -0.28f, 0.28f, -0.7f)
            quadToRelative(0f, -0.42f, -0.28f, -0.7f)
            quadTo(16.68f, 8.17f, 16.25f, 8.17f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            lineTo(10.6f, 13.4f)
            close()
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(5f)
            close()
          }
        }
        .build()
    return _filledCheckBox!!
  }

internal var _filledCheckBox: ImageVector? = null
