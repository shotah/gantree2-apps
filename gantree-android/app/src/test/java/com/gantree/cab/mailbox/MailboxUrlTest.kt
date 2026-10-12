package com.gantree.cab.mailbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MailboxUrlTest {
  @Test
  fun httpsOriginBecomesWssPhoneSocket() {
    assertEquals(
      "wss://gantree.example.workers.dev/ws/kit?role=phone",
      mailboxUrl("https://gantree.example.workers.dev", "kit"),
    )
  }

  @Test
  fun emulatorLoopbackIsWs() {
    assertEquals(
      "ws://10.0.2.2:3000/ws/kit?role=phone",
      mailboxUrl("http://10.0.2.2:3000/", "kit"),
    )
  }

  @Test
  fun slugRulesMatchTheWorker() {
    assertEquals("kit", parseSlug("Kit"))
    assertEquals("kit-2", parseSlug("  KIT-2  "))
    assertNull(parseSlug("1kit"))
    assertNull(parseSlug(""))
    assertNull(parseSlug("a".repeat(33)))
  }

  @Test
  fun hostWithoutSchemeIsWss() {
    assertEquals(
      "wss://gantree.example.workers.dev/ws/kit?role=crane",
      mailboxUrl("gantree.example.workers.dev/", "kit", "crane"),
    )
  }

  @Test
  fun httpOriginTrimsSlash() {
    assertEquals("http://10.0.2.2:3000", httpOrigin("  http://10.0.2.2:3000/  "))
  }

  @Test
  fun normalizeMailboxOriginAcceptsWorkerAndAgentUrls() {
    assertEquals(
      "https://gantree.example.com",
      normalizeMailboxOrigin("https://gantree.example.com/"),
    )
    assertEquals(
      "https://gantree.example.com",
      normalizeMailboxOrigin("https://gantree.example.com/ws/kit"),
    )
    assertEquals(
      "https://gantree.example.com",
      normalizeMailboxOrigin("wss://gantree.example.com/ws/kit"),
    )
    assertEquals(
      "http://10.0.2.2:3000",
      normalizeMailboxOrigin("ws://10.0.2.2:3000/ws/kit"),
    )
    assertEquals(
      "https://gantree.example.workers.dev",
      normalizeMailboxOrigin("gantree.example.workers.dev"),
    )
    assertEquals("http://10.0.2.2:3000", normalizeMailboxOrigin("http://10.0.2.2:3000"))
  }
}
