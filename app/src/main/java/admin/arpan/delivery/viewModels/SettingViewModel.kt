package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Setting
import admin.arpan.delivery.repositories.SettingRepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
  private val application: Application,
  private val settingRepository: SettingRepository,
) : ViewModel() {

  fun getSettings(id : String) =
    liveData(Dispatchers.IO) {
      var dataResponse: Setting
      try {
        dataResponse = settingRepository.getSetting(id)
      } catch (e: Exception) {
        dataResponse =
          Setting(
            true,
            e.message.toString()
          )
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun createItem(data: Setting) =
    liveData(Dispatchers.IO) {
      var dataResponse: Setting
      try {
        dataResponse = settingRepository.create(data)
      } catch (e: Exception) {
        dataResponse = Setting(true, e.message.toString())
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: Setting
    try {
      dataResponse = settingRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = Setting(true, e.message.toString())
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = settingRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}