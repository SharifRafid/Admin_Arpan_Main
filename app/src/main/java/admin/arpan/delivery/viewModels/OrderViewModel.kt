package admin.arpan.delivery.viewModels

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.User
import admin.arpan.delivery.repositories.DARepository
import admin.arpan.delivery.repositories.OrderRepository
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
  private val application: Application,
  private val orderRepository: OrderRepository,
) : ViewModel() {

  fun getAllItems(getOrdersRequest : GetOrdersRequest) =
    liveData(Dispatchers.IO) {
      var dataResponse: GetOrdersResponse
      try {
        dataResponse = orderRepository.getOrders(getOrdersRequest)
      } catch (e: Exception) {
        dataResponse =
          GetOrdersResponse(
            true,
            e.message.toString(),
            ArrayList(), null,
            null, null,
            null
          )
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun createItem(data: OrderItemMain) =
    liveData(Dispatchers.IO) {
      var dataResponse: OrderItemMain
      try {
        dataResponse = orderRepository.createNewOrder(data)
      } catch (e: Exception) {
        dataResponse = OrderItemMain(true, e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: OrderItemMain
    try {
      dataResponse = orderRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = OrderItemMain(true, e.message.toString())
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = orderRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}