package admin.arpan.delivery.utils.networking

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.requests.LoginRequest
import admin.arpan.delivery.utils.networking.requests.RefreshRequest
import admin.arpan.delivery.utils.networking.responses.DefaultResponse
import admin.arpan.delivery.utils.networking.responses.GetOrdersResponse
import admin.arpan.delivery.utils.networking.responses.RefreshResponse
import admin.arpan.delivery.utils.networking.responses.LoginResponse
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
    suspend fun createNewOrder(@Header("Authorization") accessToken: String,@Body orderItemMain: OrderItemMain): DefaultResponse

    @POST("orders/filter")
    suspend fun getOrders(@Header("Authorization") accessToken: String,@Body getOrdersRequest: GetOrdersRequest): GetOrdersResponse
}