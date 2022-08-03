package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopCategoryItem
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.CategoryViewModel
import admin.arpan.delivery.viewModels.ShopViewModel
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_shop_category.*
import kotlinx.android.synthetic.main.dialog_add_shop_category.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@AndroidEntryPoint
class ShopCategoryActivity : AppCompatActivity() {

  val category_keys = ArrayList<String>()
  val category_names = ArrayList<String>()
  private val viewModel: CategoryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_shop_category)

    initVars()
    initLogic()
  }

  private fun initLogic() {
    LiveDataUtil.observeOnce(viewModel.getCategories(type = "shop")) { allCategoriesResponse ->
      if (allCategoriesResponse.error != true) {
        category_keys.clear()
        category_names.clear()
        val categoryItemsArray = allCategoriesResponse.results as ArrayList<Category>
        for (category in categoryItemsArray) {
          category_names.add(category.name.toString())
          category_keys.add(category.id.toString())
        }
        val adapter = ArrayAdapter(
          this@ShopCategoryActivity,
          R.layout.custom_spinner_view,
          category_names
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
          val dialog = AlertDialog.Builder(this).create()
          val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_shop_category, null)
          dialogView.edt_shop_name.setText(categoryItemsArray[position].name)
          dialogView.edt_shop_key.setText(categoryItemsArray[position].key)
          dialogView.edt_shop_order.setText(categoryItemsArray[position].order.toString())
          dialogView.addProductCategoriesButton.text = "Save Category"
          dialogView.addProductCategoriesButton.setOnClickListener {
            if (dialogView.edt_shop_name.text.isNotEmpty() &&
              dialogView.edt_shop_key.text.isNotEmpty() &&
              dialogView.edt_shop_order.text.isNotEmpty()
            ) {
              dialog.setCancelable(false)
              dialog.setCanceledOnTouchOutside(false)
              dialogView.addProductCategoriesButton.isEnabled = false
              dialogView.addProductCategoriesButton.text = "Adding..."
              val hashMap = HashMap<String, Any>()
              hashMap["name"] =
                dialogView.edt_shop_name.text.toString()
              hashMap["key"] =
                dialogView.edt_shop_key.text.toString()
              hashMap["order"] =
                dialogView.edt_shop_order.text.toString()
              hashMap["type"] = "shop"
              LiveDataUtil.observeOnce(
                viewModel.updateCategoryItem(
                  categoryItemsArray[position].id!!, hashMap
                )
              ) {
                if (it.id != null) {
                  categoryItemsArray[position] = it
                  category_names[position] = dialogView.edt_shop_name.text.toString()
                  category_keys[position] = dialogView.edt_shop_key.text.toString()
                  adapter.notifyDataSetChanged()
                } else {
                  showToast("Updated", FancyToast.SUCCESS)
                }
                dialog.dismiss()
              }
            }
          }
          dialog.setView(dialogView)
          dialog.show()
        }
        listView.setOnItemLongClickListener { parent, view, position, id ->
          AlertDialog.Builder(this)
            .setTitle("Delete Category?")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
              LiveDataUtil.observeOnce(viewModel.deleteCategory(categoryItemsArray[position].id!!)) {
                if (it.error == true) {
                  showToast("Error : ${it.message.toString()}", FancyToast.SUCCESS)
                } else {
                  categoryItemsArray.removeAt(position)
                  category_names.removeAt(position)
                  category_keys.removeAt(position)
                  adapter.notifyDataSetChanged()
                  showToast("Deleted", FancyToast.SUCCESS)
                }
                dialog.dismiss()
              }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
              dialog.dismiss()
            })
            .create().show()
          true
        }

        addCategoriessButton.setOnClickListener {
          val dialog = AlertDialog.Builder(this).create()
          val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_shop_category, null)
          dialogView.edt_shop_order.setText((category_names.size+1).toString())
          dialogView.addProductCategoriesButton.text = "Add Category"
          dialogView.addProductCategoriesButton.setOnClickListener {
            if (dialogView.edt_shop_name.text.isNotEmpty() &&
              dialogView.edt_shop_key.text.isNotEmpty() &&
              dialogView.edt_shop_order.text.isNotEmpty()
            ) {
              dialog.setCancelable(false)
              dialog.setCanceledOnTouchOutside(false)
              dialogView.addProductCategoriesButton.isEnabled = false
              dialogView.addProductCategoriesButton.text = "Saving..."
              val createCategoryItem = Category(
                null,
                dialogView.edt_shop_key.text.toString(),
                dialogView.edt_shop_name.text.toString(),
                dialogView.edt_shop_order.text.toString().toInt(),
                "shop"
              )
              LiveDataUtil.observeOnce(viewModel.createCategoryItem(createCategoryItem)) {
                if (it.id == null) {
                  showToast("Failed to create", FancyToast.ERROR)
                } else {
                  categoryItemsArray.add(it)
                  category_names.add(dialogView.edt_shop_name.text.toString())
                  category_keys.add(dialogView.edt_shop_key.text.toString())
                  adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
              }
            }
          }
          dialog.setView(dialogView)
          dialog.show()
        }

      } else {
        showToast("Error : ${allCategoriesResponse.message.toString()}", FancyToast.ERROR)
      }
    }
  }

  private fun initVars() {

  }

  fun addShopCategoryClick(view: View) {

  }
}