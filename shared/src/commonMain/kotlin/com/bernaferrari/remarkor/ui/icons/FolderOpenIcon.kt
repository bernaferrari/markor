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
internal val filledFolderOpen: ImageVector
  get() {
    if (_filledFolderOpen != null) {
      return _filledFolderOpen!!
    }
    _filledFolderOpen =
      ImageVector.Builder(
          name = "folder_open",
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
            horizontalLineTo(9.18f)
            quadToRelative(0.4f, 0f, 0.76f, 0.15f)
            reflectiveQuadToRelative(0.64f, 0.43f)
            lineTo(12f, 6f)
            horizontalLineToRelative(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 7f)
            reflectiveQuadTo(21.71f, 7.71f)
            reflectiveQuadTo(21f, 8f)
            horizontalLineTo(7.85f)
            quadTo(6.3f, 8f, 5.15f, 8.98f)
            reflectiveQuadTo(4f, 11.45f)
            verticalLineTo(18f)
            lineTo(5.98f, 11.43f)
            quadToRelative(0.2f, -0.65f, 0.74f, -1.04f)
            reflectiveQuadTo(7.9f, 10f)
            horizontalLineTo(20.8f)
            quadToRelative(1.03f, 0f, 1.61f, 0.81f)
            reflectiveQuadToRelative(0.31f, 1.76f)
            lineToRelative(-1.8f, 6f)
            quadToRelative(-0.2f, 0.65f, -0.74f, 1.04f)
            reflectiveQuadTo(19f, 20f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _filledFolderOpen!!
  }

internal var _filledFolderOpen: ImageVector? = null
