package com.gantree.cab.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.gantree.theme.GantreeThemes
import androidx.compose.ui.unit.sp
import com.gantree.cab.mailbox.chatSp
import com.gantree.cab.mailbox.parseFont

data class CabColors(
  val canvas: Color,
  val panel: Color,
  val track: Color,
  val line: Color,
  val edge: Color,
  val fg: Color,
  val body: Color,
  val muted: Color,
  val dim: Color,
  val accent: Color,
  val mark: Color,
  val accentLine: Color,
  val accentSoft: Color,
  val danger: Color,
  val ok: Color,
  val you: Color,
  val kit: Color,
  val scheme: String = "dark",
)

val LocalCabColors = compositionLocalOf { boomColors() }

fun cabColors(themeId: String): CabColors {
  val t = GantreeThemes.of(themeId)
  return CabColors(
    canvas = t.canvas,
    panel = t.panel,
    track = t.track,
    line = t.line,
    edge = t.edge,
    fg = t.fg,
    body = t.body,
    muted = t.muted,
    dim = t.dim,
    accent = t.accent,
    mark = t.mark,
    accentLine = t.accentLine,
    accentSoft = t.accentSoft,
    danger = t.danger,
    ok = t.ok,
    you = t.you,
    kit = t.kit,
    scheme = t.scheme,
  )
}

fun boomColors(): CabColors = cabColors(GantreeThemes.DEFAULT)

fun CabColors.toColorScheme() = if (scheme == "light") {
  lightColorScheme(
    primary = accent,
    onPrimary = canvas,
    primaryContainer = accentSoft,
    onPrimaryContainer = mark,
    secondary = ok,
    onSecondary = canvas,
    secondaryContainer = track,
    onSecondaryContainer = ok,
    tertiary = ok,
    onTertiary = canvas,
    background = canvas,
    onBackground = body,
    surface = panel,
    onSurface = fg,
    surfaceVariant = track,
    onSurfaceVariant = muted,
    surfaceContainerLowest = canvas,
    surfaceContainerLow = panel,
    surfaceContainer = track,
    surfaceContainerHigh = line,
    surfaceContainerHighest = kit,
    outline = edge,
    outlineVariant = line,
    error = danger,
    onError = fg,
    inversePrimary = accentLine,
  )
} else {
  darkColorScheme(
    primary = accent,
    onPrimary = canvas,
    primaryContainer = accentSoft,
    onPrimaryContainer = mark,
    secondary = ok,
    onSecondary = canvas,
    secondaryContainer = track,
    onSecondaryContainer = ok,
    tertiary = ok,
    onTertiary = canvas,
    background = canvas,
    onBackground = body,
    surface = panel,
    onSurface = fg,
    surfaceVariant = track,
    onSurfaceVariant = muted,
    surfaceContainerLowest = canvas,
    surfaceContainerLow = panel,
    surfaceContainer = track,
    surfaceContainerHigh = line,
    surfaceContainerHighest = kit,
    outline = edge,
    outlineVariant = line,
    error = danger,
    onError = fg,
    inversePrimary = accentLine,
  )
}

private fun cabTypography(chat: Float): Typography {
  val base = Typography()
  return Typography(
    displayLarge = base.displayLarge.copy(fontFamily = CabSans),
    displayMedium = base.displayMedium.copy(fontFamily = CabSans),
    displaySmall = base.displaySmall.copy(fontFamily = CabSans),
    headlineLarge = base.headlineLarge.copy(fontFamily = CabSans),
    headlineMedium = base.headlineMedium.copy(fontFamily = CabSans),
    headlineSmall = base.headlineSmall.copy(fontFamily = CabSans),
    titleLarge = base.titleLarge.copy(fontFamily = CabSans),
    titleMedium = base.titleMedium.copy(fontFamily = CabSans),
    titleSmall = base.titleSmall.copy(fontFamily = CabSans),
    bodyLarge = base.bodyLarge.copy(fontFamily = CabSans, fontSize = chat.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = CabSans),
    bodySmall = base.bodySmall.copy(fontFamily = CabSans),
    labelLarge = base.labelLarge.copy(fontFamily = CabSans),
    labelMedium = base.labelMedium.copy(fontFamily = CabSans),
    labelSmall = base.labelSmall.copy(fontFamily = CabSans),
  )
}

@Composable
fun CabTheme(themeId: String = "boom", fontId: String = "sm", content: @Composable () -> Unit) {
  val colors = cabColors(themeId)
  CompositionLocalProvider(LocalCabColors provides colors) {
    MaterialTheme(
      colorScheme = colors.toColorScheme(),
      typography = cabTypography(chatSp(parseFont(fontId))),
      content = content,
    )
  }
}
