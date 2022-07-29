package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.repositories.AuthRepository
import admin.arpan.delivery.ui.home.HomeActivityMain
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.responses.LoginResponse
import admin.arpan.delivery.utils.networking.responses.RefreshResponse
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
class AuthViewModel @Inject constructor(
  private val application: Application,
  private val authRepository: AuthRepository
) : ViewModel() {

  fun getLoginResponse(username: String, password: String) =
    liveData(Dispatchers.IO) {
      var loginResponse: LoginResponse
      try {
        loginResponse = authRepository.getLoginResponse(username, password)
      } catch (e: Exception) {
        loginResponse = LoginResponse(true, "Error : ${e.message.toString()}", null, null)
        e.printStackTrace()
      }
      emit(loginResponse)
    }

  fun getRefreshResponse() = liveData(Dispatchers.IO) {
    var refreshResponse: RefreshResponse
    try {
      refreshResponse = authRepository.getRefreshResponse()
    } catch (e: Exception) {
      refreshResponse = RefreshResponse(true, "Error : ${e.message.toString()}", null, null)
      e.printStackTrace()
    }
    emit(refreshResponse)
  }

  fun switchActivity(loginResponse: LoginResponse) {
    if (loginResponse.error != true) {
      if (loginResponse.user!!.roles.contains("admin") ||
        loginResponse.user.roles.contains("moderator")
      ) {
        val intent = Intent(
          application.applicationContext,
          HomeActivityMain::class.java
        )
        authRepository.saveLoginResponse(loginResponse)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        application.applicationContext.startActivity(intent)
      } else {
        application.applicationContext.showToast(
          loginResponse.message!!,
          FancyToast.ERROR
        )
      }
    } else {
      application.applicationContext.showToast(
        loginResponse.message!!,
        FancyToast.ERROR
      )
    }
  }

  fun switchActivity(refreshResponse: RefreshResponse) {
    if (refreshResponse.error != true) {
      if (refreshResponse.access != null &&
        refreshResponse.refresh != null
      ) {
        val intent = Intent(
          application.applicationContext,
          HomeActivityMain::class.java
        )
        authRepository.saveTokens(Tokens(refreshResponse.access, refreshResponse.refresh))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        application.applicationContext.startActivity(intent)
      } else {
        application.applicationContext.showToast(
          refreshResponse.message!!,
          FancyToast.ERROR
        )
      }
    } else {
      application.applicationContext.showToast(
        refreshResponse.message!!,
        FancyToast.ERROR
      )
    }
  }

}