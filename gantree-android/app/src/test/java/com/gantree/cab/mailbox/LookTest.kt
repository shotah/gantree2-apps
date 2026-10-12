package com.gantree.cab.mailbox

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LookTest {
  @Test
  fun themeFallsBackToBoom() {
    val tokens = JSONObject(File("../../shared/theme/tokens.json").readText())
    val themes = tokens.getJSONArray("themes")
    val ids = List(themes.length()) { i -> themes.getJSONObject(i).getString("id") }
    assertEquals(tokens.getString("default"), DEFAULT_THEME)
    assertEquals(ids, THEME_IDS)
    assertEquals(THEME_PLAIN + THEME_MOODS, THEME_IDS)
    assertEquals(DEFAULT_THEME, parseTheme(null))
    assertEquals("boom", parseTheme("nope"))
    assertEquals("boom", parseTheme("noir"))
    assertEquals("boom", parseTheme("lamp"))
    assertEquals("siren", parseTheme("siren"))
    assertEquals("lemonade", parseTheme("lemonade"))
    assertEquals("paper", parseTheme("paper"))
    assertEquals("ink", knownTheme("ink"))
    assertEquals("marquee", knownTheme("marquee"))
    assertEquals(null, knownTheme("noir"))
    assertEquals(null, knownTheme("nope"))
  }

  @Test
  fun followPaintsTheRoomThemeUntilItIsCleared() {
    assertEquals("siren", paintedTheme(true, "siren", "boom"))
    assertEquals("boom", paintedTheme(true, "", "boom"))
    assertEquals("boom", paintedTheme(true, "nope", "boom"))
    assertEquals("paper", paintedTheme(true, "noir", "paper"))
    assertEquals("boom", paintedTheme(true, "lamp", "ember"))
    assertEquals("rain", paintedTheme(false, "siren", "rain"))
  }

  @Test
  fun fetchedThemeKeepsALiveNoticeOverAStaleGet() {
    assertEquals("rain", fetchedRoomTheme("siren", "rain", "boom"))
    assertEquals("rain", fetchedRoomTheme("", "rain", ""))
    assertEquals("marquee", fetchedRoomTheme("siren", "siren", "marquee"))
    assertEquals("", fetchedRoomTheme("siren", "siren", ""))
  }

  @Test
  fun fontFallsBackToSmall() {
    assertEquals(DEFAULT_FONT, parseFont(null))
    assertEquals("sm", parseFont("nope"))
    assertEquals("lg", parseFont("lg"))
    assertEquals(listOf("sm", "md", "lg", "xl"), FONT_IDS)
    assertEquals(14f, chatSp("sm"), 0.01f)
    assertEquals(16f, chatSp("md"), 0.01f)
    assertEquals(20f, chatSp("lg"), 0.01f)
    assertEquals(24f, chatSp("xl"), 0.01f)
    assertEquals(14f, chatSp("nope"), 0.01f)
  }

  @Test
  fun labelsMatchGantree() {
    assertEquals("Boom", themeLabel("boom"))
    assertEquals("Boom", themeLabel("nope"))
    assertEquals("Boom", themeLabel("noir"))
    assertEquals("Paper", themeLabel("paper"))
    assertEquals("Ink", themeLabel("ink"))
    assertEquals("Marquee", themeLabel("marquee"))
    assertEquals("Lemonade", themeLabel("lemonade"))
    assertEquals("Flicker", themeLabel("flicker"))
    assertEquals("Small", fontLabel("sm"))
    assertEquals("Medium", fontLabel("md"))
    assertEquals("Large", fontLabel("lg"))
    assertEquals("Extra large", fontLabel("xl"))
  }
}
