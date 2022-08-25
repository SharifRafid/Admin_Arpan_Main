package admin.arpan.delivery.utils.networking

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.SlidingTextItem
import admin.arpan.delivery.models.*
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.requests.SendNotificationRequest
import admin.arpan.delivery.utils.networking.responses.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
  @POST("auth/login-with-email-pass")
  suspend fun login(@Body request: LoginRequest): LoginResponse

  @POST("auth/refresh")
  suspend fun refreshSession(@Body request: RefreshRequest): RefreshResponse

  @POST("auth/logout")
  suspend fun logout(
    @Header("Authorization") accessToken: String,
    @Body refreshToken: HashMap<String, Any>
  ): DefaultResponse

  @POST("notifications/send-notification-to-user")
  suspend fun sendNotificationToUser(
    @Header("Authorization") accessToken: String,
    @Body sendNotificationRequest: SendNotificationRequest
  ): DefaultResponse

  @POST("notifications/send-notification-to-da")
  suspend fun sendNotificationToDA(
    @Header("Authorization") accessToken: String,
    @Body sendNotificationRequest: SendNotificationRequest
  ): DefaultResponse

  @GET("users")
  suspend fun getAllUsers(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllUserResponse

  @POST("users")
  suspend fun createUser(
    @Header("Authorization") accessToken: String,
    @Body shop: User
  ): User

  @POST("users/registration-tokens-admin")
  suspend fun addAdminRegistrationToken(
    @Header("Authorization") accessToken: String,
    @Body fcmToken: HashMap<String, Any>
  ): DefaultResponse

  @POST("users/registration-tokens-moderator")
  suspend fun addModeratorRegistrationToken(
    @Header("Authorization") accessToken: String,
    @Body fcmToken: HashMap<String, Any>
  ): DefaultResponse

  @PATCH("users/{id}")
  suspend fun updateUser(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): User

  @DELETE("users/{id}")
  suspend fun deleteUser(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse


  @Multipart
  @POST("file/upload")
  suspend fun uploadFile(
    @Header("Authorization") accessToken: String,
    @Part fileName: MultipartBody.Part,
    @Header("path") path: String,
  ): Image

  @POST("orders")
  suspend fun createNewOrder(
    @Header("Authorization") accessToken: String,
    @Body orderItemMain: OrderItemMain
  ): OrderItemMain

  @POST("orders/filter")
  suspend fun getOrders(
    @Header("Authorization") accessToken: String,
    @Body getOrdersRequest: GetOrdersRequest
  ): GetOrdersResponse

  @PATCH("orders/{id}")
  suspend fun updateOrder(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): OrderItemMain

  @GET("orders/{id}")
  suspend fun getOrderById(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): OrderItemMain

  @DELETE("orders/{id}")
  suspend fun deleteOrder(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

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

  @DELETE("shops/{id}")
  suspend fun deleteShop(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @PATCH("shops/{id}/{categoryId}/remove-product-category")
  suspend fun removeCategoryFromShop(
    @Header("Authorization") accessToken: String,
    @Path("id") shopId: String,
    @Path("categoryId") categoryId: String,
  ): DefaultResponse

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
    @Query("shopId") shopId: String,
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

  @GET("das")
  suspend fun getAllDAs(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllDAResponse

  @GET("das/active")
  suspend fun getActiveDAs(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllUserResponse

  @POST("users")
  suspend fun createDA(
    @Header("Authorization") accessToken: String,
    @Body shop: User
  ): User

  @PATCH("users/{id}")
  suspend fun updateDA(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): User

  @DELETE("users/{id}")
  suspend fun deleteDA(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @GET("notices")
  suspend fun getAllNotices(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllNoticesResponse

  @POST("notices")
  suspend fun createNotice(
    @Header("Authorization") accessToken: String,
    @Body shop: SlidingTextItem
  ): SlidingTextItem

  @PATCH("notices/{id}")
  suspend fun updateNotice(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): SlidingTextItem

  @DELETE("notices/{id}")
  suspend fun deleteNotice(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @GET("settings/{id}")
  suspend fun getSettings(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): Setting

  @POST("settings")
  suspend fun createSetting(
    @Header("Authorization") accessToken: String,
    @Body shop: Setting
  ): Setting

  @PATCH("settings/{id}")
  suspend fun updateSetting(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body shop: HashMap<String, Any>
  ): Setting

  @DELETE("settings/{id}")
  suspend fun deleteSetting(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @GET("banners")
  suspend fun getAllBanners(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllBannerResponse

  @POST("banners")
  suspend fun createBanner(
    @Header("Authorization") accessToken: String,
    @Body banner: Banner
  ): Banner

  @PATCH("banners/{id}")
  suspend fun updateBanner(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body banner: HashMap<String, Any>
  ): Banner

  @DELETE("banners/{id}")
  suspend fun deleteBanner(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse

  @GET("locations")
  suspend fun getAllLocations(
    @Header("Authorization") accessToken: String,
    @Query("limit") limit: Int,
    @Query("page") page: Int
  ): GetAllLocationResponse

  @POST("locations")
  suspend fun createLocation(
    @Header("Authorization") accessToken: String,
    @Body banner: Location
  ): Location

  @PATCH("locations/{id}")
  suspend fun updateLocation(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String,
    @Body banner: HashMap<String, Any>
  ): Location

  @DELETE("locations/{id}")
  suspend fun deleteLocation(
    @Header("Authorization") accessToken: String,
    @Path("id") id: String
  ): DefaultResponse
}
