package admin.arpan.delivery.ui.products

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ProductItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.ProductRecyclerAdapterInterface
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Product
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.ui.fragments.DialogFragmentInterface
import admin.arpan.delivery.ui.fragments.ShopProductCategoryFragment
import admin.arpan.delivery.ui.shops.UpdateShop
import admin.arpan.delivery.utils.*
import admin.arpan.delivery.viewModels.CategoryViewModel
import admin.arpan.delivery.viewModels.ProductViewModel
import admin.arpan.delivery.viewModels.ShopViewModel
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.category_item_file.view.*
import kotlinx.android.synthetic.main.dialog_add_category.view.addProductCategoriesButton
import kotlinx.android.synthetic.main.dialog_add_category.view.edt_shop_name
import kotlinx.android.synthetic.main.dialog_add_shop_category.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class ProductsActivity : AppCompatActivity(), ProductRecyclerAdapterInterface {

  var currentSelectedProductMainIndex = 0
  private var shop_key = ""
  private lateinit var shop: Shop
  private var shop_category_key = ""
  private var shop_category_tag_name = ""
  private lateinit var progressDialog: Dialog
  private val categoryItemsArray = ArrayList<Category>()
  private val keysList = ArrayList<String>()
  private val namesList = ArrayList<String>()
  private lateinit var categories_item_array_adapter: ArrayAdapter<String>
  lateinit var productsItemAdapterMain: ProductItemRecyclerAdapter
  val productsMainArrayList = ArrayList<Product>()
  private val productViewModel: ProductViewModel by viewModels()
  private val categoryViewModel: CategoryViewModel by viewModels()
  private val shopViewModel: ShopViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_products)
    initVars()
    initLogic()
  }

  private fun initVars() {
    progressDialog = createProgressDialog()
  }

  private fun initLogic() {
    shop = getGsonParser()!!.fromJson(intent.getStringExtra("data").toString(), Shop::class.java)
    shop_key = shop.id!!
    title_text_view.text = shop.name
    title_text_view.setOnClickListener {
      val intent = Intent(this, UpdateShop::class.java)
      intent.putExtra("data", getGsonParser()!!.toJson(shop))
      startActivity(intent)
    }
    loadFirestoreShopCategories()
    categories_item_array_adapter = ArrayAdapter<String>(
      this@ProductsActivity,
      R.layout.category_item_file,
      R.id.titleTextView,
      namesList
    )
  }

  private fun loadFirestoreShopCategories() {
    progressDialog.show()
    LiveDataUtil.observeOnce(categoryViewModel.getProductCategoriesOfShop(shop_key)) {
      progressDialog.dismiss()
      if (it == null) {
        showToast("Failed to fetch categories", FancyToast.ERROR)
      } else {
        namesList.clear()
        keysList.clear()
        categoryItemsArray.clear()
        categoryItemsArray.addAll(it)
        Collections.sort(categoryItemsArray, kotlin.Comparator { o1, o2 ->
          (o1.order!!).compareTo(o2.order!!)
        })
        for (item in categoryItemsArray) {
          keysList.add(item.id!!)
          namesList.add(item.name!!)
        }
        mainRecyclerView.adapter = categories_item_array_adapter

        categories_item_array_adapter.notifyDataSetChanged()
        mainRecyclerView.setOnItemClickListener { parent, view, position, id ->
          view.isSelected = true
          view.titleTextView.isSelected = true
          shop_category_key = keysList[position]
          shop_category_tag_name = categoryItemsArray[position].id!!
          loadProductsFromCategory()
        }
        mainRecyclerView.setOnItemLongClickListener { parent, view, position, id ->
          val dialogAskingEditOrDelete = AlertDialog.Builder(this)
          dialogAskingEditOrDelete.setPositiveButton(
            "EDIT"
          ) { dialogParent, _ ->
            dialogParent.dismiss()
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this).create()
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
                hashMap["type"] = "product"
                LiveDataUtil.observeOnce(
                  categoryViewModel.updateCategoryItem(
                    categoryItemsArray[position].id!!, hashMap
                  )
                ) { categoryItem ->
                  if (categoryItem.id != null) {
                    categoryItemsArray[position] = categoryItem
                    namesList[position] = dialogView.edt_shop_name.text.toString()
                    keysList[position] = dialogView.edt_shop_key.text.toString()
                    categories_item_array_adapter.notifyDataSetChanged()
                  } else {
                    showToast("Updated category", FancyToast.SUCCESS)
                  }
                  dialog.dismiss()
                }
              }
            }
            dialog.setView(dialogView)
            dialog.show()
          }
          dialogAskingEditOrDelete.setNegativeButton(
            "DELETE"
          ) { dialog, _ ->
            dialog.dismiss()
            AlertDialog.Builder(this)
              .setTitle("Are you sure to delete this category?")
              .setMessage("This will remove this category from this shop")
              .setPositiveButton(
                getString(R.string.yes_ok)
              ) { diaInt, _ ->
                diaInt.dismiss()
                progressDialog.show()
                LiveDataUtil.observeOnce(
                  shopViewModel
                    .removeCategoryFromShop(shop_key, categoryItemsArray[position].id!!)
                ) { defaultR ->
                  progressDialog.dismiss()
                  if (defaultR.error == true) {
                    showToast("Error : ${defaultR.message.toString()}", FancyToast.SUCCESS)
                  } else {
                    categoryItemsArray.removeAt(position)
                    namesList.removeAt(position)
                    keysList.removeAt(position)
                    categories_item_array_adapter.notifyDataSetChanged()
                    showToast("Deleted category", FancyToast.SUCCESS)
                  }
                }
              }
              .setNegativeButton(
                getString(R.string.no_its_ok)
              ) { dialogInterface, _ -> dialogInterface.dismiss() }
              .create().show()
          }
          dialogAskingEditOrDelete.create().show()
          true
        }
        mainRecyclerView.choiceMode = ListView.CHOICE_MODE_SINGLE
        if (keysList.isNotEmpty()) {
          mainRecyclerView.setItemChecked(0, true)
          shop_category_key = keysList[0]
          shop_category_tag_name = categoryItemsArray[0].id!!
          loadProductsFromCategory()
        }
      }
    }
  }

  fun addNewCategory(view: View) {
    val shopProductCategoryFragment = ShopProductCategoryFragment(object : DialogFragmentInterface {
      override fun getCategories(): ArrayList<Category> {
        return categoryItemsArray
      }

      override fun setCategories(categories: ArrayList<Category>) {
        categoryItemsArray.clear()
        categoryItemsArray.addAll(categories)
        Log.e("CL", categories.size.toString())
      }

      override fun getShopId(): String {
        return shop_key
      }

      override fun notifyDataSetChanged() {
        categories_item_array_adapter.notifyDataSetChanged()

      }
    })
    shopProductCategoryFragment.show(supportFragmentManager, "ProductCategoryFragment")
  }

  fun addNewProduct(view: View) {
    val bundle = Bundle()
    bundle.putString("shop_key", shop_key)
    bundle.putString("product_category_key", shop_category_key)
    bundle.putString("product_category", shop_category_tag_name)
    bundle.putString("product_order", (productsMainArrayList.size + 1).toString())
    val addProductFragment = AddProductFragmennt()
    addProductFragment.arguments = bundle
    addProductFragment.show(supportFragmentManager, "")
  }

  private fun loadProductsFromCategory() {
    progressDialog.show()
    LiveDataUtil.observeOnce(productViewModel.getProductsByCategoryId(shop_category_tag_name, shop_key)) {
      progressDialog.dismiss()
      if (it.error == true) {
        showToast("Error : ${it.message}", FancyToast.ERROR)
      } else {
        productsMainArrayList.clear()
        productsMainArrayList.addAll(it.results!!)
        productsItemAdapterMain =
          ProductItemRecyclerAdapter(
            this@ProductsActivity,
            this@ProductsActivity,
            productsMainArrayList, shop.name!!, shop_category_key, shop_key,
            this
          )
        productsRecyclerView.adapter = productsItemAdapterMain
        val linearLayoutManager = LinearLayoutManager(this@ProductsActivity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        productsRecyclerView.layoutManager = linearLayoutManager
      }
    }

  }

  override fun onSwitchProductStatusCheckedChanged(
    position: Int,
    product: Product,
    buttonView: View,
    isChecked: Boolean
  ) {

    if (isChecked) {
      if (product.inStock != true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["inStock"] = true
        LiveDataUtil.observeOnce(
          productViewModel
            .updateProductItem(product.id!!, hashMap)
        ) {
          buttonView.isEnabled = true
          if (it.id != null) {
            productsMainArrayList[position] = it
            productsItemAdapterMain.notifyItemChanged(position)
          } else {
            showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    } else {
      if (product.inStock == true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["inStock"] = false
        LiveDataUtil.observeOnce(
          productViewModel
            .updateProductItem(product.id!!, hashMap)
        ) {
          buttonView.isEnabled = true
          if (it.id != null) {
            productsMainArrayList[position] = it
            productsItemAdapterMain.notifyItemChanged(position)
          } else {
            showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    }
  }

  override fun deleteProduct(position: Int, product: Product) {
    progressDialog.show()
    LiveDataUtil.observeOnce(productViewModel.deleteProduct(product.id!!)) {
      progressDialog.dismiss()
      if (it.error == true) {
        showToast("Error : ${it.message.toString()}", FancyToast.ERROR)
      } else {
        productsMainArrayList.removeAt(position)
        productsItemAdapterMain.notifyItemRemoved(position)
        productsItemAdapterMain.notifyItemRangeChanged(position, productsMainArrayList.size)
      }
    }
  }

}