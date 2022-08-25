package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.User
import admin.arpan.delivery.repositories.DARepository
import admin.arpan.delivery.repositories.UserRepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
  private val application: Application,
  private val userRepository: UserRepository,
) : ViewModel() {

  fun getAllItems() =
    liveData(Dispatchers.IO) {
      var dataResponse: GetAllUserResponse
      try {
        dataResponse = userRepository.getAll()
      } catch (e: Exception) {
        dataResponse =
          GetAllUserResponse(
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

  fun createItem(data: User) =
    liveData(Dispatchers.IO) {
      var dataResponse: User
      try {
        dataResponse = userRepository.create(data)
      } catch (e: Exception) {
        dataResponse = User()
        e.printStackTrace()
      }
      emit(dataResponse)
    }

  fun updateItem(id: String, data: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var dataResponse: User
    try {
      dataResponse = userRepository.update(id, data)
    } catch (e: Exception) {
      dataResponse = User()
      e.printStackTrace()
    }
    emit(dataResponse)
  }

  fun deleteItem(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = userRepository.delete(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

  fun addRegTokenAdmin(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = userRepository.addRegistrationTokenAdmin(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

  fun addRegTokenModerator(id: String) = liveData(Dispatchers.IO) {
    var defaultResponse: DefaultResponse
    try {
      defaultResponse = userRepository.addRegistrationTokenModerator(id)
    } catch (e: Exception) {
      defaultResponse = DefaultResponse(true, e.message.toString())
      e.printStackTrace()
    }
    emit(defaultResponse)
  }

}