package admin.arpan.delivery.ui.fragments

import admin.arpan.delivery.CalculationLogics
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.ui.home.AddOffers
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.settings.SettingActivity
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.getDate
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.viewModels.AuthViewModel
import admin.arpan.delivery.viewModels.HomeViewModel
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment(), OrderOldSubItemRecyclerAdapterInterface {

  private var param1: String? = null
  private var param2: String? = null
  private lateinit var contextMain: Context
  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val TAG = "HomeFragment"
  private lateinit var homeViewModelMainData: HomeViewModelMainData
  private var ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
  private var ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
  private var ordersMainArrayList = ArrayList<OrderItemMain>()
  private val viewModel: HomeViewModel by viewModels()
  private val authViewModel: AuthViewModel by viewModels()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    homeViewModelMainData =
      activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
    loadTopItemsRecyclerData(view)
    loadOrdersMainOneDayData(view)
    view.addCustomOrderButton.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.addCustomOrder)
    }
    view.powerImageView.setOnClickListener {
      homeMainNewInterface.logOutUser()
    }
    view.settingsImageView.setOnClickListener {
      contextMain.startActivity(Intent(contextMain, SettingActivity::class.java))
//            homeMainNewInterface.navigateToFragment(R.id.daManagementFragment)
    }
    view.materialCardViewOrders.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.usersFragment)
    }
  }

  private fun loadOrdersMainOneDayData(view: View) {
    view.noProductsText.visibility = View.GONE
    view.progressBar.visibility = View.VISIBLE
    view.recyclerView.visibility = View.GONE

    val c = Calendar.getInstance() // this takes current date
    c[Calendar.HOUR_OF_DAY] = 0
    c[Calendar.MINUTE] = 0
    c[Calendar.SECOND] = 0

    val d = Calendar.getInstance() // this takes current date
    d[Calendar.HOUR_OF_DAY] = 24
    d[Calendar.MINUTE] = 60
    d[Calendar.SECOND] = 60

    val startTimeMillis = c.timeInMillis
    val endTimeMillis = d.timeInMillis

    LiveDataUtil.observeOnce(
      viewModel.getOrders(
        GetOrdersRequest(
          startTimeMillis,
          endTimeMillis,
          100,
          1
        )
      )
    ) {
      if (it.error == true) {
        view.noProductsText.visibility = View.VISIBLE
        view.noProductsTextView.text = getString(R.string.you_have_no_orders)
        view.progressBar.visibility = View.GONE
        view.recyclerView.visibility = View.GONE
      } else {
        if (it.results.isNullOrEmpty()) {
          view.noProductsText.visibility = View.VISIBLE
          view.noProductsTextView.text = getString(R.string.you_have_no_orders)
          view.progressBar.visibility = View.GONE
          view.recyclerView.visibility = View.GONE
        } else {
          homeViewModelMainData.setOrdersOneDayDataMainList(it.results)
          view.noProductsText.visibility = View.GONE
          view.progressBar.visibility = View.GONE
          view.recyclerView.visibility = View.VISIBLE
          ordersMainArrayList = it.results
          //Only for HomeFragment calculations when the passed data is for one day
          val calculationResult =
            CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayList)
          view.ordersTotalTextView.text = calculationResult.totalOrders.toString()
          view.totalIncomeTextView.text = calculationResult.arpansIncome.toString()
          view.completedOrdersTextView.text = calculationResult.completed.toString()
          view.cancelledOrdersTextView.text = calculationResult.cancelled.toString()
          placeOrderMainData(view)
        }
      }
    }
  }

  private fun placeOrderMainData(view: View) {
    ordersMainHashMap.clear()
    ordersMainOldItemsArrayList.clear()

    for (order in ordersMainArrayList) {
      val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy")
      if (ordersMainHashMap.containsKey(date)) {
        ordersMainHashMap[date]!!.add(order)
      } else {
        ordersMainHashMap[date!!] = ArrayList()
        ordersMainHashMap[date]!!.add(order)
      }
    }
    for (item in ordersMainHashMap.entries) {
      val order = OrderOldItems(
        date = item.key,
        orders = item.value
      )
      order.orders.reverse()
      ordersMainOldItemsArrayList.add(order)
    }
    Collections.sort(ordersMainOldItemsArrayList, kotlin.Comparator { o1, o2 ->
      o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
    })
    ordersMainOldItemsArrayList.reverse()
    val orderAdapterMain = OrderOldMainItemRecyclerAdapter(
      contextMain, ordersMainOldItemsArrayList, this, false,
      showDaStatsMode = false, "", viewModel
    )
    view.recyclerView.layoutManager = LinearLayoutManager(contextMain)
    view.recyclerView.adapter = orderAdapterMain
    view.noProductsText.visibility = View.GONE
    view.progressBar.visibility = View.GONE
    view.recyclerView.visibility = View.VISIBLE
  }

  private fun loadTopItemsRecyclerData(view: View) {
    view.shopMangementButton.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.shopsFragment)
    }
    view.offerManagement.setOnClickListener {
      contextMain.startActivity(Intent(contextMain, AddOffers::class.java))
    }
    view.daManageMentCardView.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.daManagementFragment)
    }
    view.feedBackCardView.setOnClickListener {
      homeMainNewInterface.openFeedBackDialog()
    }
    view.ordersTextView.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.ordersFilterDate)
    }
    view.statisticsCardView.setOnClickListener {
      homeMainNewInterface.navigateToFragment(R.id.shopStatistics)
    }
  }


  override fun openSelectedOrderItemAsDialog(
    position: Int,
    mainItemPositions: Int,
    docId: String,
    userId: String,
    orderItemMain: OrderItemMain
  ) {
    homeMainNewInterface.openSelectedOrderItemAsDialog(
      position,
      mainItemPositions,
      docId,
      userId,
      orderItemMain,
    )
  }
}