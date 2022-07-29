package admin.arpan.delivery.ui.order

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.model.DaStatusItem
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.db.model.OrderItemMain
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.ui.home.HomeViewModel
import admin.arpan.delivery.utils.getDate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_offers.*
import kotlinx.android.synthetic.main.activity_ordres.*
import kotlinx.android.synthetic.main.activity_ordres.title_text_view
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OrdresActivity : AppCompatActivity() , OrderOldSubItemRecyclerAdapterInterface{

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var ordersMainArrayList =  ArrayList<OrderItemMain>()
    private var ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
    var ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
    lateinit var orderAdapterMain : OrderOldMainItemRecyclerAdapter
    var daStatusList = ArrayList<DaStatusItem>()
    var selectedRecyclerAdapterItem = 0
    var mainItemPositionsRecyclerAdapter = 0
    private var startTimeMonthMillis = 0L
    private var endTimeMonthMillis = 0L
    val locationsArrayList = ArrayList<LocationItem>()
    private lateinit var homeViewModel : HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordres)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        initVars()
        title_text_view.setOnClickListener {
            onBackPressed()
        }
        FirebaseDatabase.getInstance()
            .reference
            .child("data")
            .child("delivery_charges")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    locationsArrayList.clear()
                    for (snap in snapshot.children) {
                        val locationItem = LocationItem(
                            key = snap.key.toString(),
                            locationName = snap.child("name").value.toString(),
                            deliveryCharge = snap.child("deliveryCharge").value.toString().toInt(),
                            daCharge = snap.child("daCharge").value.toString().toInt(),
                        )
                        if (snap.child("deliveryChargeClient").value != null) {
                            locationItem.deliveryChargeClient =
                                snap.child("deliveryChargeClient").value.toString().toInt()
                        }
                        locationsArrayList.add(locationItem)
                    }
                    homeViewModel.setLocationsArrayList(locationsArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
        //initLogics()
    }

    private fun initLogics() {
        loadDataFirstTime()
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        orderAdapterMain = OrderOldMainItemRecyclerAdapter(
          this,
          ordersMainOldItemsArrayList,
          this,
          false,
          false,
          "",
          null
        )
        val c = Calendar.getInstance() // this takes current date
        c[Calendar.DAY_OF_MONTH] = 1
        c[Calendar.HOUR_OF_DAY] = 0

        val d = Calendar.getInstance() // this takes current date
        d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        d[Calendar.HOUR_OF_DAY] = 24

        startTimeMonthMillis = c.timeInMillis
        endTimeMonthMillis = d.timeInMillis

        loadDataFirstTime()
    }

    private fun loadDataFirstTime() {
        if(FirebaseAuth.getInstance().currentUser==null){
            noProductsText.visibility = View.VISIBLE
            noProductsTextView.text = getString(R.string.you_are_not_logged_i)
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }else{
            val todaysDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
            firebaseFirestore.collectionGroup("users_order_collection")
                    .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", startTimeMonthMillis)
                    .whereLessThanOrEqualTo("orderPlacingTimeStamp", endTimeMonthMillis)
                    .orderBy("orderPlacingTimeStamp")
                .addSnapshotListener { value, error ->
                    FirebaseDatabase.getInstance().reference.child("da_agents_realtime_details")
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                daStatusList.clear()
                                for(item in snapshot.children){
                                    if(todaysDate==item.child("orderDateCount").child("date").value.toString()){
                                        daStatusList.add(
                                            DaStatusItem(
                                                item.key.toString(),
                                                item.child("name").value.toString(),
                                                item.child("status").value.toString(),
                                                item.child("orderDateCount").child("date").value.toString(),
                                                (item.child("orderDateCount").child("orders").value as Long).toInt(),
                                            )
                                        )
                                    }else{
                                        daStatusList.add(
                                            DaStatusItem(
                                                item.key.toString(),
                                                item.child("name").value.toString(),
                                                item.child("status").value.toString(),
                                                item.child("orderDateCount").child("date").value.toString(),
                                                0,
                                            )
                                        )
                                    }
                                }
                                homeViewModel.setDaStatusList(daStatusList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                error.toException().printStackTrace()
                            }

                        })
                    if(value!=null){
                        ordersMainArrayList.clear()
                        for(document in value.documents){
                            val o = document.toObject(OrderItemMain::class.java)!!
                            o.docID = document.id
                            ordersMainArrayList.add(o)
                        }
                        if(ordersMainArrayList.isNotEmpty()){
                            placeOrderMainData()
                        }else{
                            noProductsText.visibility = View.VISIBLE
                            noProductsTextView.text = getString(R.string.you_have_no_orders)
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                    }else{
                        noProductsText.visibility = View.VISIBLE
                        noProductsTextView.text = getString(R.string.you_have_no_orders)
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                    }
                }
        }
    }

    private fun placeOrderMainData() {
//        val ordersCount = ordersMainArrayList.size
        val calculationResult = CalculationLogics().calculateArpansStatsForArpan(ordersMainArrayList)
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
//        ordersTotalTextView.text = ordersCount.toString()
//        totalIncomeTextView.text = income.toString()
//        completedOrdersTextView.text = completed.toString()
//        cancelledOrdersTextView.text = cancelled.toString()

        ordersTotalTextView.text = calculationResult.totalOrders.toString()
        totalIncomeTextView.text = calculationResult.arpansIncome.toString()
        completedOrdersTextView.text = calculationResult.completed.toString()
        cancelledOrdersTextView.text = calculationResult.cancelled.toString()
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = orderAdapterMain

        noProductsText.visibility = View.GONE
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

//    private fun loadSecondData(s: String) {
//        noProductsText.visibility = View.GONE
//        progressBar.visibility = View.VISIBLE
//        recyclerView.visibility = View.GONE
//        firebaseFirestore
//                .collectionGroup("users_order_collection")
//                .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", startTimeMonthMillis)
//                .whereLessThanOrEqualTo("orderPlacingTimeStamp", endTimeMonthMillis)
//                .orderBy("orderPlacingTimeStamp")
//                .get()
//                .addOnCompleteListener {
//                    if(it.isSuccessful){
//                        ordersMainArrayList.clear()
//                        for(document in it.result!!.documents){
//                            val o = document.toObject(OrderItemMain::class.java)!!
//                            o.docID = document.id
//                            ordersMainArrayList.add(o)
//                        }
//                        if(ordersMainArrayList.isNotEmpty()){
//                            placeOrderMainData()
//                        }else{
//                            noProductsText.visibility = View.VISIBLE
//                            noProductsTextView.text = getString(R.string.you_have_no_orders)
//                            progressBar.visibility = View.GONE
//                            recyclerView.visibility = View.GONE
//                        }
//                    }else{
//                        noProductsText.visibility = View.VISIBLE
//                        noProductsTextView.text = getString(R.string.you_have_no_orders)
//                        progressBar.visibility = View.GONE
//                        recyclerView.visibility = View.GONE
//                        it.exception!!.printStackTrace()
//                    }
//                }
//    }

    fun addCustomOrder(view: View) {
    }

    override fun openSelectedOrderItemAsDialog(
        position: Int,
        mainItemPositions: Int,
        docId: String,
        userId: String,
        orderItemMain : OrderItemMain
    ) {
        selectedRecyclerAdapterItem = position
        mainItemPositionsRecyclerAdapter = mainItemPositions
        val bundle = Bundle()
        bundle.putString("orderID",docId)
        bundle.putString("customerId",userId)
        homeViewModel.setCurrentSelectedOrderItem(orderItemMain)
        val fg = OrderHistoryFragmentNew3()
        fg.arguments = bundle
        fg.show(supportFragmentManager, "")
    }
}