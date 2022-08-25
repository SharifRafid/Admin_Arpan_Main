package admin.arpan.delivery.viewModels

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.repositories.OrderRepository
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import java.sql.Ref
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val application: Application,
  private val orderRepository: OrderRepository
) : ViewModel() {

  fun createNewOrder(orderItemMain: OrderItemMain) =
    liveData(Dispatchers.IO) {
      var defaultResponse: OrderItemMain
      try {
        defaultResponse = orderRepository.createNewOrder(orderItemMain)
      } catch (e: Exception) {
        defaultResponse = OrderItemMain(true, "Error : ${e.message.toString()}")
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

}