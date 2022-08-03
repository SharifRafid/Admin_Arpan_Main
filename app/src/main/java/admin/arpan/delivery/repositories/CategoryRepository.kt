package admin.arpan.delivery.repositories

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import admin.arpan.delivery.utils.networking.responses.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository
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

  suspend fun createCategory(category: Category): Category {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Category()
    } else {
      retrofitBuilder.apiService.createNewCategory("Bearer $accessToken", category)
    }
  }

  suspend fun getCategories(type: String): GetAllCategoriesResponse {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      GetAllCategoriesResponse(true, "Not logged in")
    } else {
      retrofitBuilder.apiService.getAllCategories("Bearer $accessToken",type, 200, 1)
    }
  }

  suspend fun updateCategory(id: String, item: HashMap<String, Any>): Category {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      Category()
    } else {
      retrofitBuilder.apiService.updateCategory("Bearer $accessToken", id, item)
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

  suspend fun getProductCategoriesOfShop(id: String): ArrayList<Category>? {
    val accessToken = getAccessToken()
    return if (accessToken == null) {
      null
    } else {
      retrofitBuilder.apiService.getProductCategoriesOfShop("Bearer $accessToken",id)
    }
  }

}