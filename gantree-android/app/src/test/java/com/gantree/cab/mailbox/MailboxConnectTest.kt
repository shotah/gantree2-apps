package com.gantree.cab.mailbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MailboxConnectTest {
  @Test
  fun connectNeedsASlugAndABearer() {
    assertEquals("Talking to needs an agent slug like kit.", mailboxConnectError("Kit!", "tok"))
    assertTrue(mailboxConnectError("kit", "")!!.contains("Google session"))
    assertNull(mailboxConnectError("kit", "jwe"))
  }

  @Test
  fun socketHintCallsOutRoomListOnForbidden() {
    val hint = mailboxSocketHint(403, "forbidden")
    assertTrue(hint.contains("HTTP 403"))
    assertTrue(hint.contains("room list"))
  }

  @Test
  fun socketHintKeepsTransportDetailWhenThereIsNoHttp() {
    assertEquals(
      "Mailbox socket down — failed to connect",
      mailboxSocketHint(null, "failed to connect"),
    )
  }

  @Test
  fun signedInWithNoAgentsIsNotLive() {
    val hint = mailboxSignedInHint("ada@example.com", emptyList())
    assertTrue(hint.contains("ada@example.com"))
    assertTrue(hint.contains("no agents"))
    assertEquals("Signed in as ada@example.com", mailboxSignedInHint("ada@example.com", listOf("kit")))
  }

  @Test
  fun notAllowedHintNamesTheEmailOrSkipsIt() {
    assertEquals(
      "Google worked (ada@example.com), but you\u2019re not on the gantree list yet. Copy your id below and send it to your gantree admin.",
      mailboxNotAllowedHint("ada@example.com"),
    )
    assertEquals(
      "Google worked, but you\u2019re not on the gantree list yet. Copy your id below and send it to your gantree admin.",
      mailboxNotAllowedHint(""),
    )
    assertEquals(mailboxNotAllowedHint(""), mailboxNotAllowedHint("   "))
  }

  @Test
  fun describeAuthErrorNamesTheRefusal() {
    val denied = describeAuthError(403, """{"error":"not_allowed","sub":"118000","email":"ada@example.com"}""")
    assertEquals(NotAllowed("118000", "ada@example.com"), denied.notAllowed)
    assertEquals(mailboxNotAllowedHint("ada@example.com"), denied.hint)
    val stranger = describeAuthError(403, """{"error":"not_allowed","sub":"118000","email":null}""")
    assertEquals(NotAllowed("118000", null), stranger.notAllowed)
    assertEquals(mailboxNotAllowedHint(""), stranger.hint)
    assertEquals(
      "Sign-in was refused (nonce) \u2014 try again.",
      describeAuthError(401, """{"error":"nonce"}""").hint,
    )
    assertEquals(
      "Sign-in was refused (token) \u2014 try again.",
      describeAuthError(401, """{"error":"token"}""").hint,
    )
    assertNull(describeAuthError(401, """{"error":"nonce"}""").notAllowed)
    assertEquals(
      "Sign-in was refused \u2014 try again.",
      describeAuthError(401, "nope").hint,
    )
    assertEquals(
      "gantree has no Google login configured yet.",
      describeAuthError(503, """{"error":"not_configured"}""").hint,
    )
    val bad = describeAuthError(400, """{"error":"bad_request"}""")
    assertNull(bad.notAllowed)
    assertEquals("Sign-in failed (HTTP 400).", bad.hint)
  }

  @Test
  fun allowlistCopyIsEmailThenSub() {
    assertEquals("ada@example.com\n1182", allowlistCopy("ada@example.com", "1182"))
    assertEquals("1182", allowlistCopy("  ", "1182"))
    assertEquals("", allowlistCopy("", ""))
  }

  @Test
  fun timeoutHintIsASentence() {
    assertTrue(mailboxTimeoutHint().contains("timed out"))
  }

  @Test
  fun retryStopsOnAuthAndNotFound() {
    assertFalse(mailboxShouldRetry(401))
    assertFalse(mailboxShouldRetry(403))
    assertFalse(mailboxShouldRetry(404))
    assertFalse(mailboxShouldRetry(4401))
    assertTrue(mailboxShouldRetry(null))
    assertTrue(mailboxShouldRetry(500))
  }

  @Test
  fun sessionDropsOnHandshake401AndClose4401NotOn403() {
    assertTrue(mailboxHttpDropsSession(401))
    assertFalse(mailboxHttpDropsSession(403))
    assertFalse(mailboxHttpDropsSession(404))
    assertFalse(mailboxHttpDropsSession(null))
    assertTrue(mailboxCloseDropsAuth(MAILBOX_CLOSE_UNAUTHORIZED))
    assertFalse(mailboxCloseDropsAuth(1000))
    assertFalse(mailboxCloseDropsAuth(1001))
    assertTrue(mailboxAuthLostHint().contains("sign in again"))
  }

  @Test
  fun sweepOnlyWhileSomeoneWatchesAndNotBackToBack() {
    assertTrue(watchingThread(phoneResumed = true, carThreadVisible = false))
    assertTrue(watchingThread(phoneResumed = false, carThreadVisible = true))
    assertFalse(watchingThread(phoneResumed = false, carThreadVisible = false))
    assertTrue(SWEEP_MIN_GAP_MS < SWEEP_EVERY_MS)
  }

  @Test
  fun retryDelayDoublesThenCaps() {
    assertEquals(2_000L, mailboxRetryDelayMs(0))
    assertEquals(4_000L, mailboxRetryDelayMs(1))
    assertEquals(32_000L, mailboxRetryDelayMs(4))
    assertEquals(60_000L, mailboxRetryDelayMs(5))
    assertEquals(60_000L, mailboxRetryDelayMs(99))
  }

  @Test
  fun expiredSessionIsAConnectError() {
    assertTrue(mailboxConnectError("kit", "", sessionExpired = true)!!.contains("expired"))
    assertNull(mailboxConnectError("kit", "jwe", sessionExpired = false))
  }

  @Test
  fun sessionExpZeroIsNotExpired() {
    assertFalse(sessionExpired(0L, 9_999_999L))
    assertFalse(sessionExpired(100L, 99L))
    assertTrue(sessionExpired(100L, 100L))
    assertTrue(sessionExpired(100L, 101L))
  }

  @Test
  fun liveBearerDoesNotFallBackToSpikeAfterGoogleExpires() {
    assertEquals("jwe", liveBearer("jwe", 200L, "secret", 50L))
    assertEquals("", liveBearer("jwe", 100L, "secret", 100L))
    assertEquals("secret", liveBearer("", 100L, "secret", 200L))
  }

  @Test
  fun persistSpikeOnlyOnLoopbackDebug() {
    assertTrue(persistSpikeAllowed("http://10.0.2.2:3000", false, true))
    assertTrue(persistSpikeAllowed("http://localhost:3000", false, true))
    assertFalse(persistSpikeAllowed("https://gantree.example.com", false, true))
    assertTrue(persistSpikeAllowed("https://gantree.example.com", false, false))
    assertFalse(persistSpikeAllowed("http://10.0.2.2:3000", true, true))
    assertFalse(loopbackMailboxHost(""))
  }
}
