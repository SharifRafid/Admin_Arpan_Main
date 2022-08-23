package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Banner
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BannerRepository
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

  suspend fun getAll(): GetAllBannerResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllBannerResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllBanners("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): Banner {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Banner(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.updateBanner("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: Banner): Banner {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Banner(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createBanner("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteBanner("Bearer $accessToken", id)
    }
  }

}