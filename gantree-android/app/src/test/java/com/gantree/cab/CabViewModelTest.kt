package com.gantree.cab

import androidx.test.core.app.ApplicationProvider
import com.gantree.cab.drive.MailboxService
import com.gantree.cab.mailbox.mailboxNotAllowedHint
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/** 403 not_allowed: Google was fine, gantree has no row. No JWE, no socket. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = CabApp::class)
class CabViewModelTest {
  private lateinit var server: MockWebServer
  private lateinit var app: CabApp
  private lateinit var vm: CabViewModel

  @Before
  fun start() {
    server = MockWebServer()
    server.start()
    app = ApplicationProvider.getApplicationContext<CabApp>()
    app.prefs.origin = "http://127.0.0.1:9"
    app.prefs.session = ""
    app.prefs.sessionExp = 0L
    app.prefs.email = ""
    app.prefs.spike = ""
    vm = CabViewModel(app)
    vm.setOrigin(server.url("/").toString())
    Shadows.shadowOf(app).clearStartedServices()
  }

  @After
  fun stop() {
    server.shutdown()
  }

  @Test
  fun notAllowedStoresNothingAndShowsTheId() {
    server.enqueue(
      MockResponse().setResponseCode(403).setBody(
        """{"error":"not_allowed","sub":"118000","email":"ada@example.com"}""",
      ),
    )
    runBlocking { vm.finishSignIn(app, "id-token", "sealed-nonce") }
    assertEquals("", app.prefs.session)
    assertEquals(0L, app.prefs.sessionExp)
    assertEquals("", app.prefs.email)
    assertFalse(app.prefs.signedIn)
    assertEquals("118000", vm.sub.value)
    assertEquals("ada@example.com", vm.email.value)
    assertEquals(emptyList<String>(), vm.agents.value)
    assertEquals(mailboxNotAllowedHint("ada@example.com"), vm.authHint.value)
    assertEquals(mailboxNotAllowedHint("ada@example.com"), app.mouth.hint.value)
    val req = server.takeRequest()
    assertEquals("POST", req.method)
    assertEquals("/api/auth/token", req.path)
    assertEquals(1, server.requestCount)
    assertFalse(mailboxStarted())
  }

  @Test
  fun notAllowedWithNoEmailStillShowsTheSub() {
    server.enqueue(
      MockResponse().setResponseCode(403).setBody(
        """{"error":"not_allowed","sub":"118000","email":null}""",
      ),
    )
    runBlocking { vm.finishSignIn(app, "id-token", "sealed-nonce") }
    assertEquals("", app.prefs.session)
    assertEquals("", vm.email.value)
    assertEquals("118000", vm.sub.value)
    assertEquals(emptyList<String>(), vm.agents.value)
    assertEquals(mailboxNotAllowedHint(""), vm.authHint.value)
    assertFalse(mailboxStarted())
  }

  @Test
  fun refusedNonceNamesTheCauseAndDoesNotStartTheSocket() {
    server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"nonce"}"""))
    runBlocking { vm.finishSignIn(app, "id-token", "nope") }
    assertEquals("", app.prefs.session)
    assertEquals("", vm.sub.value)
    assertEquals("", vm.email.value)
    assertEquals("Sign-in was refused (nonce) \u2014 try again.", vm.authHint.value)
    assertFalse(mailboxStarted())
  }

  @Test
  fun refusedTokenNamesTheCause() {
    server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"token"}"""))
    runBlocking { vm.finishSignIn(app, "id-token", "sealed-nonce") }
    assertEquals("", app.prefs.session)
    assertEquals("Sign-in was refused (token) \u2014 try again.", vm.authHint.value)
    assertFalse(mailboxStarted())
  }

  @Test
  fun notConfiguredSaysGoogleLoginIsMissing() {
    server.enqueue(MockResponse().setResponseCode(503).setBody("""{"error":"not_configured"}"""))
    runBlocking { vm.finishSignIn(app, "id-token", "sealed-nonce") }
    assertEquals("", app.prefs.session)
    assertEquals("", vm.sub.value)
    assertEquals("gantree has no Google login configured yet.", vm.authHint.value)
    assertFalse(mailboxStarted())
  }

  private fun mailboxStarted(): Boolean =
    Shadows.shadowOf(app).allStartedServices.any {
      it.component?.className == MailboxService::class.java.name
    }
}
