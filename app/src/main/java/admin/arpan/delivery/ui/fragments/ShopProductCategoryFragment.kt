package admin.arpan.delivery.ui.fragments

import admin.arpan.delivery.R
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.CategoryViewModel
import admin.arpan.delivery.viewModels.ShopViewModel
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_shop_category.view.*
import kotlinx.android.synthetic.main.fragment_shop_product_category.view.*


@AndroidEntryPoint
class ShopProductCategoryFragment(private val dialogFragmentInterface: DialogFragmentInterface) :
  DialogFragment() {

  private val categoryViewModel: CategoryViewModel by viewModels()
  private val shopViewModel: ShopViewModel by viewModels()

  private lateinit var contextMain: Context

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_shop_product_category, container, false)
  }

  private val allCategories = ArrayList<Category>()
  private val allProductCategories = ArrayList<Category>()
  private lateinit var mCategoriesAdapter: CustomShopProductCatAdapter
  private lateinit var progressDialog: Dialog

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    progressDialog = contextMain.createProgressDialog()
    mCategoriesAdapter = CustomShopProductCatAdapter(
      allCategories,
      contextMain
    )

    view.mainListView.adapter = mCategoriesAdapter

    LiveDataUtil.observeOnce(categoryViewModel.getCategories("product")) {
      if (it.error == true) {
        contextMain.showToast("Failed to fetch data", FancyToast.ERROR)
        dismiss()
      } else {
        allCategories.clear()
        for(i in it.results!!){
          if(!dialogFragmentInterface.getCategories().contains(i)){
            allCategories.add(i)
          }
        }
        mCategoriesAdapter.notifyDataSetChanged()
      }
    }

    view.mainListView.setOnItemClickListener { adapterView, view, i, l ->
      progressDialog.show()
      val oldCategories = dialogFragmentInterface.getCategories()
      val map = HashMap<String, Any>()
      val pCIDs = ArrayList<String>()
      oldCategories.map { cItem -> pCIDs.add(cItem.id!!) }
      pCIDs.add(allCategories[i].id!!)
      map["productCategories"] = pCIDs
      LiveDataUtil.observeOnce(
        shopViewModel.updateShopItem(
          dialogFragmentInterface.getShopId(),
          map
        )
      ) {
        progressDialog.dismiss()
        if (it.id == null) {
          contextMain.showToast("Failed to add", FancyToast.ERROR)
        } else {
          // TODO fix this function to not have to exit the activity
          contextMain.showToast("Added", FancyToast.SUCCESS)
          activity?.finish()
        }
      }
    }

    view.mainListView.setOnItemLongClickListener { adapterView, view, i, l ->
      AlertDialog.Builder(contextMain)
        .setTitle("Are you sure to delete this category?")
        .setMessage("This will only delete the category and not the products.....")
        .setPositiveButton(
          getString(R.string.yes_ok)
        ) { diaInt, _ ->
          diaInt.dismiss()
          progressDialog.show()
          LiveDataUtil.observeOnce(
            categoryViewModel
              .deleteCategory(allCategories[i].id!!)
          ) {
            progressDialog.dismiss()
            if (it.error == true) {
              contextMain.showToast("Error : ${it.message.toString()}", FancyToast.SUCCESS)
            } else {
              allCategories.removeAt(i)
              mCategoriesAdapter.notifyDataSetChanged()
              contextMain.showToast("Deleted category", FancyToast.SUCCESS)
            }
          }
        }
        .setNegativeButton(
          getString(R.string.no_its_ok)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        .create().show()
      true
    }

    view.floatingActionButton.setOnClickListener {
      val dialog = androidx.appcompat.app.AlertDialog.Builder(contextMain).create()
      val dialogView = layoutInflater.inflate(R.layout.dialog_add_shop_category, null)
      dialogView.edt_shop_order.setText((allProductCategories.size + 1).toString())
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
            "product"
          )
          LiveDataUtil.observeOnce(categoryViewModel.createCategoryItem(createCategoryItem)) {
            if (it.id == null) {
              contextMain.showToast("Failed to create", FancyToast.ERROR)
            } else {
              allCategories.add(it)
              allProductCategories.add(it)
              mCategoriesAdapter.notifyDataSetChanged()
            }
            dialog.dismiss()
          }
        }
      }
      dialog.setView(dialogView)
      dialog.show()
    }
  }

}

interface DialogFragmentInterface {
  fun getCategories(): ArrayList<Category>
  fun setCategories(categories: ArrayList<Category>)
  fun getShopId(): String
  fun notifyDataSetChanged()
}

class CustomShopProductCatAdapter(data: ArrayList<Category>, context: Context) :
  ArrayAdapter<Category?>(context, R.layout.category_item_file, data as List<Category?>),
  View.OnClickListener {
  private val dataSet: ArrayList<Category>
  var mContext: Context

  // View lookup cache
  private class ViewHolder {
    var titleTextView: TextView? = null
  }

  override fun onClick(v: View) {

  }

  private var lastPosition = -1
  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    // Get the data item for this position
    var convertView = convertView
    val dataModel: Category? = getItem(position)
    // Check if an existing view is being reused, otherwise inflate the view
    val viewHolder: ViewHolder // view lookup cache stored in tag
    if (convertView == null) {
      viewHolder = ViewHolder()
      val inflater = LayoutInflater.from(context)
      convertView = inflater.inflate(R.layout.category_item_file, parent, false)
      viewHolder.titleTextView =
        convertView!!.findViewById<TextView>(R.id.titleTextView) as TextView?
      convertView.setTag(viewHolder)
    } else {
      viewHolder = convertView.tag as ViewHolder
    }
    lastPosition = position
    viewHolder.titleTextView!!.setText(dataModel!!.name)
    // Return the completed view to render on screen
    return convertView
  }

  init {
    dataSet = data
    mContext = context
  }
}