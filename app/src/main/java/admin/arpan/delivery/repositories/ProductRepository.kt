package admin.arpan.delivery.repositories

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Product
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository
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

  suspend fun createProduct(product: Product): Product {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Product()
    } else {
      retrofitBuilder.apiService.createNewProduct("Bearer $accessToken", product)
    }
  }

  suspend fun getProducts(): GetAllProductsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllProductsResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getAllProducts("Bearer $accessToken", 100, 1)
    }
  }


  suspend fun getProductsByCategoryId(id: String): GetAllProductsResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllProductsResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getProductsByCategoryId("Bearer $accessToken", id, 100, 1)
    }
  }


  suspend fun updateProduct(id: String, item: HashMap<String, Any>): Product {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Product()
    } else {
      retrofitBuilder.apiService.updateProduct("Bearer $accessToken", id, item)
    }
  }

  suspend fun deleteProduct(id: String): DefaultResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      DefaultResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.deleteProduct("Bearer $accessToken", id)
    }
  }


}