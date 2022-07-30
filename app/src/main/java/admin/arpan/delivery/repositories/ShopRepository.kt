package admin.arpan.delivery.repositories

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

//  suspend fun createNewOrder(orderItemMain: OrderItemMain): DefaultResponse {
//    val accessToken = getAccessToken()
//    return if (accessToken == null) {
//      DefaultResponse(true, "Not logged in")
//    } else {
//      retrofitBuilder.apiService.createNewOrder("Bearer $accessToken", orderItemMain)
//    }
//  }

  suspend fun getShops(): GetAllShopsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllShopsResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllShops("Bearer $accessToken")
    }
  }

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }
}