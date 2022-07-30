package admin.arpan.delivery.viewModels

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.repositories.AuthRepository
import admin.arpan.delivery.repositories.OrderRepository
import admin.arpan.delivery.repositories.ShopRepository
import admin.arpan.delivery.ui.home.HomeActivityMain
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.responses.*
import admin.arpan.delivery.utils.showToast
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import java.sql.Ref
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val application: Application,
  private val orderRepository: OrderRepository,
  private val shopRepository: ShopRepository,
) : ViewModel() {

  fun createNewOrder(orderItemMain: OrderItemMain) =
    liveData(Dispatchers.IO) {
      var defaultResponse: DefaultResponse
      try {
        defaultResponse = orderRepository.createNewOrder(orderItemMain)
      } catch (e: Exception) {
        defaultResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(defaultResponse)
    }

  fun getOrders(getOrdersRequest: GetOrdersRequest) =
    liveData(Dispatchers.IO) {
      var getOrdersResponse: GetOrdersResponse
      try {
        getOrdersResponse = orderRepository.getOrders(getOrdersRequest)
      } catch (e: Exception) {
        getOrdersResponse = GetOrdersResponse(
          true, "Error : ${e.message.toString()}",
          null, null, null, null, null
        )
        e.printStackTrace()
      }
      emit(getOrdersResponse)
    }

  fun getShops() =
    liveData(Dispatchers.IO) {
      var getAllShopsResponse: GetAllShopsResponse
      try {
        getAllShopsResponse = shopRepository.getShops()
      } catch (e: Exception) {
        getAllShopsResponse =
          GetAllShopsResponse(true, e.message.toString(), ArrayList(), null, null, null, null)
        e.printStackTrace()
      }
      emit(getAllShopsResponse)
    }

}