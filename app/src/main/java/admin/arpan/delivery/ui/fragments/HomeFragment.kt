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
import admin.arpan.delivery.db.adapter.TopMenuRecyclerAdapter
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.ui.home.AddOffers
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.order.OrdresActivity
import admin.arpan.delivery.ui.settings.SettingActivity
import admin.arpan.delivery.utils.getDate
import androidx.recyclerview.widget.GridLayoutManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_user_feed_back.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OrderOldSubItemRecyclerAdapterInterface {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var contextMain : Context
    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private val TAG = "HomeFragment"
    private lateinit var homeViewModelMainData: HomeViewModelMainData
    private var ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
    private var ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
    private var ordersMainArrayList =  ArrayList<OrderItemMain>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextMain = context
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
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
        if(homeViewModelMainData.getOrdersOneDayDataMainList().value.isNullOrEmpty()){
            homeMainNewInterface.loadOrdersOneDayList()
        }
        homeViewModelMainData.getOrdersOneDayDataMainList().observe(requireActivity(), Observer {
            if(it.isEmpty()){
                view.noProductsText.visibility = View.VISIBLE
                view.noProductsTextView.text = getString(R.string.you_have_no_orders)
                view.progressBar.visibility = View.GONE
                view.recyclerView.visibility = View.GONE
            }else{
                view.noProductsText.visibility = View.GONE
                view.progressBar.visibility = View.GONE
                view.recyclerView.visibility = View.VISIBLE
                ordersMainArrayList = it
                Log.e(TAG, it.size.toString())
                //Only for HomeFragment calculations when the passed data is for one day
                val calculationResult = CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayList)
                view.ordersTotalTextView.text = calculationResult.totalOrders.toString()
                view.totalIncomeTextView.text = calculationResult.arpansIncome.toString()
                view.completedOrdersTextView.text = calculationResult.completed.toString()
                view.cancelledOrdersTextView.text = calculationResult.cancelled.toString()
                placeOrderMainData(view)
            }
        })
    }

    private fun placeOrderMainData(view : View) {
        ordersMainHashMap.clear()
        ordersMainOldItemsArrayList.clear()

        for(order in ordersMainArrayList){
            val date = getDate(order.orderPlacingTimeStamp, "dd-MM-yyyy")
            if(ordersMainHashMap.containsKey(date)){
                ordersMainHashMap[date]!!.add(order)
            }else{
                ordersMainHashMap[date!!] = ArrayList()
                ordersMainHashMap[date]!!.add(order)
            }
        }
        for(item in ordersMainHashMap.entries){
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
        val orderAdapterMain = OrderOldMainItemRecyclerAdapter(contextMain, ordersMainOldItemsArrayList, this, false,
            showDaStatsMode = false, "")
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun openSelectedOrderItemAsDialog(position: Int, mainItemPositions: Int, docId: String, userId: String, orderItemMain: OrderItemMain) {
        homeMainNewInterface.openSelectedOrderItemAsDialog(position, mainItemPositions, docId, userId, orderItemMain,)
    }
}