package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldMainItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.adapter.TopMenuRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.auth.MainActivity
import admin.arpan.delivery.ui.order.OrderHistoryFragment
import admin.arpan.delivery.ui.order.OrderHistoryFragmentNew
import admin.arpan.delivery.ui.order.OrderHistoryFragmentNew2
import admin.arpan.delivery.ui.order.OrdresActivity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.noProductsText
import kotlinx.android.synthetic.main.activity_home.noProductsTextView
import kotlinx.android.synthetic.main.activity_home.progressBar
import kotlinx.android.synthetic.main.activity_home.recyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity(), OrderOldSubItemRecyclerAdapterInterface {

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
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        initLoadPrefsData()

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

        val titlesTop = arrayListOf("Shop Management", "Offer Management", "Order Management", "DA Management",
            "Settings", "Add Custom Order", "Feedbacks")
        val imagesTop = arrayListOf(
            R.drawable.ic_arpan_shop_icon,
            R.drawable.ic_offer_icon,
            R.drawable.ic_order_manage,
            R.drawable.ic_asset_11,
            R.drawable.ic_round_settings_24,
            R.drawable.ic_round_add_24,
            R.drawable.ic_baseline_feedback_24
        )

        val lm = GridLayoutManager(this, 3)
        lm.orientation = GridLayoutManager.VERTICAL
        mainRecyclerView.layoutManager = lm
        mainRecyclerView.isNestedScrollingEnabled = false
        mainRecyclerView.adapter =
            TopMenuRecyclerAdapter(
                this,
                imagesTop,
                titlesTop
            )
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        orderAdapterMain = OrderOldMainItemRecyclerAdapter(this, ordersMainOldItemsArrayList, this, false, false, "")

        val c = Calendar.getInstance() // this takes current date
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0

        val d = Calendar.getInstance() // this takes current date
        d[Calendar.HOUR_OF_DAY] = 24
        d[Calendar.MINUTE] = 60
        d[Calendar.SECOND] = 60

        startTimeMonthMillis = c.timeInMillis
        endTimeMonthMillis = d.timeInMillis

        title_text_view.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sure to logout ?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
                .create().show()
        }
        initFirebaseMessaging()
        loadOrdersDataMainOneDay()
    }

    private fun initLoadPrefsData() {
        FirebaseDatabase.getInstance().reference
            .child("SavedPrefClientTf")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val array = ArrayList<SavedPrefClientTf>()
                    for(snap in snapshot.children){
                        array.add(snap.getValue(SavedPrefClientTf::class.java)!!)
                    }
                    homeViewModel.setUserSavedPrefClientTfArrayList(array)
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }

            })
    }

    private fun loadOrdersDataMainOneDay() {
        val todaysDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
        FirebaseDatabase.getInstance().reference.child("da_agents_realtime_details")
            .addValueEventListener(object : ValueEventListener {
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
        FirebaseFirestore.getInstance()
            .collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", startTimeMonthMillis)
            .whereLessThanOrEqualTo("orderPlacingTimeStamp", endTimeMonthMillis)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener { value, error ->
                error?.printStackTrace()
                if(value!=null){
                    ordersMainArrayList.clear()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        ordersMainArrayList.add(o)
                    }
                    homeViewModel.setOrdersMainArrayList(ordersMainArrayList)
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
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }

    private fun initFirebaseMessaging() {
        if(FirebaseAuth.getInstance().currentUser!=null){
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
            val token = getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
                .getString("TOKEN", "")
            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val t = task.result!!
                    Log.e("TOKEN", t)
                    if (token != t) {
                        val tokenArray: MutableMap<String, Any> = HashMap()
                        tokenArray["registrationTokens"] = FieldValue.arrayUnion(t)
                        val map = HashMap<String, String>()
                        map["registration_token"] = t
                        getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
                            .edit().putString("TOKEN", t).apply()
                        FirebaseFirestore.getInstance()
                            .collection("admin_app_notification_data_tokens")
                            .document("admin_app_notification_data_tokens")
                            .update(tokenArray)
                    }
                })
        }
    }

    override fun openSelectedOrderItemAsDialog(position : Int, mainItemPositions : Int, docId : String, userId : String, orderItemMain: OrderItemMain) {
        selectedRecyclerAdapterItem = position
        mainItemPositionsRecyclerAdapter = mainItemPositions
        val bundle = Bundle()
        bundle.putString("orderID", docId)
        bundle.putString("customerId", userId)
        homeViewModel.setCurrentSelectedOrderItem(orderItemMain)
        val fg = OrderHistoryFragmentNew2()
        fg.arguments = bundle
    }

}