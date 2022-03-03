package admin.arpan.delivery.ui.order

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.getDate
import admin.arpan.delivery.utils.parseDate
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import android.widget.DatePicker
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_orders_filter_by_day.view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrdersFilterByDay.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrdersFilterByDay : Fragment(), OrderOldSubItemRecyclerAdapterInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var contextMain : Context
    private lateinit var selectedStartDate : String
    private lateinit var selectedEndDate : String
    private lateinit var homeViewModelMainData: HomeViewModelMainData
    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private val TAG = "OrdersFilterDate"

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders_filter_by_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initVars(view)
        initLogic(view)
    }

    private fun initLogic(view: View) {
        view.setDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yy = calendar.get(Calendar.YEAR)
            val mm = calendar.get(Calendar.MONTH)
            val dd = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(contextMain,object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view2: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    selectedStartDate = ""+dayOfMonth+"-"+month+"-"+year
                    view.setDateButton.text = ""+selectedStartDate
                    loadLastMonthData(view, year, month, dayOfMonth)
                }
            }, yy, mm, dd);
            datePicker.show();
        }
    }

    private fun loadLastMonthData(view : View, yyyy:Int, mm:Int, dd:Int) {
        FirebaseFirestore.getInstance().collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp",
                parseDate("$dd/$mm/$yyyy", "dd/MM/yyyy"))
            .whereLessThanOrEqualTo("orderPlacingTimeStamp",
                parseDate("$dd/$mm/$yyyy", "dd/MM/yyyy")+24*60*60*1000)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener(contextMain as Activity) { value, error ->
                if(value!=null){
                    val tempOrdersThisMonthArrayList = ArrayList<OrderItemMain>()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        tempOrdersThisMonthArrayList.add(o)
                    }
                    if(tempOrdersThisMonthArrayList.isNotEmpty()){
                        placeOrderMainData(tempOrdersThisMonthArrayList, view)
                    }else{
                        Log.e(TAG, "OrderThisMonthArraySnapShotList size is 0")
                    }
                }else{
                    Log.e(TAG, "OrderThisMonthArraySnapShotList is NULL")
                }
            }
    }

    private fun placeOrderMainData(ordersMainArrayList : ArrayList<OrderItemMain>, view: View) {
        val ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
        val ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
//        val ordersCount = ordersMainArrayList.size
//        var income = 0
//        var completed = 0
//        var cancelled = 0
//        for(order in ordersMainArrayList){
//            if(order.orderStatus=="COMPLETED"){
//                if(order.orderCompletedStatus=="CANCELLED"){
//                    cancelled += 1
//                }else{
//                    completed += 1
//                    var arpansCharge = 0
//                    for(productItem in order.products){
//                        arpansCharge += (productItem.product_arpan_profit*productItem.product_item_amount)
//                    }
//                    income += (order.deliveryCharge - order.daCharge) + arpansCharge
//                }
//            }
//        }
        val calculationResult = CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayList)
        view.ordersTotalTextView.text = calculationResult.totalOrders.toString()
        view.totalIncomeTextView.text = calculationResult.arpansIncome.toString()
        view.completedOrdersTextView.text = calculationResult.completed.toString()
        view.cancelledOrdersTextView.text = calculationResult.cancelled.toString()

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
            ordersMainOldItemsArrayList.add(
                order
            )
        }
        Collections.sort(ordersMainOldItemsArrayList, kotlin.Comparator { o1, o2 ->
            o1.orders[0].orderPlacingTimeStamp.compareTo(o2.orders[0].orderPlacingTimeStamp)
        })
        ordersMainOldItemsArrayList.reverse()
        view.recyclerView.layoutManager = LinearLayoutManager(contextMain)
        val orderAdapterMain = OrderOldMainItemRecyclerAdapter(contextMain, ordersMainOldItemsArrayList, this,
            showStats = true,
            showDaStatsMode = false,
            da_category = ""
        )
        view.recyclerView.adapter = orderAdapterMain

        view.noProductsText.visibility = View.GONE
        view.progressBar.visibility = View.GONE
        view.recyclerView.visibility = View.VISIBLE
    }

    private fun initVars(view: View) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
        view.title_text_view.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }
    }

    override fun openSelectedOrderItemAsDialog(
        position: Int,
        mainItemPositions: Int,
        docId: String,
        userId: String,
        orderItemMain: OrderItemMain
    ) {
        val bundle = Bundle()
        bundle.putString("orderID",docId)
        bundle.putString("customerId",userId)
        homeMainNewInterface.navigateToFragment(R.id.orderHistoryFragmentNew22, bundle)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OrdersFilterByDay.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OrdersFilterByDay().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}