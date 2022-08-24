package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Location
import admin.arpan.delivery.models.User
import admin.arpan.delivery.repositories.DARepository
import admin.arpan.delivery.repositories.LocationRepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
  private val application: Application,
  private val locationRepository: LocationRepository,
) : ViewModel() {

  fun getAllItems() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllLocationResponse
      try {
        dataResponse = locationRepository.getAll()
      } catch (e: Exception) {
        dataResponse =
          GetAllLocationResponse(
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

  fun createItem(data: Location) =
    liveData(Dispatchers.IO) {
      var dataResponse: Location
      try {
        dataResponse = locationRepository.create(data)
      } catch (e: Exception) {
        dataResponse = Location()
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: Location
    try {
      dataResponse = locationRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = Location()
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = locationRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}