package admin.arpan.delivery.repositories

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.responses.DefaultResponse
import admin.arpan.delivery.utils.networking.responses.GetOrdersResponse
import admin.arpan.delivery.utils.networking.responses.LoginResponse
import admin.arpan.delivery.utils.networking.responses.RefreshResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository
@Inject constructor(
  private val retrofitBuilder: RetrofitBuilder,
  private val preference: Preference
) {

  suspend fun createNewOrder(orderItemMain: OrderItemMain): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.createNewOrder("Bearer $accessToken", orderItemMain)
    }
  }

  suspend fun getOrders(getOrdersRequest: GetOrdersRequest): GetOrdersResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetOrdersResponse(true, "Not logged in", null, null, null, null, null)
    } else {
      retrofitBuilder.apiService.getOrders("Bearer $accessToken", getOrdersRequest)
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