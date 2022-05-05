package admin.arpan.delivery.ui.fragments

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ShopItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.ShopStatCardAdapter
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.SavedPrefClientTf
import admin.arpan.delivery.db.model.ShopStatItem
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.parseDate
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_orders_filter_by_day.view.*
import kotlinx.android.synthetic.main.fragment_shop_statistics.*
import kotlinx.android.synthetic.main.fragment_shops.view.*
import kotlinx.android.synthetic.main.location_item_recycler_view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList

class ShopStatistics : Fragment() {

    private lateinit var contextMain : Context

    private lateinit var shopNameTextView : TextView
    private lateinit var startDateButton : Button
    private lateinit var endDateButton : Button
    private lateinit var recyclerViewMain : RecyclerView
    private lateinit var shopsSpinner : Spinner
    private lateinit var clientsSpinner : Spinner
    private lateinit var daSpinner : Spinner

    private var mainItemsArray = ArrayList<ShopStatItem>()
    private lateinit var mainItemsAdapter : ShopStatCardAdapter
    private var shopNamesArrayList = ArrayList<String>()
    private var shopKeysArrayList = ArrayList<String>()
    private val tempOrdersThisMonthArrayList = ArrayList<OrderItemMain>()
    private var startTime = 0L
    private var endTime = 0L
    private var shopName = ""
    private var shopId = ""
    private var clientId = ""
    private var daId = ""
    private val TAG = "ShopStatisticsActivity"

    private lateinit var progressDialog : Dialog
    private lateinit var homeMainNewInterface : HomeMainNewInterface
    private lateinit var homeViewModelMainData: HomeViewModelMainData

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
        shopNameTextView = view.findViewById(R.id.title_text_view)
        startDateButton = view.findViewById(R.id.startDateButton)
        endDateButton = view.findViewById(R.id.endDateButton)
        recyclerViewMain = view.findViewById(R.id.recyclerView)
        shopsSpinner = view.findViewById(R.id.shopsSpinner)
        clientsSpinner = view.findViewById(R.id.clientSpinner)
        daSpinner = view.findViewById(R.id.daSpinner)
        progressDialog = contextMain.createProgressDialog()
        startDateButton.setOnClickListener { openStartDatePopUp() }
        endDateButton.setOnClickListener { openEndDatePopUp() }
        mainItemsAdapter = ShopStatCardAdapter(contextMain, mainItemsArray)
        recyclerViewMain.layoutManager = GridLayoutManager(contextMain, 3)
        recyclerViewMain.adapter = mainItemsAdapter
        loadInitialData()
    }

    private fun loadInitialData() {
        if(homeViewModelMainData.getShopsMainArrayListData().value.isNullOrEmpty()){
            homeMainNewInterface.loadShopsDataMain()
        }
        homeViewModelMainData.getShopsMainArrayListData().observe(requireActivity()) {
            shopKeysArrayList.clear()
            shopNamesArrayList.clear()
            shopNamesArrayList.add("All Shops")
            shopKeysArrayList.add("All Shops")
            it?.forEach { shopItem ->
                shopKeysArrayList.add(shopItem.key)
                shopNamesArrayList.add(shopItem.name)
            }
            shopsSpinner.adapter = ArrayAdapter(contextMain,
                R.layout.support_simple_spinner_dropdown_item,
                shopNamesArrayList
            )
            shopsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                    shopId = if(i == 0){
                        ""
                    }else{
                        shopKeysArrayList[i]
                    }
                    placeOrderMainData()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
        val arrayListPrefs = ArrayList<String>()
        val arrayListPrefsMain= ArrayList<SavedPrefClientTf>()
        arrayListPrefs.add("All Clients")
        arrayListPrefsMain.add(SavedPrefClientTf())
        for(item in homeViewModelMainData.getUserSavedPrefClientTfArrayList().value!!){
            arrayListPrefs.add(item.user_name)
            arrayListPrefsMain.add(item)
        }
        clientSpinner.adapter = ArrayAdapter(contextMain,
            R.layout.support_simple_spinner_dropdown_item,
            arrayListPrefs)

        clientsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                clientId = if(i == 0){
                    ""
                }else{
                    arrayListPrefsMain[i].user_name
                }
                placeOrderMainData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        val arrayListDaList = homeViewModelMainData.getDaMainListData().value!!
        val arrayListDaListString = ArrayList<String>()
        arrayListDaListString.add("All Da")
        for(item in arrayListDaList){
            arrayListDaListString.add(item.da_name)
        }

        daSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                daId = if(i == 0){
                    ""
                }else{
                    arrayListDaList[i-1].key
                }
                placeOrderMainData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        daSpinner.adapter = ArrayAdapter(contextMain,
            R.layout.support_simple_spinner_dropdown_item,
            arrayListDaListString)
    }

    private fun openStartDatePopUp(){
        val calendar = Calendar.getInstance()
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)
        val dd = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(contextMain,
            { _, year, month, dayOfMonth ->
                setStartTime(year, month, dayOfMonth)
            }, yy, mm, dd)
        datePicker.show()
    }
    private fun openEndDatePopUp(){
        val calendar = Calendar.getInstance()
        val yy = calendar.get(Calendar.YEAR)
        val mm = calendar.get(Calendar.MONTH)
        val dd = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(contextMain,
            { _, year, month, dayOfMonth ->
                setEndTime(year, month, dayOfMonth)
            }, yy, mm, dd)
        datePicker.show()
    }
    private fun setStartTime(yyyy:Int, mm:Int, dd:Int){
        startTime = parseDate("$dd/$mm/$yyyy 00:00", "dd/MM/yyyy hh:mm")
        startDateButton.text = "$dd/$mm/$yyyy"
//        startDateButton.text = startTime.toString()+"."+endTime.toString()
        triggerDataFetch()
    }

    private fun setEndTime(yyyy:Int, mm:Int, dd:Int){
        endTime = parseDate("$dd/$mm/$yyyy 24:00", "dd/MM/yyyy hh:mm")
        endDateButton.text = "$dd/$mm/$yyyy"
        triggerDataFetch()
    }

    private fun triggerDataFetch() {
        if(startTime == 0L || endTime == 0L){
            return
        }
        if(startTime > endTime){
            return
        }
        progressDialog.show()
        FirebaseFirestore.getInstance().collectionGroup("users_order_collection")
            .whereGreaterThanOrEqualTo("orderPlacingTimeStamp", startTime)
            .whereLessThanOrEqualTo("orderPlacingTimeStamp", endTime)
            .orderBy("orderPlacingTimeStamp")
            .addSnapshotListener(contextMain as Activity) { value, error ->
                if(value!=null){
                    tempOrdersThisMonthArrayList.clear()
                    for(document in value.documents){
                        val o = document.toObject(OrderItemMain::class.java)!!
                        o.docID = document.id
                        tempOrdersThisMonthArrayList.add(o)
                    }
                    if(tempOrdersThisMonthArrayList.isNotEmpty()){
                        placeOrderMainData()
                        progressDialog.dismiss()
                    }else{
                        Log.e(TAG, "OrderThisMonthArraySnapShotList size is 0")
                    }
                }else{
                    Log.e(TAG, "OrderThisMonthArraySnapShotList is NULL")
                }
            }
    }

    private fun placeOrderMainData() {
        var shopIncome = 0
        var shopSales = 0
        var arpanProfit = 0
        var arpanProfitShop = 0
        var arpanProfitDa = 0
        var totalOrders = 0
        var totalIncome = 0
        var daIncome = 0
        var daSales = 0
        var totalProducts = 0
        var clientSales = 0
        mainItemsArray.clear()
        for(item in tempOrdersThisMonthArrayList){
            totalOrders += 1
            totalProducts += item.products.size
            totalIncome += item.totalPrice
            for(pItem in item.products){
                arpanProfit += pItem.product_arpan_profit
            }
            if(shopId != ""){
                if(item.products.any {
                    it.product_item_shop_key == shopId
                    }){
                    shopSales += 1
                    for(pItem in item.products){
                        if(pItem.product_item_shop_key == shopId){
                            shopIncome += (pItem.product_item_price - pItem.product_arpan_profit)
                            arpanProfitShop += pItem.product_arpan_profit
                        }
                    }
                }
            }
            if(daId != ""){
                if(item.daID == daId){
                    daSales += 1
                    daIncome += item.daCharge
                    for(pItem in item.products){
                        arpanProfitDa += pItem.product_arpan_profit
                    }
                }
            }
            if(clientId != ""){
                if(item.userName.trim().contains(clientId.trim())){
                    clientSales += 1
                }
            }
        }
        mainItemsArray.add(ShopStatItem(0, shopIncome.toString(), "Shop Income"))
        mainItemsArray.add(ShopStatItem(0, shopSales.toString(), "Shop Sales"))
        mainItemsArray.add(ShopStatItem(0, totalProducts.toString(), "Total Products"))
        mainItemsArray.add(ShopStatItem(0, totalOrders.toString(), "Total Orders"))
        mainItemsArray.add(ShopStatItem(0, totalIncome.toString(), "Total Income"))
        mainItemsArray.add(ShopStatItem(0, daIncome.toString(), "DA Income"))
        mainItemsArray.add(ShopStatItem(0, daSales.toString(), "DA Sales"))
        mainItemsArray.add(ShopStatItem(0, clientSales.toString(), "Client Sales"))
        mainItemsArray.add(ShopStatItem(0, arpanProfit.toString(), "Total Profit"))
        mainItemsArray.add(ShopStatItem(0, arpanProfitShop.toString(), "Shop Profit"))
        mainItemsArray.add(ShopStatItem(0, arpanProfitDa.toString(), "DA Profit"))
        mainItemsAdapter.notifyItemRangeChanged(0, mainItemsArray.size)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_statistics, container, false)
    }
}