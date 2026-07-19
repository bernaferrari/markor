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
internal val filledExpandLess: ImageVector
  get() {
    if (_filledExpandLess != null) {
      return _filledExpandLess!!
    }
    _filledExpandLess =
      ImageVector.Builder(
          name = "expand_less",
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
            moveTo(12f, 10.77f)
            lineToRelative(-3.9f, 3.9f)
            quadTo(7.83f, 14.95f, 7.4f, 14.95f)
            reflectiveQuadTo(6.7f, 14.68f)
            reflectiveQuadTo(6.43f, 13.98f)
            reflectiveQuadTo(6.7f, 13.27f)
            lineToRelative(4.6f, -4.6f)
            quadTo(11.45f, 8.52f, 11.63f, 8.46f)
            reflectiveQuadTo(12f, 8.4f)
            reflectiveQuadToRelative(0.38f, 0.06f)
            reflectiveQuadTo(12.7f, 8.67f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
            reflectiveQuadToRelative(-0.27f, 0.7f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            reflectiveQuadTo(15.9f, 14.68f)
            lineTo(12f, 10.77f)
            close()
          }
        }
        .build()
    return _filledExpandLess!!
  }

internal var _filledExpandLess: ImageVector? = null
