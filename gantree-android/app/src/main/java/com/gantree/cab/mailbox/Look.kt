package com.gantree.cab.mailbox

import com.gantree.theme.GantreeThemes

/**
 * Catalog order from [GantreeThemes]. Plain is neutral `feel`;
 * moods are the rest. A stored id that is no longer here falls to boom.
 */
val THEME_PLAIN: List<String> =
  GantreeThemes.all.values.filter { it.feel == "neutral" }.map { it.id }

val THEME_MOODS: List<String> =
  GantreeThemes.all.values.filter { it.feel != "neutral" }.map { it.id }

val THEME_IDS: List<String> = GantreeThemes.ids
const val DEFAULT_THEME = GantreeThemes.DEFAULT

val FONT_IDS = listOf("sm", "md", "lg", "xl")
const val DEFAULT_FONT = "sm"

fun knownTheme(v: String?): String? =
  if (v != null && v in THEME_IDS) v else null

fun parseTheme(v: String?): String = GantreeThemes.of(v).id

fun parseFont(v: String?): String =
  if (v != null && v in FONT_IDS) v else DEFAULT_FONT

fun chatSp(fontId: String): Float = when (parseFont(fontId)) {
  "md" -> 16f
  "lg" -> 20f
  "xl" -> 24f
  else -> 14f
}

fun themeLabel(id: String): String = parseTheme(id).replaceFirstChar { it.uppercase() }

fun fontLabel(id: String): String = when (parseFont(id)) {
  "md" -> "Medium"
  "lg" -> "Large"
  "xl" -> "Extra large"
  else -> "Small"
}

/**
 * What to paint. Follow Kit when the room has a known id; empty / junk / follow-off
 * keeps the human's pick. Same rule as gantree `THEME_BOOT`.
 */
fun paintedTheme(follow: Boolean, roomTheme: String, mine: String): String {
  if (!follow) {
    return parseTheme(mine)
  }
  return knownTheme(roomTheme) ?: parseTheme(mine)
}

/**
 * GET `/api/theme` vs a live notice. If the room id changed while GET was in
 * flight, keep the notice so Kit's mood is not overwritten by a stale body.
 */
fun fetchedRoomTheme(before: String, live: String, got: String): String =
  if (live != before) live else got
