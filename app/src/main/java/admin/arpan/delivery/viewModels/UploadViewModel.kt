package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Image
import admin.arpan.delivery.repositories.UploadRepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
  private val application: Application,
  private val uploadRepository: UploadRepository,
) : ViewModel() {

  fun uploadItem(body: MultipartBody.Part, path: String) =
    liveData(Dispatchers.IO) {
      var uploadResponse: Image
      try {
        uploadResponse = uploadRepository.uploadFile(body, path)
      } catch (e: Exception) {
        uploadResponse = Image()
        e.printStackTrace()
      }
      emit(uploadResponse)
    }

//  fun deleteCategory(id: String) =
//    liveData(Dispatchers.IO) {
//      var defaultResponse: DefaultResponse
//      try {
//        defaultResponse = categoryRepository.deleteCategory(id)
//      } catch (e: Exception) {
//        defaultResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
//        e.printStackTrace()
//      }
//      emit(defaultResponse)
//    }
}