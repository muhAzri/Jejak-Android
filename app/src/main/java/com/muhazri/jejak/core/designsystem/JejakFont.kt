package com.muhazri.jejak.core.designsystem

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.muhazri.jejak.R

/** Typography scale from the F0 design. Display = Mochiy Pop One, body = Plus Jakarta Sans. */
object JejakFont {

    val Display = FontFamily(Font(R.font.mochiy_pop_one_regular, FontWeight.Normal))

    val Body = FontFamily(
        Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
        Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    )

    /** Display face at an arbitrary size; the big readouts scale with the layout. */
    fun display(size: TextUnit): TextStyle = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Normal,
        fontSize = size,
        lineHeight = size * 1.2f,
    )

    /** h1 · page title (display face). */
    val h1 = display(18.sp)

    /** h2 · sheet and modal titles (display face). */
    val h2Display = display(16.sp)

    val h2 = body(FontWeight.Bold, 16.sp)
    val p1 = body(FontWeight.Normal, 14.sp)
    val p1Semibold = body(FontWeight.SemiBold, 14.sp)
    val p1Bold = body(FontWeight.Bold, 14.sp)
    val p2 = body(FontWeight.Normal, 13.sp)
    val p2Semibold = body(FontWeight.SemiBold, 13.sp)
    val p2Bold = body(FontWeight.Bold, 13.sp)
    val p3 = body(FontWeight.Normal, 12.sp)
    val p3Semibold = body(FontWeight.SemiBold, 12.sp)
    val p3Bold = body(FontWeight.Bold, 12.sp)

    private fun body(weight: FontWeight, size: TextUnit): TextStyle = TextStyle(
        fontFamily = Body,
        fontWeight = weight,
        fontSize = size,
        lineHeight = size * 1.4f,
    )
}
