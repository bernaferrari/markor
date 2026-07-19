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
internal val filledLabel: ImageVector
  get() {
    if (_filledLabel != null) {
      return _filledLabel!!
    }
    _filledLabel =
      ImageVector.Builder(
          name = "label",
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
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(15f)
            quadToRelative(0.48f, 0f, 0.9f, 0.21f)
            reflectiveQuadTo(16.6f, 4.8f)
            lineToRelative(4.5f, 6f)
            quadToRelative(0.4f, 0.53f, 0.4f, 1.2f)
            reflectiveQuadToRelative(-0.4f, 1.2f)
            lineToRelative(-4.5f, 6f)
            quadToRelative(-0.28f, 0.38f, -0.7f, 0.59f)
            reflectiveQuadTo(15f, 20f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _filledLabel!!
  }

internal var _filledLabel: ImageVector? = null
