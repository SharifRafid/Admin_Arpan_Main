package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderOldSubItemRecyclerAdapterInterface
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.auth.MainActivity
import admin.arpan.delivery.ui.feedback.UserFeedBackFragment
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.order.OrderHistoryFragmentNew2
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.getDate
import admin.arpan.delivery.utils.showToast
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shashank.sony.fancytoastlib.FancyToast
import java.lang.Exception
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class HomeActivityMain : AppCompatActivity(), HomeMainNewInterface{

    private final val TAG = "HomeActivityMain"

    private lateinit var navController: NavController

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var selectedRecyclerAdapterItem = 0
    private var mainItemPositionsRecyclerAdapter = 0

    private lateinit var homeViewModelMainData: HomeViewModelMainData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_main)
        initVars()
        initLogics()
    }

    private fun initLogics() {
        initFirebaseMessaging()
        loadDaRealtimeDataStatusList()
        loadSavedClientsPrefData()
        loadRealtimeDeliveryChargesList()
        loadShopsDataList()
        loadDaDataList()
    }

    private fun loadDaDataList() {
        firebaseFirestore.collection("da_agents_main_list_collection")
            .addSnapshotListener(this) { value, error ->
                error?.printStackTrace()
                if(value!=null){
                    if(value.documents.size==0){
                        showToast("No Da Found", FancyToast.ERROR)
                    }else{
                        val tempDaList = ArrayList<DaAgent>()
                        for(document in value.documents){
                            val da = document.toObject(DaAgent::class.java)
                            da!!.key = document.id
                            tempDaList.add(da)
                        }
                        Collections.sort(tempDaList,
                            Comparator<DaAgent?> { m1, m2 ->
                                try{
                                    m1!!.da_uid.toInt().compareTo(m2!!.da_uid.toInt())
                                }catch (e : Exception){
                                    m1!!.da_uid.compareTo(m2!!.da_uid)
                                }
                            })
                        Collections.sort(tempDaList,
                            Comparator<DaAgent?> { m1, m2 ->
                                m2!!.da_status_active.compareTo(m1!!.da_status_active)
                            })
                        homeViewModelMainData.setDaMainListData(tempDaList)
                    }
                }
            }
    }

    private fun initVars() {
        homeViewModelMainData = ViewModelProvider(this).get(HomeViewModelMainData::class.java)
        navController = Navigation.findNavController(this, R.id.main_home_fragment_container)
    }

    private fun loadShopsDataList() {
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
            .orderBy(Constants.FIELD_FD_SM_CATEGORY)
            .orderBy(Constants.FIELD_FD_SM_ORDER)
            .addSnapshotListener(this) { value, error ->
                error?.printStackTrace()
                if(value!=null){
                    if(value.documents.isNotEmpty()){
                        val tempShopArrayList = ArrayList<ShopItem>()
                        for(document in value.documents){
                            val shopItem = ShopItem(
                                key = document.id,
                                name = document.getString(Constants.FIELD_FD_SM_NAME).toString(),
                                categories = document.getString(Constants.FIELD_FD_SM_CATEGORY).toString(),
                                image = document.getString(Constants.FIELD_FD_SM_ICON).toString(),
                                cover_image = document.getString(Constants.FIELD_FD_SM_COVER).toString(),
                                da_charge = document.getString(Constants.FIELD_FD_SM_DA_CHARGE).toString(),
                                deliver_charge = document.getString(Constants.FIELD_FD_SM_DELIVERY).toString(),
                                location = document.getString(Constants.FIELD_FD_SM_LOCATION).toString(),
                                username = document.getString(Constants.FIELD_FD_SM_USERNAME).toString(),
                                password = document.getString(Constants.FIELD_FD_SM_PASSWORD).toString(),
                                order = document.getString(Constants.FIELD_FD_SM_ORDER).toString().toInt(),
                                status = document.getString(Constants.FIELD_FD_SM_STATUS).toString(),
                                isClient = document.getString(Constants.FIELD_FD_SM_IS_CLIENT).toString(),
                                dynamicLink = document.getString(Constants.FIELD_FD_SM_DYNAMIC_LINK).toString()
                            )
                            if(document.contains("shopNotice")){
                                shopItem.shopNotice = document.getString("shopNotice").toString()
                            }
                            if(document.contains("shopNoticeColor")){
                                shopItem.shopNoticeColor = document.getString("shopNoticeColor").toString()
                            }
                            if(document.contains("shopNoticeColorBg")){
                                shopItem.shopNoticeColorBg = document.getString("shopNoticeColorBg").toString()
                            }
                            if(document.contains("shopDiscount")){
                                shopItem.shopDiscount = document.getBoolean("shopDiscount")!!
                            }
                            if(document.contains("shopCategoryDiscount")){
                                shopItem.shopCategoryDiscount = document.getBoolean("shopCategoryDiscount")!!
                            }
                            if(document.contains("shopCategoryDiscountName")){
                                shopItem.shopCategoryDiscountName = document.getString("shopCategoryDiscountName")!!
                            }
                            if(document.contains("shopDiscountPercentage")){
                                shopItem.shopDiscountPercentage = document.getString("shopDiscountPercentage").toString().toFloat()
                            }
                            if(document.contains("shopDiscountMinimumPrice")){
                                shopItem.shopDiscountMinimumPrice = document.getString("shopDiscountMinimumPrice").toString().toFloat()
                            }
                            tempShopArrayList.add(shopItem)
                        }
                        homeViewModelMainData.setShopsMainArrayListData(tempShopArrayList)
                        Log.e(TAG, "ShopsArraySnapShotList Size = 0" + tempShopArrayList.size)
                    }else{
                        Log.e(TAG, "ShopsArraySnapShotList Size = 0")
                    }
                }else{
                    Log.e(TAG, "ShopsArraySnapShotList Snapshot Value Null")
                }
            }
    }

    private fun loadOrdersDataList(){
        val c = Calendar.getInstance() // this takes current date
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0

        val d = Calendar.getInstance() // this takes current date
        d[Calendar.HOUR_OF_DAY] = 24
        d[Calendar.MINUTE] = 60
        d[Calendar.SECOND] = 60

        val startTimeMonthMillis = c.timeInMillis
        val endTimeMonthMillis = d.timeInMillis
        firebaseFirestore.collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", startTimeMonthMillis)
            .whereLessThanOrEqualTo("orderPlacingTimeStamp", endTimeMonthMillis)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener { value, error ->
                error?.printStackTrace()
                if(value!=null){
                    val tempOrdersMainArrayList = ArrayList<OrderItemMain>()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        tempOrdersMainArrayList.add(o)
                    }
                    if(tempOrdersMainArrayList.isNotEmpty()){
                        homeViewModelMainData.setOrdersOneDayDataMainList(tempOrdersMainArrayList)
                        Log.e(TAG, "OrderTodayArraySnapShotList Size = "+tempOrdersMainArrayList.size)
                    }else{
                        Log.e(TAG, "OrderTodayArraySnapShotList Size = 0")
                    }
                }else{
                    Log.e(TAG, "OrderTodayArraySnapShotList is NULL")
                }
            }
    }

    private fun loadOrdersDataThisMonth(){
        val c = Calendar.getInstance() // this takes current date
        c[Calendar.DAY_OF_MONTH] = 1
        c[Calendar.HOUR_OF_DAY] = 0
        val d = Calendar.getInstance() // this takes current date
        d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        d[Calendar.HOUR_OF_DAY] = 24
        firebaseFirestore.collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", c.timeInMillis)
            .whereLessThanOrEqualTo("orderPlacingTimeStamp", d.timeInMillis)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener(this) { value, error ->
                if(value!=null){
                    val tempOrdersThisMonthArrayList = ArrayList<OrderItemMain>()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        tempOrdersThisMonthArrayList.add(o)
                    }
                    if(tempOrdersThisMonthArrayList.isNotEmpty()){
                        homeViewModelMainData.setThisMonthOrdersMainArrayList(tempOrdersThisMonthArrayList)
                    }else{
                        Log.e(TAG, "OrderThisMonthArraySnapShotList size is 0")
                    }
                }else{
                    Log.e(TAG, "OrderThisMonthArraySnapShotList is NULL")
                }
            }
    }

    private fun loadOrdersDataLastMonth(){
        val c = Calendar.getInstance() // this takes current date
        c.add(Calendar.MONTH, -1)
        c[Calendar.DAY_OF_MONTH] = 1
        c[Calendar.HOUR_OF_DAY] = 0
        val d = Calendar.getInstance() // this takes current date
        d.add(Calendar.MONTH, -1)
        d[Calendar.DAY_OF_MONTH] = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        d[Calendar.HOUR_OF_DAY] = 24
        firebaseFirestore.collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", c.timeInMillis)
            .whereLessThanOrEqualTo("orderPlacingTimeStamp", d.timeInMillis)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener(this) { value, error ->
                if(value!=null){
                    val tempOrdersThisMonthArrayList = ArrayList<OrderItemMain>()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        tempOrdersThisMonthArrayList.add(o)
                    }
                    if(tempOrdersThisMonthArrayList.isNotEmpty()){
                        homeViewModelMainData.setLastMonthOrdersMainData(tempOrdersThisMonthArrayList)
                    }else{
                        Log.e(TAG, "OrderThisMonthArraySnapShotList size is 0")
                    }
                }else{
                    Log.e(TAG, "OrderThisMonthArraySnapShotList is NULL")
                }
            }
    }

    private fun loadDaRealtimeDataStatusList(){
        val todaysDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
        firebaseDatabase.child("da_agents_realtime_details").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val daStatusList = ArrayList<DaStatusItem>()
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
                                    item.child("statusTextTitle").value.toString(),
                                    )
                            )
                        }
                    }
                    homeViewModelMainData.setDaRealtimeStatusList(daStatusList)
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }

            })
    }

    private fun loadUsersListData(){
        firebaseFirestore.collection("users")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    val usersArrayList = ArrayList<UserItem>()
                    for(document in it.result!!.documents){
                        val doc = document.toObject(UserItem::class.java)!!
                        doc.key = document.id
                        usersArrayList.add(doc)
                    }
                    homeViewModelMainData.setUsersMainArrayListData(usersArrayList)
                }else{
                    it.exception?.printStackTrace()
                }
            }
    }

    private fun loadRealtimeDeliveryChargesList(){
        firebaseDatabase
            .child("data")
            .child("delivery_charges")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempLocationsArrayList = ArrayList<LocationItem>()
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
                        tempLocationsArrayList.add(locationItem)
                    }
                    homeViewModelMainData.setLocationsArrayList(tempLocationsArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
    }

    private fun loadSavedClientsPrefData() {
        FirebaseDatabase.getInstance().reference
            .child("SavedPrefClientTf")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempUserSavedPrefClientTfArrayList = ArrayList<SavedPrefClientTf>()
                    for(snap in snapshot.children){
                        tempUserSavedPrefClientTfArrayList.add(snap.getValue(SavedPrefClientTf::class.java)!!)
                    }
                    homeViewModelMainData.setUserSavedPrefClientTfArrayList(tempUserSavedPrefClientTfArrayList)
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }

            })
    }

    override fun navigateToFragment(id: Int) {
        navController.navigate(id)
    }

    override fun navigateToFragment(index: Int, bundle: Bundle) {
        navController.navigate(index, bundle)
    }

    override fun loadOrdersOneDayList() {
        loadOrdersDataList()
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

    override fun openSelectedOrderItemAsDialog(position: Int, mainItemPositions: Int, docId: String, userId: String, orderItemMain: OrderItemMain){
        selectedRecyclerAdapterItem = position
        mainItemPositionsRecyclerAdapter = mainItemPositions
        val bundle = Bundle()
        bundle.putString("orderID", docId)
        bundle.putString("customerId", userId)
        homeViewModelMainData.setCurrentSelectedOrderItemMain(orderItemMain)
        navController.navigate(R.id.orderHistoryFragmentNew22, bundle)
    }

    override fun callOnBackPressed() {
        onBackPressed()
    }

    override fun loadShopsDataMain() {
        loadShopsDataList()
    }

    override fun openFeedBackDialog() {
        UserFeedBackFragment().show(supportFragmentManager, "")
    }

    override fun loadDaMainListData() {
        loadDaDataList()
    }

    override fun logOutUser() {
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

    override fun loadThisMonthOrdersMainData() {
        loadOrdersDataThisMonth()
    }

    override fun loadLastMonthOrdersMainData() {
        loadOrdersDataLastMonth()
    }

    override fun loadUsersMainArrayListData() {
        loadUsersListData()
    }

}