package com.gantree.cab.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.gantree.cab.mailbox.DEFAULT_FONT
import com.gantree.cab.mailbox.DEFAULT_LANG
import com.gantree.cab.mailbox.DEFAULT_PHOTO_SIZE
import com.gantree.cab.mailbox.DEFAULT_THEME
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The closed-set settings are dropdowns: one field each, the choices under it, a pick reports the id. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CabSettingsTest {
  @get:Rule
  val compose = createComposeRule()

  private val picked = mutableListOf<Pair<String, String>>()

  private fun settings(
    voiceOffered: Boolean = true,
    followTheme: Boolean = true,
    kitTheme: String = "",
    email: String = "",
    agents: List<String> = emptyList(),
    sub: String = "",
    slug: String = "kit",
    onSlug: (String) -> Unit = {},
  ) {
    compose.setContent {
      CabTheme(themeId = DEFAULT_THEME, fontId = DEFAULT_FONT) {
        CabSettings(
          origin = "https://gantree.example",
          slug = slug,
          spike = "",
          email = email,
          googleReady = false,
          agents = agents,
          sub = sub,
          themeId = DEFAULT_THEME,
          fontId = DEFAULT_FONT,
          onOrigin = {},
          onSlug = onSlug,
          onSpike = {},
          onTheme = { picked += "theme" to it },
          onFont = { picked += "font" to it },
          onConnect = {},
          onGoogle = {},
          onSignOut = {},
          photoSizeId = DEFAULT_PHOTO_SIZE,
          onPhotoSize = { picked += "photo" to it },
          voiceOffered = voiceOffered,
          langId = DEFAULT_LANG,
          onLang = { picked += "lang" to it },
          followTheme = followTheme,
          kitTheme = kitTheme,
        )
      }
    }
  }

  /** The drawer scrolls and the test screen is short: bring the field up, then tap it open. */
  private fun open(label: String) {
    compose.onNodeWithContentDescription(label).performScrollTo().performClick()
  }

  @Test
  fun eachPickerIsOneFieldShowingTheCurrentChoice() {
    settings()
    compose.onNodeWithContentDescription("Language").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("English").assertIsDisplayed()
    compose.onNodeWithContentDescription("Theme").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Boom").assertIsDisplayed()
    compose.onNodeWithContentDescription("Font size").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Small").assertIsDisplayed()
    compose.onNodeWithContentDescription("Photo size").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Medium · 1024 px").assertIsDisplayed()
  }

  @Test
  fun openingThemeListsPlainAndMoodsAndAPickReportsTheId() {
    settings(kitTheme = "paper")
    open("Theme")
    compose.onNodeWithText("Plain").assertIsDisplayed()
    compose.onNodeWithText("Moods").assertIsDisplayed()
    compose.onNodeWithContentDescription("Boom").assertIsSelected()
    compose.onNodeWithContentDescription("Paper, Kit").assertIsDisplayed()
    compose.onNodeWithContentDescription("Marquee").performClick()
    assertEquals(listOf("theme" to "marquee"), picked)
    compose.onNodeWithContentDescription("Lemonade").assertDoesNotExist()
  }

  @Test
  fun kitTagHidesWhenFollowIsOff() {
    settings(followTheme = false, kitTheme = "paper")
    open("Theme")
    compose.onNodeWithContentDescription("Paper").assertIsDisplayed()
    compose.onNodeWithContentDescription("Paper, Kit").assertDoesNotExist()
  }

  @Test
  fun fontLanguageAndPhotoPickTheirIds() {
    settings()
    open("Font size")
    compose.onNodeWithContentDescription("Extra large").performClick()
    open("Language")
    compose.onNodeWithContentDescription("日本語 · Japanese").performClick()
    open("Language")
    compose.onNodeWithContentDescription("Tiếng Việt · Vietnamese").performClick()
    open("Photo size")
    compose.onNodeWithContentDescription("Small · 640 px").performClick()
    assertEquals(listOf("font" to "xl", "lang" to "ja", "lang" to "vi", "photo" to "small"), picked)
  }

  @Test
  fun noAgentsShowsTheGoogleSubToCopy() {
    settings(email = "ada@example.com", sub = "118000")
    compose.onNodeWithText("Not on any agent yet — give this to your gantree admin")
      .performScrollTo()
      .assertIsDisplayed()
    compose.onNodeWithContentDescription("google sub").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("118000").assertIsDisplayed()
    compose.onNodeWithText("Copy").performScrollTo().performClick()
    compose.onNodeWithText("Copied").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun strangerWithNoEmailStillShowsTheSub() {
    settings(sub = "118000")
    compose.onNodeWithText("Not on any agent yet — give this to your gantree admin")
      .performScrollTo()
      .assertIsDisplayed()
    compose.onNodeWithContentDescription("google sub").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Copy").performScrollTo().performClick()
    compose.onNodeWithText("Copied").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun talkingToCommitsTheSlugOnDone() {
    val slugs = mutableListOf<String>()
    settings(slug = "evie", onSlug = { slugs += it })
    compose.onNodeWithText("evie").performScrollTo().performTextReplacement("tim")
    compose.onNodeWithText("tim").performImeAction()
    assertEquals(listOf("tim"), slugs)
  }

  @Test
  fun agentChipSelectsThatSlug() {
    val slugs = mutableListOf<String>()
    settings(slug = "evie", agents = listOf("evie", "tim"), onSlug = { slugs += it })
    compose.onNodeWithText("Tim").performScrollTo().performClick()
    assertEquals(listOf("tim"), slugs)
  }

  @Test
  fun agentsHideTheCopyBlock() {
    settings(email = "ada@example.com", agents = listOf("kit"), sub = "118000")
    compose.onNodeWithText("Not on any agent yet — give this to your gantree admin").assertDoesNotExist()
  }

  @Test
  fun languageOnlyShowsWhenTheWorkerPublishesVoice() {
    settings(voiceOffered = false)
    compose.onNodeWithContentDescription("Theme").performScrollTo().assertIsDisplayed()
    compose.onNodeWithContentDescription("Language").assertDoesNotExist()
  }
}
