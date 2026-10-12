package com.gantree.cab.mailbox

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthApiTest {
  private lateinit var server: MockWebServer
  private lateinit var api: AuthApi

  @Before
  fun start() {
    server = MockWebServer()
    server.start()
    api = AuthApi()
  }

  @After
  fun stop() {
    server.shutdown()
  }

  @Test
  fun configReadsTheFiveContractKeys() {
    server.enqueue(
      MockResponse().setBody(
        """{"mode":"google","google":true,"voice":true,"version":"0.2.10","dev":true}""",
      ),
    )
    val got = api.config(server.url("/").toString())
    assertEquals("google", got.mode)
    assertEquals(true, got.google)
    assertEquals(true, got.voice)
    assertEquals("/api/auth/config", server.takeRequest().path)
  }

  @Test
  fun configReadsModeAndGoogle() {
    server.enqueue(MockResponse().setBody("""{"mode":"google","google":true}"""))
    val got = api.config(server.url("/").toString())
    assertEquals("google", got.mode)
    assertEquals(true, got.google)
    assertEquals("/api/auth/config", server.takeRequest().path)
  }

  @Test
  fun meReadsAgentsAndSkipsBlanks() {
    server.enqueue(MockResponse().setBody("""{"sub":"u1","email":"ada@example.com","agents":["kit","", "dock","Nope!"]}"""))
    val got = api.me(server.url("/").toString(), "jwe")
    assertEquals("u1", got.sub)
    assertEquals("ada@example.com", got.email)
    assertEquals(listOf("kit", "dock"), got.agents)
    assertEquals("Bearer jwe", server.takeRequest().getHeader("Authorization"))
  }

  @Test
  fun configIgnoresAdditiveKeys() {
    server.enqueue(MockResponse().setBody("""{"mode":"google","google":true,"version":"0.4.0","dev":true}"""))
    val got = api.config(server.url("/").toString())
    assertEquals("google", got.mode)
    assertEquals(true, got.google)
    assertEquals(false, got.voice)
    assertEquals(null, server.takeRequest().getHeader("Cookie"))
  }

  @Test
  fun configReadsVoiceWhenTheWorkerPublishesIt() {
    server.enqueue(MockResponse().setBody("""{"mode":"google","google":true,"voice":true}"""))
    assertEquals(true, api.config(server.url("/").toString()).voice)
    server.enqueue(MockResponse().setBody("""{"mode":"google","google":true,"voice":"yes"}"""))
    assertEquals(false, api.config(server.url("/").toString()).voice)
  }

  @Test
  fun nonceReadsTheValue() {
    server.enqueue(MockResponse().setBody("""{"nonce":"nce-1"}"""))
    val got = api.nonce(server.url("/").toString())
    assertEquals("nce-1", got)
    val req = server.takeRequest()
    assertEquals("/api/auth/nonce", req.path)
    assertEquals("GET", req.method)
    assertEquals(null, req.getHeader("Cookie"))
  }

  @Test
  fun nonceNotConfiguredReturnsNull() {
    server.enqueue(MockResponse().setResponseCode(503).setBody("""{"error":"not_configured"}"""))
    assertNull(api.nonce(server.url("/").toString()))
  }

  @Test
  fun nonceForSignInSurfacesNotConfigured() {
    server.enqueue(MockResponse().setResponseCode(503).setBody("""{"error":"not_configured"}"""))
    try {
      api.nonceForSignIn(server.url("/").toString())
      throw AssertionError("expected AuthException")
    } catch (e: AuthException) {
      assertEquals(503, e.code)
      assertEquals("not_configured", parseAuthError(e.body))
    }
  }

  @Test
  fun nonceForSignInTreatsOtherFailuresAsNull() {
    server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"not found"}"""))
    assertNull(api.nonceForSignIn(server.url("/").toString()))
  }

  @Test
  fun nonceMissingRouteReturnsNull() {
    server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"not found"}"""))
    assertEquals(null, api.nonce(server.url("/").toString()))
  }

  @Test
  fun nonceEmptyBodyReturnsNull() {
    server.enqueue(MockResponse().setBody("{}"))
    assertEquals(null, api.nonce(server.url("/").toString()))
  }

  @Test
  fun configDefaultsWhenTheBodyIsEmpty() {
    server.enqueue(MockResponse().setBody("{}"))
    val got = api.config(server.url("/").toString())
    assertEquals(null, got.mode)
    assertEquals(false, got.google)
    assertEquals(false, got.voice)
  }

  @Test
  fun meWithoutAgentsIsEmpty() {
    server.enqueue(MockResponse().setBody("""{"sub":"u1"}"""))
    val got = api.me(server.url("/").toString(), "jwe")
    assertEquals(null, got.email)
    assertEquals(emptyList<String>(), got.agents)
  }


  @Test
  fun meFallsBackToAliasWhenAgentsIsAbsent() {
    server.enqueue(MockResponse().setBody("""{"sub":"u1","email":"ada@example.com","cranes":["kit","", "dock","Nope!"]}"""))
    val got = api.me(server.url("/").toString(), "jwe")
    assertEquals(listOf("kit", "dock"), got.agents)
  }

  @Test
  fun mePrefersAgentsWhenTheArrayIsEmpty() {
    server.enqueue(MockResponse().setBody("""{"sub":"u1","agents":[],"cranes":["kit"]}"""))
    val got = api.me(server.url("/").toString(), "jwe")
    assertEquals(emptyList<String>(), got.agents)
  }

  @Test
  fun tokenPostsIdTokenAndKeepsExpInSeconds() {
    server.enqueue(
      MockResponse().setBody(
        """{"token":"session-jwe","sub":"118000","email":"ada@example.com","exp":1700000000}""",
      ),
    )
    val got = api.token(server.url("/").toString(), "id-token", "sealed-nonce")
    assertEquals("session-jwe", got.token)
    assertEquals("118000", got.sub)
    assertEquals("ada@example.com", got.email)
    assertEquals(1700000000L, got.exp)
    val req = server.takeRequest()
    assertEquals("POST", req.method)
    assertEquals("/api/auth/token", req.path)
    assertNull(req.getHeader("Cookie"))
    val raw = req.body.readUtf8()
    assertTrue(raw.contains("\"id_token\":\"id-token\""))
    assertTrue(raw.contains("\"nonce\":\"sealed-nonce\""))
  }

  @Test
  fun tokenAllowsNullEmail() {
    server.enqueue(
      MockResponse().setBody("""{"token":"session-jwe","sub":"118000","email":null,"exp":1700000000}"""),
    )
    val got = api.token(server.url("/").toString(), "id-token", "sealed-nonce")
    assertNull(got.email)
    assertEquals(1700000000L, got.exp)
  }

  @Test
  fun tokenNotAllowedIs403AndParses() {
    val raw = """{"error":"not_allowed","sub":"118000","email":"ada@example.com"}"""
    server.enqueue(MockResponse().setResponseCode(403).setBody(raw))
    try {
      api.token(server.url("/").toString(), "id-token", "sealed-nonce")
      throw AssertionError("expected AuthException")
    } catch (e: AuthException) {
      assertEquals(403, e.code)
      assertEquals(NotAllowed("118000", "ada@example.com"), parseNotAllowed(e.body))
    }
  }

  @Test
  fun tokenNotAllowedEmailMayBeNull() {
    val raw = """{"error":"not_allowed","sub":"118000","email":null}"""
    assertEquals(NotAllowed("118000", null), parseNotAllowed(raw))
    assertNull(parseNotAllowed("""{"error":"token","sub":"118000"}"""))
    assertNull(parseNotAllowed("""{"error":"not_allowed"}"""))
    assertNull(parseNotAllowed("nope"))
  }

  @Test
  fun tokenNonce401() {
    server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"nonce"}"""))
    try {
      api.token(server.url("/").toString(), "id-token", "nope")
      throw AssertionError("expected AuthException")
    } catch (e: AuthException) {
      assertEquals(401, e.code)
      assertEquals("nonce", parseAuthError(e.body))
    }
  }

  @Test
  fun failedStatusIsAuthException() {
    server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"nope"}"""))
    try {
      api.config(server.url("/").toString())
      throw AssertionError("expected AuthException")
    } catch (e: AuthException) {
      assertEquals(401, e.code)
      assertTrue(e.body.contains("nope"))
    }
  }
}
