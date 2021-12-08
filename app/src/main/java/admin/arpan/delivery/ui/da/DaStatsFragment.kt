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
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.getDate
import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_da_stats.view.*
import kotlinx.android.synthetic.main.fragment_da_stats.view.ordersDateMonthRadioGroup
import kotlinx.android.synthetic.main.fragment_orders_filter_date.view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList


class DaStatsFragment : Fragment(), OrderOldSubItemRecyclerAdapterInterface {

    private lateinit var mainView: View
    private var TAG = "DaStatsFragment"
    var selectedDaAgent = DaAgent()
    private lateinit var contextMain : Context
    lateinit var homeMainNewInterface: HomeMainNewInterface
    private lateinit var homeViewModelMainData: HomeViewModelMainData
    private lateinit var observerMain : Observer<ArrayList<OrderItemMain>>
    var ordersMainOldItemsArrayListCompleted = ArrayList<OrderOldItems>()
    private var ordersMainHashMapCompletedOrders = HashMap<String, ArrayList<OrderItemMain>>()

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
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!

        view.title_text_view2.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }

        mainView = view
        selectedDaAgent = DaAgent()

        selectedDaAgent.key = arguments?.getString("key").toString()
        selectedDaAgent.da_uid = arguments?.getString("da_uid").toString()
        selectedDaAgent.da_name = arguments?.getString("da_name").toString()
        selectedDaAgent.da_mobile = arguments?.getString("da_mobile").toString()
        selectedDaAgent.da_bkash = arguments?.getString("da_bkash").toString()
        selectedDaAgent.da_password = arguments?.getString("da_password").toString()
        selectedDaAgent.da_blood_group = arguments?.getString("da_blood_group").toString()
        selectedDaAgent.da_category = arguments?.getString("da_category").toString()
        selectedDaAgent.da_image = arguments?.getString("da_image").toString()
        selectedDaAgent.da_address = arguments?.getString("da_address").toString()
        selectedDaAgent.da_status_active = arguments?.getString("da_status_active").toString().toBoolean()

        view.ordersDateMonthRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.pendingRadioButton){
                setThisMonthView()
            }else{
                setLastMonthView()
            }
        }

        observerMain = Observer {
            placeDataOnView(it)
            if(it.isNotEmpty()){
                homeViewModelMainData.getLastMonthOrdersMainData().removeObservers(requireActivity())
                homeViewModelMainData.getThisMonthOrdersArrayList().removeObservers(requireActivity())
            }
        }

        view.ordersDateMonthRadioGroup.check(R.id.pendingRadioButton)
    }

    private fun placeDataOnView(it: java.util.ArrayList<OrderItemMain>?) {
        val orderList = ArrayList<OrderItemMain>()
        for(orderItem in it!!){
            if(orderItem.daID == selectedDaAgent.key){
                orderList.add(orderItem)
            }
        }
        placeOrderMainDataCompletedOrders(orderList)
    }

    fun placeOrderMainDataCompletedOrders(ordersMainArrayListCompleted : ArrayList<OrderItemMain>) {
        ordersMainHashMapCompletedOrders.clear()
        ordersMainOldItemsArrayListCompleted.clear()
        for(order in ordersMainArrayListCompleted){
            val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy").toString()
            if(ordersMainHashMapCompletedOrders.containsKey(date)){
                ordersMainHashMapCompletedOrders[date]!!.add(order)
            }else{
                ordersMainHashMapCompletedOrders[date!!] = ArrayList()
                ordersMainHashMapCompletedOrders[date]!!.add(order)
            }
        }
        val calculationResult = CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayListCompleted)
        if(selectedDaAgent.da_category=="পারমানেন্ট"){
            mainView.myIncomeTextView.text = calculationResult.agentsIncomePermanent.toString()
            mainView.totalOrderThisMonthTextView.text = calculationResult.totalOrders.toString()
            mainView.arpanBokeyaTextView.text = calculationResult.agentsDueToArpan.toString()
        }else{
            mainView.myIncomeTextView.text = calculationResult.agentsIncome.toString()
            mainView.totalOrderThisMonthTextView.text = calculationResult.totalOrders.toString()
            mainView.arpanBokeyaTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
        }
        for(item in ordersMainHashMapCompletedOrders.entries){
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
        mainView.thisMonthDaRecycler.adapter = OrderOldMainItemRecyclerAdapter(contextMain, ordersMainOldItemsArrayListCompleted, this, true, true, selectedDaAgent.da_category)
    }

    private fun setLastMonthView() {
        if(homeViewModelMainData.getLastMonthOrdersMainData().value!!.isEmpty()){
            homeMainNewInterface.loadLastMonthOrdersMainData()
        }
        homeViewModelMainData.getLastMonthOrdersMainData().observe(requireActivity(), observerMain)
    }

    private fun setThisMonthView() {
        if(homeViewModelMainData.getThisMonthOrdersArrayList().value!!.isEmpty()){
            homeMainNewInterface.loadThisMonthOrdersMainData()
        }
        homeViewModelMainData.getThisMonthOrdersArrayList().observe(requireActivity(), observerMain)
    }

    override fun openSelectedOrderItemAsDialog(
        position: Int,
        mainItemPositions: Int,
        docId: String,
        userId: String,
        orderItemMain: OrderItemMain
    ) {
        homeMainNewInterface.openSelectedOrderItemAsDialog(position, mainItemPositions, docId, userId, orderItemMain,)
    }

}