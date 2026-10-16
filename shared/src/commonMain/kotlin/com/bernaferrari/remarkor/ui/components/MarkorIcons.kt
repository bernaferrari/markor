package com.bernaferrari.remarkor.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-drawn icons for places where the stock Material Symbol didn't say what the control does.
 * Drawn on the 24dp grid with 2px rounded strokes to sit next to Material Symbols Rounded.
 */
object MarkorIcons {
    /** A note with a second one peeking out behind it: "all your notes". */
    val AllNotes: ImageVector by lazy {
        icon("all_notes") {
            stroked {
                // Back note: only its top and right edges show.
                moveTo(8f, 6.5f)
                lineTo(8f, 5.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 10.5f, 3f)
                lineTo(17.5f, 3f)
                arcTo(2.5f, 2.5f, 0f, false, true, 20f, 5.5f)
                lineTo(20f, 13.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 17.5f, 16f)
                lineTo(16.5f, 16f)
            }
            stroked {
                roundRect(4f, 7f, 16f, 21f, 2.5f)
                moveTo(7.5f, 12f)
                lineTo(12.5f, 12f)
                moveTo(7.5f, 16f)
                lineTo(10.5f, 16f)
            }
        }
    }

    /** Selected variant: the front note is solid with its text lines cut out. */
    val AllNotesFilled: ImageVector by lazy {
        icon("all_notes_filled") {
            stroked {
                moveTo(8f, 6.5f)
                lineTo(8f, 5.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 10.5f, 3f)
                lineTo(17.5f, 3f)
                arcTo(2.5f, 2.5f, 0f, false, true, 20f, 5.5f)
                lineTo(20f, 13.5f)
                arcTo(2.5f, 2.5f, 0f, false, true, 17.5f, 16f)
                lineTo(16.5f, 16f)
            }
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                roundRect(3f, 6f, 17f, 22f, 3f)
                roundRect(6.5f, 11f, 13.5f, 13f, 1f)
                roundRect(6.5f, 15f, 11.5f, 17f, 1f)
            }
        }
    }

    /** Uneven tiles, like the staggered note grid it switches to. */
    val MasonryGrid: ImageVector by lazy {
        icon("masonry_grid") {
            stroked {
                roundRect(4f, 4f, 10.5f, 13f, 2f)
                roundRect(4f, 16f, 10.5f, 20f, 2f)
                roundRect(13.5f, 4f, 20f, 8f, 2f)
                roundRect(13.5f, 11f, 20f, 20f, 2f)
            }
        }
    }

    /** Full-width stacked cards, like the list it switches to. */
    val CardList: ImageVector by lazy {
        icon("card_list") {
            stroked {
                roundRect(4f, 4f, 20f, 10.5f, 2f)
                roundRect(4f, 13.5f, 20f, 20f, 2f)
            }
        }
    }

    /** Shrinking bars with a down arrow: "order by". */
    val Sort: ImageVector by lazy {
        icon("sort_order") {
            stroked {
                moveTo(4f, 6f)
                lineTo(12f, 6f)
                moveTo(4f, 12f)
                lineTo(10f, 12f)
                moveTo(4f, 18f)
                lineTo(8f, 18f)
                moveTo(17f, 5f)
                lineTo(17f, 19f)
                moveTo(13.5f, 15.5f)
                lineTo(17f, 19f)
                lineTo(20.5f, 15.5f)
            }
        }
    }
}

private inline fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

private inline fun ImageVector.Builder.stroked(block: PathBuilder.() -> Unit) {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block,
    )
}

private fun PathBuilder.roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
    moveTo(left + radius, top)
    lineTo(right - radius, top)
    arcTo(radius, radius, 0f, false, true, right, top + radius)
    lineTo(right, bottom - radius)
    arcTo(radius, radius, 0f, false, true, right - radius, bottom)
    lineTo(left + radius, bottom)
    arcTo(radius, radius, 0f, false, true, left, bottom - radius)
    lineTo(left, top + radius)
    arcTo(radius, radius, 0f, false, true, left + radius, top)
    close()
}
