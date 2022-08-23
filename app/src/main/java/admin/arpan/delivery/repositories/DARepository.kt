package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DARepository
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

  suspend fun getAll(): GetAllDAResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllDAResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllDAs("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User()
    } else {
      retrofitBuilder.apiService.updateDA("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: User): User {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      User(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createDA("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteDA("Bearer $accessToken", id)
    }
  }

}