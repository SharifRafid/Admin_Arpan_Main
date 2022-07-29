package admin.arpan.delivery.utils.networking

import admin.arpan.delivery.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuilder {

  private lateinit var retrofit: Retrofit

  fun getRetrofit(): Retrofit {
      if (!this::retrofit.isInitialized) {
          retrofit = Retrofit.Builder()
              .baseUrl(Constants.SERVER_BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .build() //Doesn't require the adapter
      }
      return retrofit
  }

  val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}