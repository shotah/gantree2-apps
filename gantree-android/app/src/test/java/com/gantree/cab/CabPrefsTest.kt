package com.gantree.cab

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Legacy `cab` prefs copy into `gantree` once. Keys stay short. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CabPrefsTest {
  private val ctx: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun copiesCabPrefsIntoGantreeAndLeavesCabAlone() {
    val cab = ctx.getSharedPreferences("cab", Context.MODE_PRIVATE)
    cab.edit()
      .putString("origin", "https://gantree.example")
      .putString("slug", "kit")
      .putString("email", "ada@example.com")
      .putLong("session_exp", 9L)
      .putString("gps", "on")
      .commit()

    val prefs = CabPrefs(ctx)
    assertEquals("https://gantree.example", prefs.origin)
    assertEquals("kit", prefs.slug)
    assertEquals("ada@example.com", prefs.email)
    assertEquals(9L, prefs.sessionExp)
    assertEquals(true, prefs.gps)

    val gantree = ctx.getSharedPreferences("gantree", Context.MODE_PRIVATE)
    assertEquals("https://gantree.example", gantree.getString("origin", null))
    assertEquals("kit", gantree.getString("slug", null))
    assertEquals("ada@example.com", cab.getString("email", null))

    cab.edit().putString("slug", "dock").commit()
    assertEquals("kit", CabPrefs(ctx).slug)
  }

  @Test
  fun skipsMigrationWhenGantreeAlreadyHasOrigin() {
    ctx.getSharedPreferences("gantree", Context.MODE_PRIVATE)
      .edit()
      .putString("origin", "https://kept.example")
      .commit()
    ctx.getSharedPreferences("cab", Context.MODE_PRIVATE)
      .edit()
      .putString("origin", "https://old.example")
      .putString("slug", "dock")
      .commit()

    val prefs = CabPrefs(ctx)
    assertEquals("https://kept.example", prefs.origin)
    assertEquals("kit", prefs.slug)
    assertFalse(ctx.getSharedPreferences("gantree", Context.MODE_PRIVATE).contains("slug"))
  }

  @Test
  fun skipsMigrationWhenGantreeAlreadyHasSlug() {
    ctx.getSharedPreferences("gantree", Context.MODE_PRIVATE)
      .edit()
      .putString("slug", "kit")
      .commit()
    ctx.getSharedPreferences("cab", Context.MODE_PRIVATE)
      .edit()
      .putString("origin", "https://old.example")
      .commit()

    CabPrefs(ctx)
    assertFalse(ctx.getSharedPreferences("gantree", Context.MODE_PRIVATE).contains("origin"))
  }
}
