package admin.arpan.delivery.ui.da

import admin.arpan.delivery.CalculationLogics
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.models.User
import admin.arpan.delivery.models.enums.OrderStatus
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.*
import admin.arpan.delivery.utils.networking.requests.GetOrdersRequest
import admin.arpan.delivery.viewModels.DAViewModel
import admin.arpan.delivery.viewModels.OrderViewModel
import android.app.Dialog
import android.content.Context
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_da_stats.view.*
import kotlinx.android.synthetic.main.fragment_da_stats.view.ordersDateMonthRadioGroup
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class DaStatsFragment : Fragment(), OrderOldSubItemRecyclerAdapterInterface {

  private lateinit var mainView: View
  private var TAG = "DaStatsFragment"
  var selectedDaAgent = User()
  private lateinit var contextMain: Context
  lateinit var homeMainNewInterface: HomeMainNewInterface
  var ordersMainOldItemsArrayListCompleted = ArrayList<OrderOldItems>()
  private var ordersMainHashMapCompletedOrders = HashMap<String, ArrayList<OrderItemMain>>()
  private val ordersViewModel: OrderViewModel by viewModels()
  private lateinit var dialog: Dialog

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_da_stats, container, false)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    dialog = context.createProgressDialog()
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.title_text_view2.setOnClickListener {
      homeMainNewInterface.callOnBackPressed()
    }

    mainView = view
    selectedDaAgent = getGsonParser()!!.fromJson(
      requireArguments().getString("data", "").toString(),
      User::class.java
    )
    view.ordersDateMonthRadioGroup.setOnCheckedChangeListener { group, checkedId ->
      if (checkedId == R.id.pendingRadioButton) {
        setThisMonthView()
      } else {
        setLastMonthView()
      }
    }

    view.ordersDateMonthRadioGroup.check(R.id.pendingRadioButton)
  }

  fun placeOrderMainDataCompletedOrders(ordersMainArrayListCompleted: ArrayList<OrderItemMain>) {
    ordersMainHashMapCompletedOrders.clear()
    ordersMainOldItemsArrayListCompleted.clear()
    for (order in ordersMainArrayListCompleted) {
      val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy").toString()
      if (ordersMainHashMapCompletedOrders.containsKey(date)) {
        ordersMainHashMapCompletedOrders[date]!!.add(order)
      } else {
        ordersMainHashMapCompletedOrders[date] = ArrayList()
        ordersMainHashMapCompletedOrders[date]!!.add(order)
      }
    }
    val calculationResult =
      CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayListCompleted)
    if (selectedDaAgent.daCategory == Constants.DA_PERM) {
      mainView.myIncomeTextView.text = calculationResult.agentsIncomePermanent.toString()
      mainView.totalOrderThisMonthTextView.text = calculationResult.totalOrders.toString()
      mainView.arpanBokeyaTextView.text = calculationResult.agentsDueToArpan.toString()
    } else {
      mainView.myIncomeTextView.text = calculationResult.agentsIncome.toString()
      mainView.totalOrderThisMonthTextView.text = calculationResult.totalOrders.toString()
      mainView.arpanBokeyaTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
    }
    for (item in ordersMainHashMapCompletedOrders.entries) {
      val order = OrderOldItems(
        date = item.key,
        orders = item.value
      )
      order.orders.reverse()
      ordersMainOldItemsArrayListCompleted.add(order)
    }
    Collections.sort(ordersMainOldItemsArrayListCompleted, kotlin.Comparator { o1, o2 ->
      o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
    })
    ordersMainOldItemsArrayListCompleted.reverse()
    mainView.thisMonthDaRecycler.layoutManager = LinearLayoutManager(contextMain)
    mainView.thisMonthDaRecycler.adapter = OrderOldMainItemRecyclerAdapter(
      contextMain,
      ordersMainOldItemsArrayListCompleted,
      this,
      true,
      true,
      selectedDaAgent.daCategory!!,
      null
    )
  }

  private fun setLastMonthView() {
    val c = Calendar.getInstance() // this takes current date
    c.add(Calendar.MONTH, -1)
    c[Calendar.DAY_OF_MONTH] = 1
    c[Calendar.HOUR_OF_DAY] = 0
    val d = Calendar.getInstance() // this takes current date
    d.add(Calendar.MONTH, -1)
    d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
    d[Calendar.HOUR_OF_DAY] = 24
    val getOrdersRequest = GetOrdersRequest()
    getOrdersRequest.startTimeMillis = c.timeInMillis
    getOrdersRequest.endTimeMillis = d.timeInMillis
    getOrdersRequest.limit = 100
    getOrdersRequest.page = 1
    getOrdersRequest.orderStatus = OrderStatus.COMPLETED
    getOrdersRequest.daId = selectedDaAgent.id
    dialog.show()
    LiveDataUtil.observeOnce(ordersViewModel.getAllItems(getOrdersRequest)) {
      dialog.dismiss()
      if(it.error == true){
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      }else{
        placeOrderMainDataCompletedOrders(it.results!!)
      }
    }
  }

  private fun setThisMonthView() {
    val c = Calendar.getInstance() // this takes current date
    c[Calendar.DAY_OF_MONTH] = 1
    c[Calendar.HOUR_OF_DAY] = 0
    val d = Calendar.getInstance() // this takes current date
    d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
    d[Calendar.HOUR_OF_DAY] = 24
    val getOrdersRequest = GetOrdersRequest()
    getOrdersRequest.startTimeMillis = c.timeInMillis
    getOrdersRequest.endTimeMillis = d.timeInMillis
    getOrdersRequest.limit = 100
    getOrdersRequest.page = 1
    getOrdersRequest.orderStatus = OrderStatus.COMPLETED
    getOrdersRequest.daId = selectedDaAgent.id
    dialog.show()
    LiveDataUtil.observeOnce(ordersViewModel.getAllItems(getOrdersRequest)) {
      dialog.dismiss()
      if(it.error == true){
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      }else{
        placeOrderMainDataCompletedOrders(it.results!!)
      }
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