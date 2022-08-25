package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Setting
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository
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

  suspend fun getSetting(id : String): Setting {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Setting(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getSettings("Bearer $accessToken", id)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): Setting {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Setting()
    } else {
      retrofitBuilder.apiService.updateSetting("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: Setting): Setting {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Setting(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createSetting("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteSetting("Bearer $accessToken", id)
    }
  }

}