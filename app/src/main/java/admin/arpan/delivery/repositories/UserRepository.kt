package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }

  suspend fun getAll(): GetAllUserResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllUserResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllUsers("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User()
    } else {
      retrofitBuilder.apiService.updateUser("Bearer $accessToken", id, data)
    }
  }

  suspend fun addRegistrationTokenAdmin(data: String): DefaultResponse {
    val accessToken = getAccessToken()
    val fcmToken = HashMap<String, Any>()
    fcmToken["fcmToken"] = data
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.addAdminRegistrationToken("Bearer $accessToken", fcmToken)
    }
  }

  suspend fun addRegistrationTokenModerator(data: String): DefaultResponse {
    val accessToken = getAccessToken()
    val fcmToken = HashMap<String, Any>()
    fcmToken["fcmToken"] = data
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.addModeratorRegistrationToken("Bearer $accessToken", fcmToken)
    }
  }

  suspend fun create(data: User): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createUser("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteUser("Bearer $accessToken", id)
    }
  }

}