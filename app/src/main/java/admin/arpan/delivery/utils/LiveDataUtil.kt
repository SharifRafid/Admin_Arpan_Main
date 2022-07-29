package admin.arpan.delivery.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

object LiveDataUtil {
  fun <T> observeOnce(liveData: LiveData<T>, observer: Observer<T>) {
    liveData.observeForever(object : Observer<T> {
      override fun onChanged(t: T) {
        liveData.removeObserver(this)
        observer.onChanged(t)
      }
    })
  }
}