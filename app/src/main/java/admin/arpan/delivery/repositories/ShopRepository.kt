package admin.arpan.delivery.repositories

import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
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

  private fun getAccessToken(): String? {
    return if (preference.getTokens() != null) {
      preference.getTokens()!!.access.token
    } else {
      null
    }
  }

  suspend fun getShops(): GetAllShopsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllShopsResponse(true, "Not logged in", ArrayList(), null, null, null, null)
    } else {
      retrofitBuilder.apiService.getAllShops("Bearer $accessToken", 100, 1)
    }
  }

  suspend fun updateShop(id: String, shop: HashMap<String, Any>): Shop {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Shop()
    } else {
      retrofitBuilder.apiService.updateShop("Bearer $accessToken", id, shop)
    }
  }

  suspend fun createShop(shop: Shop): Shop {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Shop()
    } else {
      retrofitBuilder.apiService.createShop("Bearer $accessToken", shop)
    }
  }

  suspend fun deleteShop(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteShop("Bearer $accessToken", id)
    }
  }

  suspend fun removeCategoryFromShop(shopId: String, categoryId: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.removeCategoryFromShop("Bearer $accessToken", shopId, categoryId)
    }
  }
}