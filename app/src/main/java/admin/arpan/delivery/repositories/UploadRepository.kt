package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Image
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository
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

  suspend fun uploadFile(file: MultipartBody.Part, path: String): Image {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Image()
    } else {
      retrofitBuilder.apiService.uploadFile("Bearer $accessToken", file, path)
    }

  }

  suspend fun deleteCategory(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteCategory("Bearer $accessToken", id)
    }
  }
}