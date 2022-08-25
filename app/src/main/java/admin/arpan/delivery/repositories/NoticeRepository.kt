package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.SlidingTextItem
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository
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

  suspend fun getAll(): GetAllNoticesResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllNoticesResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllNotices("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun update(id: String, data: HashMap<String, Any>): SlidingTextItem {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      SlidingTextItem(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.updateNotice("Bearer $accessToken", id, data)
    }
  }

  suspend fun create(data: SlidingTextItem): SlidingTextItem {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      SlidingTextItem(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createNotice("Bearer $accessToken", data)
    }
  }

  suspend fun delete(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteNotice("Bearer $accessToken", id)
    }
  }

}