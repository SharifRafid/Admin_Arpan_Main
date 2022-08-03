package admin.arpan.delivery.utils.networking

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Image
import admin.arpan.delivery.models.Product
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.responses.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
  @POST("auth/login-with-email-pass")
  suspend fun login(@Body request: LoginRequest): LoginResponse

  @POST("auth/refresh")
  suspend fun refreshSession(@Body request: RefreshRequest): RefreshResponse

  @GET("auth/logout")
  suspend fun logout(@Header("Authorization") accessToken: String): DefaultResponse

  @POST("orders")
  suspend fun createNewOrder(
    @Header("Authorization") accessToken: String,
    @Body orderItemMain: OrderItemMain
  ): DefaultResponse

  @POST("orders/filter")
  suspend fun getOrders(
    @Header("Authorization") accessToken: String,
    @Body getOrdersRequest: GetOrdersRequest
  ): GetOrdersResponse

  @GET("shops")
  suspend fun getAllShops(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllShopsResponse

  @POST("shops")
  suspend fun createShop(
    @Header("Authorization") accessToken: String,
    @Body shop: Shop
  ): Shop

  @PATCH("shops/{id}")
  suspend fun updateShop(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): Shop

  @GET("categories")
  suspend fun getAllCategories(
    @Header("Authorization") accessToken: String,
    @Query("type") type: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllCategoriesResponse

  @POST("categories")
  suspend fun createNewCategory(
    @Header("Authorization") accessToken: String,
    @Body category: Category
  ): Category

  @PATCH("categories/{id}")
  suspend fun updateCategory(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body item: HashMap<String, Any>
  ): Category

  @DELETE("categories/{id}")
  suspend fun deleteCategory(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
  ): DefaultResponse

  @Multipart
  @POST("file/upload")
  suspend fun uploadFile(
    @Header("Authorization") accessToken: String,
    @Part fileName: MultipartBody.Part,
    @Header("path") path: String,
  ): Image

  @DELETE("shops/{id}")
  suspend fun deleteShop(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @GET("categories/shop-product")
  suspend fun getProductCategoriesOfShop(
    @Header("Authorization") accessToken: String,
    @Query("id") id: String
  ): ArrayList<Category>?

  @POST("products")
  suspend fun createNewProduct(
    @Header("Authorization") accessToken: String,
    @Body product: Product
  ): Product

  @GET("products")
  suspend fun getAllProducts(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllProductsResponse

  @GET("products")
  suspend fun getProductsByCategoryId(
    @Header("Authorization") accessToken: String,
    @Query("categoryId") id: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllProductsResponse

  @PATCH("products/{id}")
  suspend fun updateProduct(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body item: HashMap<String, Any>
  ): Product

  @DELETE("products/{id}")
  suspend fun deleteProduct(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
  ): DefaultResponse
}
