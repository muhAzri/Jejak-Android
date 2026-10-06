package com.muhazri.jejak.core.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.muhazri.jejak.R

/** Heroicons v2 solid, ported from the iOS asset catalog. 24px variants unless noted. */
enum class HeroIcon(@param:DrawableRes val res: Int) {
    ShieldCheck(R.drawable.ic_shield_check),
    User(R.drawable.ic_user),
    SignalSlash(R.drawable.ic_signal_slash),
    LockClosed(R.drawable.ic_lock_closed),
    InformationCircle(R.drawable.ic_information_circle),
    MapPin(R.drawable.ic_map_pin),
    Cog(R.drawable.ic_cog_6_tooth),
    Flag(R.drawable.ic_flag),
    ExclamationTriangle(R.drawable.ic_exclamation_triangle),
    XMark(R.drawable.ic_x_mark),
    ChevronLeft(R.drawable.ic_chevron_left),
    Language(R.drawable.ic_language),
    Pause(R.drawable.ic_pause),
    Play(R.drawable.ic_play),
    Stop(R.drawable.ic_stop),

    // 20px
    ArrowRight(R.drawable.ic_arrow_right),
    Bolt(R.drawable.ic_bolt),
    ChevronRight(R.drawable.ic_chevron_right),
    GlobeAsiaAustralia(R.drawable.ic_globe_asia_australia),
    Signal(R.drawable.ic_signal),
}

/** A heroicon at an exact size, tinted like the SwiftUI `Image(heroicon:).foregroundStyle(...)`. */
@Composable
fun HeroIconImage(
    icon: HeroIcon,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(icon.res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size),
    )
}
