package com.king0929zion.openarchive.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Icon paths copied from the single-file Archive HTML prototype.
 *
 * The prototype uses a 24 x 24 stroke icon language with round caps/joins.
 * Keeping the vectors here avoids Material icon drift and any runtime SVG parsing.
 */
object ArchiveIcons {
    val Add: ImageVector by lazy { strokeIcon("Add", p("M12 5v14"), p("M5 12h14")) }
    val Back: ImageVector by lazy { strokeIcon("Back", p("M15 18l-6-6 6-6")) }
    val ChevronRight: ImageVector by lazy { strokeIcon("ChevronRight", p("M9 18l6-6-6-6")) }
    val Search: ImageVector by lazy { strokeIcon("Search", p(circle(11f, 11f, 7f)), p("M20 20l-3.5-3.5")) }
    val Close: ImageVector by lazy { strokeIcon("Close", p("M18 6 6 18"), p("M6 6l12 12")) }
    val Check: ImageVector by lazy { strokeIcon("Check", p("M20 6 9 17l-5-5")) }

    val Photo: ImageVector by lazy {
        strokeIcon(
            "Photo",
            p("M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z"),
            p(circle(9f, 9f, 2f)),
            p("m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"),
        )
    }
    val Location: ImageVector by lazy {
        strokeIcon("Location", p("M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"), p(circle(12f, 10f, 3f)))
    }
    val Sun: ImageVector by lazy {
        strokeIcon(
            "Sun",
            p(circle(12f, 12f, 4f)), p("M12 2v2"), p("M12 20v2"), p("m4.93 4.93 1.41 1.41"),
            p("m17.66 17.66 1.41 1.41"), p("M2 12h2"), p("M20 12h2"),
            p("m6.34 17.66-1.41 1.41"), p("m19.07 4.93-1.41 1.41"),
        )
    }
    val CloudSun: ImageVector by lazy {
        strokeIcon(
            "CloudSun",
            p("M12 2v2"), p("m4.93 4.93 1.41 1.41"), p("M20 12h2"), p("m19.07 4.93-1.41 1.41"),
            p("M15.947 12.65a4 4 0 0 0-5.925-4.128"),
            p("M13 22H7a5 5 0 1 1 4.9-6H13a3 3 0 0 1 0 6Z"),
        )
    }
    val Cloud: ImageVector by lazy { strokeIcon("Cloud", p("M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z")) }
    val CloudRain: ImageVector by lazy {
        strokeIcon("CloudRain", p("M4 14.899A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.5 8.242"), p("M16 14v6"), p("M8 14v6"), p("M12 16v6"))
    }
    val Snowflake: ImageVector by lazy {
        strokeIcon(
            "Snowflake", p("M2 12h20"), p("M12 2v20"), p("m20 16-4-4 4-4"), p("m4 8 4 4-4 4"),
            p("m16 4-4 4-4-4"), p("m8 20 4-4 4 4"),
        )
    }
    val Smile: ImageVector by lazy {
        strokeIcon("Smile", p(circle(12f, 12f, 10f)), p("M8 14s1.5 2 4 2 4-2 4-2"), p("M9 9h.01"), p("M15 9h.01"))
    }
    val Leaf: ImageVector by lazy {
        strokeIcon("Leaf", p("M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"), p("M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"))
    }
    val Zap: ImageVector by lazy { strokeIcon("Zap", p("M13 2 3 14h9l-1 8 10-12h-9l1-8Z")) }
    val Moon: ImageVector by lazy { strokeIcon("Moon", p("M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z")) }
    val Coffee: ImageVector by lazy {
        strokeIcon("Coffee", p("M17 8h1a4 4 0 1 1 0 8h-1"), p("M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z"), p("M6 2v2"), p("M10 2v2"), p("M14 2v2"))
    }

    val Achi: ImageVector by lazy {
        strokeIcon(
            "Achi",
            p("M12.3 3.1c4.8.1 8.6 4 8.6 8.9 0 4.9-4 8.9-8.9 8.9-4.8 0-8.7-3.9-8.9-8.7-.06-2.4.86-4.7 2.5-6.3", 2.2f),
            p("M5.9 6.2c1.7-1.9 4-3 6.4-3.1", 2.2f),
            p("M9.3 9.1v3.7", 2.6f),
            p("M14.7 8.9v3.7", 2.6f),
        )
    }
    val Album: ImageVector get() = Photo
    val Stats: ImageVector by lazy { strokeIcon("Stats", p("M3 3v16a2 2 0 0 0 2 2h16"), p("M18 17V9"), p("M13 17V5"), p("M8 17v-3")) }
    val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            p("M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2Z"),
            p(circle(12f, 12f, 3f)),
        )
    }
    val Server: ImageVector by lazy {
        strokeIcon("Server", p("M4 2h16a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Z"), p("M4 14h16a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2Z"), p("M6 6h.01"), p("M6 18h.01"))
    }

    val More: ImageVector by lazy { filledIcon("More", circle(12f, 5f, 1.7f), circle(12f, 12f, 1.7f), circle(12f, 19f, 1.7f)) }
    val Share: ImageVector by lazy {
        strokeIcon("Share", p(circle(18f, 5f, 3f)), p(circle(6f, 12f, 3f)), p(circle(18f, 19f, 3f)), p("m8.6 10.7 6.8-4.1"), p("m8.6 13.3 6.8 4.1"))
    }
    val Delete: ImageVector by lazy { strokeIcon("Delete", p("M3 6h18"), p("M8 6V4h8v2"), p("M19 6l-1 14H6L5 6"), p("M10 11v5"), p("M14 11v5")) }
    val Send: ImageVector by lazy {
        strokeIcon("Send", p("M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11Z"), p("m21.854 2.147-10.94 10.939"))
    }
    val Reply: ImageVector by lazy { strokeIcon("Reply", p("M9 14 4 9l5-5"), p("M20 20v-7a4 4 0 0 0-4-4H4")) }
    val Refresh: ImageVector by lazy { strokeIcon("Refresh", p("M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"), p("M21 3v5h-5"), p("M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"), p("M8 16H3v5")) }

    private data class StrokePath(val data: String, val width: Float)
    private fun p(data: String, width: Float = 2f) = StrokePath(data, width)

    private fun strokeIcon(name: String, vararg paths: StrokePath): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                paths.forEach { path ->
                    addPath(
                        pathData = addPathNodes(path.data),
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = path.width,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    )
                }
            }
            .build()

    private fun filledIcon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                paths.forEach { data -> addPath(pathData = addPathNodes(data), fill = SolidColor(Color.Black)) }
            }
            .build()

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} $cy a$r $r 0 1 0 ${r * 2} 0 a$r $r 0 1 0 ${-r * 2} 0"
}
