package admin.arpan.delivery.viewModels

import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.repositories.CategoryRepository
import admin.arpan.delivery.repositories.ShopRepository
import admin.arpan.delivery.utils.networking.responses.*
import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
  private val application: Application,
  private val categoryRepository: CategoryRepository,
) : ViewModel() {

  fun getCategories(type: String) =
    liveData(Dispatchers.IO) {
      var getAllCategoriesResponse: GetAllCategoriesResponse
      try {
        getAllCategoriesResponse = categoryRepository.getCategories(type)
      } catch (e: Exception) {
        getAllCategoriesResponse = GetAllCategoriesResponse(true, e.message.toString())
        e.printStackTrace()
      }
      emit(getAllCategoriesResponse)
    }

  fun getProductCategoriesOfShop(id: String) =
    liveData(Dispatchers.IO) {
      var getAllCategoriesResponse: ArrayList<Category>?
      try {
        getAllCategoriesResponse = categoryRepository.getProductCategoriesOfShop(id)
      } catch (e: Exception) {
        getAllCategoriesResponse = null
        e.printStackTrace()
      }
      emit(getAllCategoriesResponse)
    }

  fun updateCategoryItem(id: String, item: HashMap<String, Any>) = liveData(Dispatchers.IO) {
    var categoryResponse: Category
    try {
      categoryResponse = categoryRepository.updateCategory(id, item)
    } catch (e: Exception) {
      categoryResponse = Category()
      e.printStackTrace()
    }
    emit(categoryResponse)
  }

  fun createCategoryItem(category: Category) =
    liveData(Dispatchers.IO) {
      var categoryResponse: Category
      try {
        categoryResponse = categoryRepository.createCategory(category)
      } catch (e: Exception) {
        categoryResponse = Category()
        e.printStackTrace()
      }
      emit(categoryResponse)
    }

  fun deleteCategory(id: String) =
    liveData(Dispatchers.IO) {
      var defaultResponse: DefaultResponse
      try {
        defaultResponse = categoryRepository.deleteCategory(id)
      } catch (e: Exception) {
        defaultResponse = DefaultResponse(true, "Error : ${e.message.toString()}")
        e.printStackTrace()
      }
      emit(defaultResponse)
    }
}