package com.gantree.cab.mailbox

import okhttp3.CookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AuthConfig(
  val mode: String?,
  val google: Boolean,
  /** Additive: the Worker publishes pocket voice (header mic + `/api/tts`). Missing → false. */
  val voice: Boolean = false,
)

data class Me(
  val sub: String,
  val email: String?,
  val agents: List<String>,
)

data class NativeSession(
  val token: String,
  val sub: String,
  val email: String?,
  val exp: Long,
)

class AuthApi(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .cookieJar(CookieJar.NO_COOKIES)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build(),
) {
  fun config(origin: String): AuthConfig {
    val body = get(httpOrigin(origin) + "/api/auth/config")
    return AuthConfig(
      mode = body.optStringOrNull("mode"),
      google = body.optBoolean("google", false),
      voice = body.optBoolean("voice", false),
    )
  }

  /**
   * Server-issued nonce. The Worker seals it (10 minutes, not stored).
   * POST is 401 for any other value, so a failed GET is null. Do not mint one.
   */
  fun nonce(origin: String): String? {
    return try {
      readNonce(origin)
    } catch (_: AuthException) {
      null
    } catch (_: org.json.JSONException) {
      null
    }
  }

  /**
   * [nonce] for sign-in. 503 `not_configured` throws so the hint can say
   * Google login is not set up. Every other miss is null.
   */
  internal fun nonceForSignIn(origin: String): String? {
    return try {
      readNonce(origin)
    } catch (e: AuthException) {
      if (e.code == 503) {
        throw e
      }
      null
    } catch (_: org.json.JSONException) {
      null
    }
  }

  private fun readNonce(origin: String): String? {
    val body = get(httpOrigin(origin) + "/api/auth/nonce")
    return body.optStringOrNull("nonce")
  }

  fun me(origin: String, token: String): Me {
    val body = get(httpOrigin(origin) + "/api/auth/me", token)
    // `agents` is the field. `cranes` is the alias copy until cutover.
    val listed = if (body.has("agents") && body.optJSONArray("agents") != null) {
      body.optJSONArray("agents")
    } else {
      body.optJSONArray("cranes")
    }
    val list = buildList {
      if (listed != null) {
        for (i in 0 until listed.length()) {
          val s = parseSlug(listed.optString(i)) ?: continue
          add(s)
        }
      }
    }
    return Me(
      sub = body.getString("sub"),
      email = body.optStringOrNull("email"),
      agents = list,
    )
  }

  fun token(origin: String, idToken: String, nonce: String): NativeSession {
    val payload = JSONObject()
      .put("id_token", idToken)
      .put("nonce", nonce)
      .toString()
    val body = post(httpOrigin(origin) + "/api/auth/token", payload)
    return NativeSession(
      token = body.getString("token"),
      sub = body.getString("sub"),
      email = body.optStringOrNull("email"),
      exp = body.optLong("exp", 0L),
    )
  }

  private fun get(url: String, token: String? = null): JSONObject {
    val req = Request.Builder().url(url).get()
    if (!token.isNullOrEmpty()) {
      req.header("Authorization", "Bearer $token")
    }
    return execute(req.build())
  }

  private fun post(url: String, json: String): JSONObject {
    val req = Request.Builder()
      .url(url)
      .post(json.toRequestBody(JSON))
      .build()
    return execute(req)
  }

  private fun execute(req: Request): JSONObject {
    client.newCall(req).execute().use { res ->
      val raw = res.body?.string().orEmpty()
      if (!res.isSuccessful) {
        throw AuthException(res.code, raw)
      }
      return JSONObject(raw)
    }
  }

  companion object {
    private val JSON = "application/json; charset=utf-8".toMediaType()
  }
}

class AuthException(val code: Int, val body: String) : RuntimeException("auth $code")

/** Google verified this person; gantree has no row. No session was issued. */
data class NotAllowed(
  val sub: String,
  val email: String?,
)

/** 403 `not_allowed` body. Any other JSON is null. */
fun parseNotAllowed(body: String): NotAllowed? {
  val o = jsonObjectOrNull(body) ?: return null
  if (o.optString("error") != "not_allowed") {
    return null
  }
  val sub = o.optStringOrNull("sub") ?: return null
  return NotAllowed(sub = sub, email = o.optStringOrNull("email"))
}

/** `{"error":"nonce"}` → `nonce`. Missing or junk → null. */
fun parseAuthError(body: String): String? = jsonObjectOrNull(body)?.optStringOrNull("error")

private fun jsonObjectOrNull(body: String): JSONObject? = try {
  JSONObject(body)
} catch (_: org.json.JSONException) {
  null
}

private fun JSONObject.optStringOrNull(key: String): String? {
  if (!has(key) || isNull(key)) {
    return null
  }
  val v = optString(key)
  return v.takeIf { it.isNotEmpty() }
}
