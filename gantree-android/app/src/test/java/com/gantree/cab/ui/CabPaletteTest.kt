package com.gantree.cab.ui

import androidx.compose.ui.graphics.Color
import com.gantree.cab.mailbox.THEME_IDS
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class CabPaletteTest {
  private val tokens = JSONObject(File("../../shared/theme/tokens.json").readText())

  @Test
  fun everyThemeMatchesTokensAndJunkFallsToBoom() {
    val themes = tokens.getJSONArray("themes")
    val ids = List(themes.length()) { i -> themes.getJSONObject(i).getString("id") }
    assertEquals(ids, THEME_IDS)
    assertEquals("boom", tokens.getString("default"))
    for (i in 0 until themes.length()) {
      val theme = themes.getJSONObject(i)
      val id = theme.getString("id")
      val tok = theme.getJSONObject("tokens")
      val colors = cabColors(id)
      assertEquals("$id canvas", hex(tok.getString("canvas")), colors.canvas)
      assertEquals("$id panel", hex(tok.getString("panel")), colors.panel)
      assertEquals("$id track", hex(tok.getString("track")), colors.track)
      assertEquals("$id line", hex(tok.getString("line")), colors.line)
      assertEquals("$id edge", hex(tok.getString("edge")), colors.edge)
      assertEquals("$id fg", hex(tok.getString("fg")), colors.fg)
      assertEquals("$id body", hex(tok.getString("body")), colors.body)
      assertEquals("$id muted", hex(tok.getString("muted")), colors.muted)
      assertEquals("$id dim", hex(tok.getString("dim")), colors.dim)
      assertEquals("$id accent", hex(tok.getString("accent")), colors.accent)
      assertEquals("$id mark", hex(tok.getString("mark")), colors.mark)
      assertEquals("$id accentLine", hex(tok.getString("accentLine")), colors.accentLine)
      assertEquals("$id accentSoft", hex(tok.getString("accentSoft")), colors.accentSoft)
      assertEquals("$id danger", hex(tok.getString("danger")), colors.danger)
      assertEquals("$id ok", hex(tok.getString("ok")), colors.ok)
      assertEquals("$id you", hex(tok.getString("you")), colors.you)
      assertEquals("$id kit", hex(tok.getString("kit")), colors.kit)
      assertEquals("$id scheme", tok.getString("scheme"), colors.scheme)
      if (tok.getString("kit") == tok.getString("track")) {
        assertEquals("$id kit is track", colors.track, colors.kit)
      }
    }
    assertEquals(cabColors("boom").accent, cabColors("nope").accent)
    assertEquals(cabColors("boom").canvas, cabColors("noir").canvas)
    assertEquals(cabColors("boom").canvas, cabColors("lamp").canvas)
    assertEquals(cabColors("boom").canvas, boomColors().canvas)
    assertEquals(cabColors("boom").accent, boomColors().toColorScheme().primary)
    assertEquals(cabColors("boom").ok, boomColors().toColorScheme().tertiary)
    assertEquals(cabColors("boom").line, boomColors().toColorScheme().outlineVariant)
    assertEquals(cabColors("paper").line, cabColors("paper").toColorScheme().outlineVariant)
  }
}

private fun hex(raw: String): Color {
  val h = raw.removePrefix("#")
  return Color(0xFF000000 or h.toLong(16))
}
