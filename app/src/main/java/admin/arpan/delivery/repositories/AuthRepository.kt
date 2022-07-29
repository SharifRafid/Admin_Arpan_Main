package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.responses.LoginResponse
import admin.arpan.delivery.utils.networking.responses.RefreshResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

  suspend fun getLoginResponse(email: String, password: String) =
    retrofitBuilder.apiService.login(LoginRequest(email, password))

  suspend fun getRefreshResponse(): RefreshResponse {
    val refreshToken = getRefreshToken()
    return if (refreshToken == null) {
      RefreshResponse(true, "Not logged in", null, null)
    } else {
      retrofitBuilder.apiService.refreshSession(RefreshRequest(refreshToken))
    }
  }

  private fun getRefreshToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.refresh.token
    } else {
      null
    }
  }

  fun saveLoginResponse(loginResponse: LoginResponse) {
    if (loginResponse.user != null) {
      preference.saveUser(loginResponse.user)
    }
    if (loginResponse.tokens != null) {
      preference.saveTokens(loginResponse.tokens)
    }
  }

  fun saveTokens(tokens: Tokens) {
    preference.saveTokens(tokens)
  }
}