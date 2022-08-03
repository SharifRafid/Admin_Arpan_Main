package admin.arpan.delivery.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ShopRecyclerAdapterInterface
import admin.arpan.delivery.db.adapter.ShopItemRecyclerAdapter
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.ui.shops.ShopCategoryActivity
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.ShopViewModel
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_shops.view.*
import java.lang.ClassCastException

@AndroidEntryPoint
class ShopsFragment : Fragment(), ShopRecyclerAdapterInterface {

  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val TAG = "ShopsFragment"
  private lateinit var contextMain: Context
  private val viewModel: ShopViewModel by viewModels()
  private lateinit var adapterShops: ShopItemRecyclerAdapter
  private val arrayList = ArrayList<Shop>()
  private lateinit var viewMain: View
  private lateinit var progressDialog: Dialog

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    progressDialog = context.createProgressDialog()
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_shops, container, false)
  }

  override fun onResume() {
    super.onResume()
    if (this::viewMain.isInitialized) {
      loadDataFirestore(viewMain)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewMain = view
    view.addShopsButton.setOnClickListener {
      val intent = Intent(contextMain, AddShop::class.java)
      intent.putExtra("array_size", "0")
      startActivity(intent)
    }
    view.addCategoriessButton.setOnClickListener {
      val intent = Intent(contextMain, ShopCategoryActivity::class.java)
      startActivity(intent)
    }
    loadDataFirestore(view)
    view.swipeRefreshLayout.setOnRefreshListener {
      loadDataFirestore(view)
    }
  }

  private fun loadDataFirestore(view: View) {
    LiveDataUtil.observeOnce(viewModel.getShops()) {
      if (it.error == true) {
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        arrayList.clear()
        arrayList.addAll(it.results)
        val array_size = arrayList.size
        view.mainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
        adapterShops = ShopItemRecyclerAdapter(contextMain, arrayList, "", this)
        adapterShops.setHasStableIds(true)
        view.mainRecyclerView.adapter = adapterShops
        view.swipeRefreshLayout.isRefreshing = false
        view.addShopsButton.setOnClickListener {
          val intent = Intent(contextMain, AddShop::class.java)
          intent.putExtra("array_size", array_size.toString())
          startActivity(intent)
        }
      }
    }
  }

  override fun onSwitchShopStatusCheckedChanged(
    position: Int,
    shop: Shop,
    buttonView: View,
    isChecked: Boolean
  ) {
    if (isChecked) {
      if (shop.open != true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["open"] = true
        LiveDataUtil.observeOnce(viewModel.updateShopItem(shop.id!!, hashMap)) {
          buttonView.isEnabled = true
          if (it.id != null) {
            arrayList[position] = it
            adapterShops.notifyItemChanged(position)
          } else {
            contextMain.showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    } else {
      if (shop.open == true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["open"] = false
        LiveDataUtil.observeOnce(viewModel.updateShopItem(shop.id!!, hashMap)) {
          buttonView.isEnabled = true
          if (it.id != null) {
            arrayList[position] = it
            adapterShops.notifyItemChanged(position)
          } else {
            contextMain.showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    }
  }

  override fun deleteShop(position: Int, shop: Shop) {
    progressDialog.show()
    LiveDataUtil.observeOnce(viewModel.deleteShopItem(shop.id!!)) {
      progressDialog.dismiss()
      if (it.error == true) {
        contextMain.showToast("Error : ${it.message.toString()}", FancyToast.ERROR)
      } else {
        arrayList.removeAt(position)
        adapterShops.notifyItemRemoved(position)
        adapterShops.notifyItemRangeChanged(position, arrayList.size)
      }
    }
  }

  override fun createShareLink(position: Int, id: String, link: String, shareButton: View) {
    val hashMap = HashMap<String, Any>()
    hashMap["dynamicLink"] = link
    LiveDataUtil.observeOnce(viewModel.updateShopItem(id, hashMap)) {
      shareButton.isEnabled = true
      if (it.id != null) {
        arrayList[position] = it
        adapterShops.notifyItemChanged(position)
      } else {
        contextMain.showToast("Failed to update", FancyToast.ERROR)
      }
    }
  }
}