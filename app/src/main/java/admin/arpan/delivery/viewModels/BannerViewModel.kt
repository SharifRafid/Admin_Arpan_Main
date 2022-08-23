package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Banner
import admin.arpan.delivery.repositories.BannerRepository
import admin.arpan.delivery.repositories.DARepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class BannerViewModel @Inject constructor(
  private val application: Application,
  private val bannerRepository: BannerRepository,
) : ViewModel() {

  fun getAllItems() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllBannerResponse
      try {
        dataResponse = bannerRepository.getAll()
      } catch (e: Exception) {
        dataResponse =
          GetAllBannerResponse(
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

  fun createItem(data: Banner) =
    liveData(Dispatchers.IO) {
      var dataResponse: Banner
      try {
        dataResponse = bannerRepository.create(data)
      } catch (e: Exception) {
        dataResponse = Banner(true, e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: Banner
    try {
      dataResponse = bannerRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = Banner(true, e.message.toString())
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = bannerRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}