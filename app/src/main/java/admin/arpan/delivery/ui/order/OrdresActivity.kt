package admin.arpan.delivery.ui.order

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.model.DaStatusItem
import admin.arpan.delivery.db.model.OrderItemMain
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import admin.arpan.delivery.db.model.OrderOldItems
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_ordres.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OrdresActivity : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var ordersMainArrayList =  ArrayList<OrderItemMain>()
    private var ordersMainHashMap = HashMap<String, ArrayList<OrderItemMain>>()
    var ordersMainOldItemsArrayList = ArrayList<OrderOldItems>()
    lateinit var orderAdapterMain : OrderOldMainItemRecyclerAdapter
    var daStatusList = ArrayList<DaStatusItem>()
    var selectedRecyclerAdapterItem = 0
    var mainItemPositionsRecyclerAdapter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordres)
        initVars()
        initLogics()
    }

    private fun initLogics() {
        loadDataFirstTime()
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        orderAdapterMain = OrderOldMainItemRecyclerAdapter(this, ordersMainOldItemsArrayList)
    }

    private fun loadDataFirstTime() {
        if(FirebaseAuth.getInstance().currentUser==null){
            noProductsText.visibility = View.VISIBLE
            noProductsTextView.text = getString(R.string.you_are_not_logged_i)
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            radioGroup.visibility = View.GONE
        }else{
            firebaseFirestore.collectionGroup("users_order_collection")
                    .whereEqualTo("orderStatus","PENDING")
                    .orderBy("orderPlacingTimeStamp")
                    .get()
                    .addOnCompleteListener {
                        enableRadioGroupLogic()
                        FirebaseDatabase.getInstance().reference.child("da_agents_realtime_details")
                            .addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    daStatusList.clear()
                                    for(item in snapshot.children){
                                        daStatusList.add(
                                            DaStatusItem(
                                                item.key.toString(),
                                                item.child("name").value.toString(),
                                                item.child("status").value.toString(),
                                            )
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    error.toException().printStackTrace()
                                }

                            })
                        if(it.isSuccessful){
                            for(document in it.result!!.documents){
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
                                radioGroup.visibility = View.GONE
                            }
                        }else{
                            noProductsText.visibility = View.VISIBLE
                            noProductsTextView.text = getString(R.string.you_have_no_orders)
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            radioGroup.visibility = View.GONE
                            it.exception!!.printStackTrace()
                        }
                    }
        }
    }

    private fun placeOrderMainData() {
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
        radioGroup.visibility = View.VISIBLE
    }

    private fun enableRadioGroupLogic() {
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.pendingRadio -> {
                    loadSecondData("PENDING")
                }
                R.id.approvedRadio -> {
                    loadSecondData( "APPROVED")
                }
                R.id.onDeliveryRadio -> {
                    loadSecondData("DELIVERY")
                }
                R.id.completedRadio -> {
                    loadSecondData("COMPLETED")
                }
            }
        }
    }

    private fun loadSecondData(s: String) {
        noProductsText.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        radioGroup.visibility = View.VISIBLE
        firebaseFirestore
                .collectionGroup("users_order_collection")
                .whereEqualTo("orderStatus",s)
                .orderBy("orderPlacingTimeStamp")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        ordersMainArrayList.clear()
                        for(document in it.result!!.documents){
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
                            radioGroup.visibility = View.VISIBLE
                        }
                    }else{
                        noProductsText.visibility = View.VISIBLE
                        noProductsTextView.text = getString(R.string.you_have_no_orders)
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        radioGroup.visibility = View.VISIBLE
                        it.exception!!.printStackTrace()
                    }
                }
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }

    override fun onResume() {
        super.onResume()
        if(FirebaseAuth.getInstance().currentUser!=null){
            loadSecondData( "PENDING")
        }
    }
}