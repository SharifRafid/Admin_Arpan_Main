package admin.arpan.delivery.ui.da

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.DaItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.DaItemRecyclerAdapterInterface
import admin.arpan.delivery.models.User
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.networking.responses.DaItemResponse
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.DAViewModel
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_da.*
import kotlinx.android.synthetic.main.fragment_da_management.view.*
import java.lang.ClassCastException
import java.util.*

@AndroidEntryPoint
class DaManagementFragment : Fragment(), DaItemRecyclerAdapterInterface {
  private lateinit var contextMain: Context
  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val TAG = "DaManagementFragment"
  private val daViewModel: DAViewModel by viewModels()
  lateinit var progressDialog: Dialog
  private lateinit var daItemRecyclerAdapter: DaItemRecyclerAdapter
  private val daList = ArrayList<DaItemResponse>()

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
    return inflater.inflate(R.layout.fragment_da_management, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    daItemRecyclerAdapter = DaItemRecyclerAdapter(
      contextMain, daList,
      homeMainNewInterface,
      this
    )
    view.mainRecyclerView.adapter = daItemRecyclerAdapter
    view.mainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
    view.titleTextView.setOnClickListener {
      homeMainNewInterface.callOnBackPressed()
    }
    fetchData()
    view.addShopsButton.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.addDaFragment)
    }
    view.swipeRefreshLayout.setOnRefreshListener {
      view.swipeRefreshLayout.isRefreshing = false
      fetchData()
    }
  }

  private fun fetchData() {
    progressDialog.show()
    LiveDataUtil.observeOnce(daViewModel.getAllItems()) {
      progressDialog.dismiss()
      if (it.error == true) {
        context?.showToast("Failed to find", FancyToast.ERROR)
      } else {
        daList.clear()
        daList.addAll(it.results)
        daItemRecyclerAdapter.notifyDataSetChanged()
        progressDialog.dismiss()
      }
    }
  }

  override fun onSwitchDAStatusCheckedChanged(
    position: Int,
    da: User,
    buttonView: View,
    isChecked: Boolean
  ) {
    if (isChecked) {
      if (da.daStatus != true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["daStatus"] = true
        LiveDataUtil.observeOnce(daViewModel.updateItem(da.id!!, hashMap)) {
          buttonView.isEnabled = true
          if (it.id != null) {
            daList[position].daItem = it
            daItemRecyclerAdapter.notifyItemChanged(position)
          } else {
            contextMain.showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    } else {
      if (da.daStatus == true) {
        buttonView.isEnabled = false
        val hashMap = HashMap<String, Any>()
        hashMap["daStatus"] = false
        LiveDataUtil.observeOnce(daViewModel.updateItem(da.id!!, hashMap)) {
          buttonView.isEnabled = true
          if (it.id != null) {
            daList[position].daItem = it
            daItemRecyclerAdapter.notifyItemChanged(position)
          } else {
            contextMain.showToast("Failed to update", FancyToast.ERROR)
          }
        }
      }
    }
  }

  override fun deleteDA(position: Int, daAgent: User) {
    progressDialog.show()
    LiveDataUtil.observeOnce(daViewModel.deleteItem(daAgent.id!!)) {
      progressDialog.dismiss()
      if (it.error == true) {
        contextMain.showToast("Failed", FancyToast.SUCCESS)
      } else {
        daList.removeAt(position)
        daItemRecyclerAdapter.notifyItemRemoved(position)
        daItemRecyclerAdapter.notifyItemRangeChanged(position, daList.size)
        contextMain.showToast("Success Deleted", FancyToast.SUCCESS)
      }
    }
  }

}