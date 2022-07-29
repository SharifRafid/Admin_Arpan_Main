package admin.arpan.delivery.ui.order

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ProductItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.ShopProductAddItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.ShopProductAddOrderInterface
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.*
import java.lang.ClassCastException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.dialog_list_common.view.*
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.title_text_view
import android.app.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.titleTextView
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.autofillAllTextBoxesId
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.pickUpDateTextView
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.pickUpTimeTextView
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.radioGroup
import kotlinx.android.synthetic.main.fragment_edit_order.view.*
import java.text.ParseException


class AddCustomOrder : Fragment(), ShopProductAddOrderInterface,
    TimePickerDialog.OnTimeSetListener,
    com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    private lateinit var contextMain: Context
    private lateinit var viewMain : View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dialog2 : Dialog

    private val TAG = "AddCustomOrder"

    private var deliveryChargeMain = 0
    private var daChargeMain = 0
    private var totalChargeMain = 0

    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private lateinit var homeViewModelMainData: HomeViewModelMainData

    private var imagePath : Uri = Uri.parse("")

    private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
    private lateinit var shopProductAddItemRecyclerAdapter: ShopProductAddItemRecyclerAdapter

    private var arrayListTimeTitles = ArrayList<String>()
    private var arrayListTimeValues = ArrayList<Long>()

    private var locationItems  = ArrayList<LocationItem>()

    private var dateLong = 0L;

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextMain = context
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_custom_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
        initVars(view)
        initLogics(view)
        initShopAddLogics(view)
        initSavedDataPlacement(view)
        initSavedDataListeners(view)
        initSpinnersLocations(view)
        initDataSavingOnFirebaseLogic(view)
        initRadioButtonForDateAndTimePickingStuff(view)
    }

    private fun initShopAddLogics(view: View) {
        view.imageButtonAddShop.setOnClickListener {
            val dialogShopSelectList = AlertDialog.Builder(contextMain).create()
            val dialogShopSelectListView = LayoutInflater.from(contextMain).inflate(R.layout.dialog_list_common, null)
            dialogShopSelectListView.dialogTitleCommon.text = "Choose Shop"
            val shopsList = ArrayList<String>()
            val shopsMainList = homeViewModelMainData.getShopsMainArrayListData().value!!
            for(shopItem in shopsMainList){
                shopsList.add(shopItem.name)
            }
            dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(contextMain,
                R.layout.support_simple_spinner_dropdown_item, shopsList)
            dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position, id ->
                mainShopItemHashMap.add(
                    MainShopCartItem(
                        "",
                        shopsMainList[position].key,
                        ArrayList(),
                        shopsMainList[position]
                    )
                )
                shopProductAddItemRecyclerAdapter.notifyItemInserted(mainShopItemHashMap.size-1)
                dialogShopSelectList.dismiss()
            }
            dialogShopSelectList.setView(dialogShopSelectListView)
            dialogShopSelectList.show()
        }
    }

    private fun initRadioButtonForDateAndTimePickingStuff(view: View) {
        view.pickUpDateTextView.setOnClickListener {
            val now = Calendar.getInstance()
            val dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH))
            dpd.show(childFragmentManager, "Datepickerdialog")
        }

        view.pickUpDateTextView.setOnLongClickListener {
            view.pickUpDateTextView.text = "Today"
            dateLong = 0
            true
        }

        view.pickUpTimeTextView.setOnClickListener {
            val now = Calendar.getInstance()
            val dpd: TimePickerDialog = TimePickerDialog.newInstance(this, false)
            dpd.show(childFragmentManager, "Datepickerdialog")
        }

        view.pickUpTimeTextView.setOnLongClickListener {
            view.pickUpTimeTextView.text = "Now"
            true
        }
        
    }

    private fun initDataSavingOnFirebaseLogic(view: View) {
        view.autofillAllTextBoxesId.setOnClickListener {
            val arrayListPrefs = ArrayList<String>()
            val arrayListPrefsMain= ArrayList<SavedPrefClientTf>()
            for(item in homeViewModelMainData.getUserSavedPrefClientTfArrayList().value!!){
                arrayListPrefs.add(item.user_name)
                arrayListPrefsMain.add(item)
            }
            val arrayAdapter = ArrayAdapter(contextMain, R.layout.custom_spinner_item_view, arrayListPrefs)
            val alertDialogPrefs = AlertDialog.Builder(contextMain).create()
            val alertDialogPrefsView = LayoutInflater.from(contextMain).inflate(R.layout.assign_da_list_view, null)
            alertDialogPrefsView.txtAllPrice.text = "Select User Profile"
            alertDialogPrefsView.listView.adapter = arrayAdapter
            alertDialogPrefsView.listView.setOnItemClickListener { parent, view2, position, id ->
                view.txt_name.setText(arrayListPrefsMain[position].user_name)
                view.txt_number.setText(arrayListPrefsMain[position].user_mobile)
                view.txt_address.setText(arrayListPrefsMain[position].user_address)
                view.txt_note.setText(arrayListPrefsMain[position].user_note)
                view.txt_details.setText(arrayListPrefsMain[position].user_order_details)
                view.deliveryChargeTotal.setText(arrayListPrefsMain[position].delivery_charge)
                view.daChargeTotal.setText(arrayListPrefsMain[position].da_charge)
                view.totalChargeEdittext.setText(arrayListPrefsMain[position].total_charge)
                alertDialogPrefs.dismiss()
            }
            alertDialogPrefsView.listView.setOnItemLongClickListener { parent, view, position, id ->
                AlertDialog.Builder(contextMain)
                    .setTitle("Delete ?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        FirebaseDatabase.getInstance().reference
                            .child("SavedPrefClientTf")
                            .child(arrayListPrefsMain[position].key)
                            .removeValue()
                        dialog.dismiss()
                    })
                    .create()
                    .show()
                alertDialogPrefs.dismiss()
                true
            }
            alertDialogPrefs.setView(alertDialogPrefsView)
            alertDialogPrefs.show()
        }
        view.autofillAllTextBoxesId.setOnLongClickListener{
            val savedPrefClientTf = SavedPrefClientTf()
            savedPrefClientTf.key = "SPC"+System.currentTimeMillis()
            savedPrefClientTf.user_name = view.txt_name.text.toString()
            savedPrefClientTf.user_mobile = view.txt_number.text.toString()
            savedPrefClientTf.user_address = view.txt_address.text.toString()
            savedPrefClientTf.user_note = view.txt_note.text.toString()
            savedPrefClientTf.user_order_details = view.txt_details.text.toString()
            savedPrefClientTf.delivery_charge = view.deliveryChargeTotal.text.toString()
            savedPrefClientTf.da_charge = view.daChargeTotal.text.toString()
            savedPrefClientTf.total_charge = view.totalChargeEdittext.text.toString()
            if(savedPrefClientTf.key.isNotEmpty()&&savedPrefClientTf.user_name.isNotEmpty()
                &&savedPrefClientTf.user_mobile.isNotEmpty()&&savedPrefClientTf.user_address.isNotEmpty()){
                if(homeViewModelMainData.getUserSavedPrefClientTfArrayList().value!!.any {
                        it.user_address == savedPrefClientTf.user_address &&
                                it.user_name == savedPrefClientTf.user_name &&
                                it.user_note == savedPrefClientTf.user_note &&
                                it.user_order_details == savedPrefClientTf.user_order_details &&
                                it.delivery_charge == savedPrefClientTf.delivery_charge &&
                                it.da_charge == savedPrefClientTf.da_charge &&
                                it.total_charge == savedPrefClientTf.total_charge &&
                                it.user_mobile == savedPrefClientTf.user_mobile}){
                    contextMain.showToast("Already Added", FancyToast.ERROR)
                }else{
                    FirebaseDatabase.getInstance().reference
                        .child("SavedPrefClientTf")
                        .child(savedPrefClientTf.key)
                        .setValue(savedPrefClientTf)
                        .addOnCompleteListener {
                            contextMain.showToast("Success", FancyToast.SUCCESS)
                        }
                }
            }else{
                contextMain.showToast("Is Empty", FancyToast.ERROR)
            }
            true
        }
    }

    private fun initSpinnersLocations(view: View) {
        locationItems.clear()
        val deliveryLocations = ArrayList<String>()
        for(location in homeViewModelMainData.getLocationsArrayList().value!!){
            deliveryLocations.add(location.locationName)
            locationItems.add(location.copy())
        }

        val adapter = ArrayAdapter(view.context, R.layout.custom_spinner_view, deliveryLocations)
        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
        view.spinner_2.adapter = adapter
        view.spinner_2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view2: View?,
                position: Int,
                id: Long
            ) {
                deliveryChargeMain =locationItems[position].deliveryCharge
                daChargeMain = locationItems[position].daCharge

                view.deliveryChargeTotal.setText(deliveryChargeMain.toString())
                view.daChargeTotal.setText(daChargeMain.toString())
                setPriceTotalOnView(view)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                view.spinner_2.setSelection(0)
            }
        }
    }

    private fun setPriceTotalOnView(view: View) {
        view.txt_price.text = "Total: ${totalChargeMain} + ${deliveryChargeMain} = ${totalChargeMain+deliveryChargeMain}"
    }

    private fun initSavedDataListeners(view: View) {
        view.txt_details.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                sharedPreferences.edit().putString("custom_data",s.toString())
                    .apply()
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        view.txt_name.doOnTextChanged { text, start, before, count ->
            sharedPreferences.edit().putString("custom_data_name",text.toString())
                .apply()
        }
        view.txt_number.doOnTextChanged { text, start, before, count ->
            sharedPreferences.edit().putString("custom_data_number",text.toString())
                .apply()
        }
        view.txt_note.doOnTextChanged { text, start, before, count ->
            sharedPreferences.edit().putString("custom_data_note",text.toString())
                .apply()
        }
        view.txt_address.doOnTextChanged { text, start, before, count ->
            sharedPreferences.edit().putString("custom_data_address",text.toString())
                .apply()
        }
        view.totalChargeEdittext.doOnTextChanged { text, start, before, count ->
            if(text!!.isNotEmpty()){
                totalChargeMain = text.toString().toInt()
                setPriceTotalOnView(view)
            }
        }
        view.daChargeTotal.doOnTextChanged { text, start, before, count ->
            if(text!!.isNotEmpty()){
                daChargeMain = text.toString().toInt()
            }
        }
        view.deliveryChargeTotal.doOnTextChanged { text, start, before, count ->
            if(text!!.isNotEmpty()){
                deliveryChargeMain = text.toString().toInt()
                setPriceTotalOnView(view)
            }
        }
        view.imageView.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
    }

    private fun initSavedDataPlacement(view: View) {
        view.txt_details.setText(sharedPreferences.getString("custom_data",""))
        view.txt_number.setText(sharedPreferences.getString("custom_data_number",""))
        view.txt_name.setText(sharedPreferences.getString("custom_data_name",""))
        view.txt_note.setText(sharedPreferences.getString("custom_data_note",""))
        view.txt_address.setText(sharedPreferences.getString("custom_data_address",""))
        imagePath = Uri.parse(sharedPreferences.getString("photo",""))
        if(imagePath.toString().isNotEmpty()){
            view.imageView.setImageURI(imagePath)
        }
    }

    private fun initLogics(view: View) {
        view.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rb1){
                view.bkash_charge_note.visibility = View.VISIBLE
            }else{
                view.bkash_charge_note.visibility = View.GONE
            }
        }
        view.txt_place_order.setOnClickListener {
            val userName = view.txt_name.text.toString()
            val userNumber = view.txt_number.text.toString()
            val userAddress = view.txt_address.text.toString()
            val userNote = view.txt_note.text.toString()
            val totalPriceText = view.totalChargeEdittext.text.toString()
            val deliveryPriceText = view.deliveryChargeTotal.text.toString()
            val daChargeText = view.daChargeTotal.text.toString()
            if(userNumber.isNotEmpty()&&userAddress.isNotEmpty()&&
                totalPriceText.isNotEmpty()&&deliveryPriceText.isNotEmpty()&&daChargeText.isNotEmpty()){
                    if(view.orderTypeRadioGroup.checkedRadioButtonId == R.id.orderTypeCustomOrder){
                        if(view.txt_details.text.isNotEmpty() || imagePath.toString().isNotEmpty()) {
                            val dialogAskingView = LayoutInflater.from(view.context)
                                .inflate(R.layout.dialog_alert_layout_main, null)
                            val dialog = AlertDialog.Builder(view.context)
                                .setView(dialogAskingView).create()
                            dialogAskingView.btnNoDialogAlertMain.text = getString(R.string.no)
                            dialogAskingView.btnYesDialogAlertMain.text = getString(R.string.ok_text)
                            dialogAskingView.titleTextView.text = "অর্ডার প্লেস করবেন ?"
                            dialogAskingView.messageTextView.text = "আপনি অর্ডার টি কনফার্ম করতে চলেছেন, আপনি কি নিশ্চিত ?"
                            dialogAskingView.btnNoDialogAlertMain.setOnClickListener {
                                dialog.dismiss()
                            }
                            dialogAskingView.btnYesDialogAlertMain.setOnClickListener {
                                placeOrder(view, view.txt_details.text.toString(), imagePath, userName, userNumber, userAddress, userNote)
                                dialog.dismiss()
                            }
                            dialog.show()
                        } else {
                            FancyToast.makeText(
                                context, getString(R.string.fill_all_the_fields),
                                FancyToast.LENGTH_SHORT, FancyToast.ERROR, false
                            ).show()
                        }
                    }else{
                        if(mainShopItemHashMap.any{it2-> it2.cart_products.isNotEmpty()}){
                            val dialogAskingView = LayoutInflater.from(view.context)
                                .inflate(R.layout.dialog_alert_layout_main, null)
                            val dialog = AlertDialog.Builder(view.context)
                                .setView(dialogAskingView).create()
                            dialogAskingView.btnNoDialogAlertMain.text = getString(R.string.no)
                            dialogAskingView.btnYesDialogAlertMain.text = getString(R.string.ok_text)
                            dialogAskingView.titleTextView.text = "অর্ডার প্লেস করবেন ?"
                            dialogAskingView.messageTextView.text = "আপনি অর্ডার টি কনফার্ম করতে চলেছেন, আপনি কি নিশ্চিত ?"
                            dialogAskingView.btnNoDialogAlertMain.setOnClickListener {
                                dialog.dismiss()
                            }
                            dialogAskingView.btnYesDialogAlertMain.setOnClickListener {
                                placeOrder(view, view.txt_details.text.toString(), imagePath, userName, userNumber, userAddress, userNote)
                                dialog.dismiss()
                            }
                            dialog.show()
                        }else{
                            FancyToast.makeText(
                                context, "Add Products To The List",
                                FancyToast.LENGTH_SHORT, FancyToast.ERROR, false
                            ).show()
                        }
                    }
            }else{
                FancyToast.makeText(
                    context, getString(R.string.fill_all_the_fields),
                    FancyToast.LENGTH_SHORT, FancyToast.ERROR, false
                ).show()
            }
        }
        view.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.orderTypeCustomOrder){
                view.customOrderRelativeLayout.visibility = View.VISIBLE
                view.shopsMainLinearLayout.visibility = View.GONE
            }else{
                view.customOrderRelativeLayout.visibility = View.GONE
                view.shopsMainLinearLayout.visibility = View.VISIBLE
            }
        }
        viewMain.shopsRecyclerView.layoutManager = LinearLayoutManager(contextMain)
        shopProductAddItemRecyclerAdapter = ShopProductAddItemRecyclerAdapter(contextMain, mainShopItemHashMap, this)
        viewMain.shopsRecyclerView.adapter = shopProductAddItemRecyclerAdapter
    }

    private fun initVars(view: View) {
        contextMain = view.context
        viewMain = view
        sharedPreferences = view.context.getSharedPreferences("custom_orders_data", Context.MODE_PRIVATE)
        dialog2 = view.context.createProgressDialog()
        view.title_text_view.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }
    }

    private fun placeOrderFinalUpload(view: View, orderItemMain: OrderItemMain) {
        FirebaseDatabase.getInstance().reference.child("orderNumberNew")
            .child("ON")
            .get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    if(task.result!!.value==null){
                        orderItemMain.orderId = "ARP1001"
                        FirebaseDatabase.getInstance().reference.child("orderNumberNew")
                            .child("ON").setValue("1001")
                    }else{
                        orderItemMain.orderId = "ARP"+(task.result!!.value.toString().toInt()+1)
                        FirebaseDatabase.getInstance().reference.child("orderNumberNew")
                            .child("ON").setValue((task.result!!.value.toString().toInt()+1).toString())
                    }
                    FirebaseFirestore.getInstance().collection("users")
                        .document("ADMINUSERID123123123")
                        .collection("users_order_collection")
                        .add(orderItemMain)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                view.context.showToast(getString(R.string.order_placed_successfully), FancyToast.SUCCESS)
                                dialog2.dismiss()
                                sharedPreferences.edit().clear().apply()
                                homeMainNewInterface.callOnBackPressed()

                                val bundle = Bundle()
                                bundle.putString("orderID", it.result!!.id)
                                bundle.putString("customerId", "ADMINUSERID123123123")
                                homeMainNewInterface.navigateToFragment(R.id.orderHistoryFragmentNew22, bundle)
                            }else{
                                dialog2.dismiss()
                                it.exception!!.printStackTrace()
                                view.context.showToast(getString(R.string.failed_order), FancyToast.ERROR)
                            }
                        }
                }else{
                    dialog2.dismiss()
                    task.exception!!.printStackTrace()
                    view.context.showToast(getString(R.string.failed_order), FancyToast.ERROR)
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            val fullPhotoUri = data!!.data
            viewMain.imageView.setImageURI(fullPhotoUri)
            sharedPreferences.edit().putString("photo",fullPhotoUri.toString()).apply()
            imagePath = fullPhotoUri!!
        }
    }

    private fun placeOrder(view: View, name: String, image: Uri, userName: String, userNumber: String, userAddress: String, userNote: String) {
        dialog2.show()
        val key = "ORDER"+System.currentTimeMillis()
        val orderItemMain = OrderItemMain()
        orderItemMain.key = key
        orderItemMain.orderStatus = "VERIFIED"
        orderItemMain.userId = "ADMINUSERID123123123"
        orderItemMain.userPhoneAccount = userNumber
        orderItemMain.userName = userName
        orderItemMain.userNumber = userNumber
        orderItemMain.userNote = userNote
        orderItemMain.userAddress = userAddress
        orderItemMain.paymentMethod = if(viewMain.radioGroup.checkedRadioButtonId == R.id.rb1){ "bKash" }else{ "COD" }
        orderItemMain.totalPrice = totalChargeMain
        if(locationItems.isEmpty()){
            orderItemMain.locationItem = LocationItem()
        }else {
            if(viewMain.spinner_2.selectedItemPosition<0){
                orderItemMain.locationItem = LocationItem()
            }else{
                orderItemMain.locationItem = locationItems[viewMain.spinner_2.selectedItemPosition]
            }
        }
        orderItemMain.deliveryCharge = deliveryChargeMain
        orderItemMain.daCharge = daChargeMain
        orderItemMain.orderPlacingTimeStamp = if(dateLong==0L){
            System.currentTimeMillis()
        }else{
            dateLong
        }
        orderItemMain.pickUpTime = view.pickUpTimeTextView.text.toString()
        orderItemMain.lastTouchedTimeStamp = if(dateLong==0L){
            System.currentTimeMillis()
        }else{
            dateLong
        }
        orderItemMain.verifiedTimeStampMillis = if(dateLong==0L){
            System.currentTimeMillis()
        }else{
            dateLong
        }

        val firebaseStorage = FirebaseStorage.getInstance()
            .reference.child("ORDER_IMAGES")
            .child(key)

        val cartProductEntity = ArrayList<CartProductEntity>()
        val cartProductEntity2 = CartProductEntity()
        if(viewMain.orderTypeRadioGroup.checkedRadioButtonId == R.id.orderTypeCustomOrder){
            cartProductEntity2.custom_order_item = true
            cartProductEntity2.custom_order_text = name
            if(image.toString().isNotEmpty()){
                firebaseStorage.child("IMAGE_ORDER")
                    .putFile(image).addOnCompleteListener {
                        cartProductEntity2.custom_order_image = "IMAGE_ORDER"
                        cartProductEntity.add(cartProductEntity2)
                        orderItemMain.products = cartProductEntity
                        placeOrderFinalUpload(viewMain, orderItemMain)
                    }
            }else{
                cartProductEntity2.custom_order_image = ""
                cartProductEntity.add(cartProductEntity2)
                orderItemMain.products = cartProductEntity
                placeOrderFinalUpload(viewMain, orderItemMain)
            }
        }else{
            for(shopItem in mainShopItemHashMap){
                cartProductEntity.addAll(shopItem.cart_products)
            }
            orderItemMain.products = cartProductEntity
            placeOrderFinalUpload(viewMain, orderItemMain)
        }
    }

    private fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }

    override fun addProductToShop(position: Int, shopDocId: String) {
        val dialogShopSelectList = AlertDialog.Builder(context).create()
        val dialogShopSelectListView = LayoutInflater.from(context).inflate(R.layout.dialog_list_common, null)
        dialogShopSelectListView.dialogTitleCommon.text = "Choose Category"
        FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN_CATEGORY)
            .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
            .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
            .document(shopDocId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result!!.data != null) {
                        val categoryItemsArray = ArrayList<ProductCategoryItem>()
                        val categoryItemsNames = ArrayList<String>()
                        val map = task.result!!.data as Map<String, Map<String, String>>
                        for (category_field in map.entries) {
                            categoryItemsArray.add(
                                ProductCategoryItem(
                                    key = category_field.key,
                                    name = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_NAME].toString(),
                                    category_key = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_KEY].toString(),
                                    order = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_ORDER].toString()
                                        .toInt(),
                                )
                            )
                        }
                        Collections.sort(categoryItemsArray, kotlin.Comparator { o1, o2 ->
                            (o1.order).compareTo(o2.order)
                        })
                        for(shopItem in categoryItemsArray){
                            categoryItemsNames.add(shopItem.name)
                        }
                        dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(contextMain,
                            R.layout.support_simple_spinner_dropdown_item, categoryItemsNames)
                        dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position2, id ->
                            showProductAddDialogList(position, shopDocId, categoryItemsArray[position2])
                            dialogShopSelectList.dismiss()
                        }
                    }
                }
            }
        dialogShopSelectList.setView(dialogShopSelectListView)
        dialogShopSelectList.show()
    }

    override fun removeProductItem(rootAdapterPosition: Int, productItemPosition: Int) {
        mainShopItemHashMap[rootAdapterPosition].cart_products.removeAt(productItemPosition)
        shopProductAddItemRecyclerAdapter.notifyItemChanged(rootAdapterPosition)
    }

    override fun updateTotalProductsPricing() {
        totalChargeMain = 0
        for(shop in mainShopItemHashMap){
            shop.cart_products.forEach {
                totalChargeMain += (it.product_item_price*it.product_item_amount)
            }
        }
        viewMain.totalChargeEdittext.setText(totalChargeMain.toString())
        setPriceTotalOnView(viewMain)
    }

    private fun showProductAddDialogList(
        position: Int,
        shopDocId: String,
        productCategoryItem: ProductCategoryItem
    ) {
        val dialogShopSelectList = AlertDialog.Builder(context).create()
        val dialogShopSelectListView = LayoutInflater.from(context).inflate(admin.arpan.delivery.R.layout.dialog_list_common, null)
        dialogShopSelectListView.dialogTitleCommon.text = "Choose Product"
        FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
            .document(shopDocId).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
            .whereEqualTo("shopCategoryKey", productCategoryItem.category_key)
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    val productsMainArrayList = ArrayList<ProductItem>()
                    val productsMainArrayListNames = ArrayList<String>()
                    for(document in it.result!!.documents){
                        val obj = document.toObject(ProductItem::class.java)!!
                        obj.key = document.id
                        productsMainArrayList.add(obj)
                        productsMainArrayListNames.add(obj.name)
                    }
                    dialogShopSelectListView.dialogListViewCommon.adapter = ArrayAdapter(contextMain,
                        R.layout.support_simple_spinner_dropdown_item, productsMainArrayListNames)
                    dialogShopSelectListView.dialogListViewCommon.setOnItemClickListener { parent, view, position2, id ->
                        mainShopItemHashMap[position].cart_products.add(
                            CartProductEntity(
                                id= 0,
                                product_item = true,
                                product_item_price = productsMainArrayList[position2].price.toInt() + productsMainArrayList[position2].arpanCharge,
                                product_arpan_profit = productsMainArrayList[position2].arpanCharge,
                                product_item_amount = 1,
                                product_item_category_tag = productCategoryItem.category_key,
                                product_item_shop_key = shopDocId,
                                product_item_name = productsMainArrayList[position2].name,
                                product_item_desc = productsMainArrayList[position2].shortDescription
                            )
                        )
                        shopProductAddItemRecyclerAdapter.notifyItemChanged(position)
                        updateTotalProductsPricing()
                        dialogShopSelectList.dismiss()
                    }
                }else{
                    it.exception!!.printStackTrace()
                }
            }
        dialogShopSelectList.setView(dialogShopSelectListView)
        dialogShopSelectList.show()
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val time = "$hourOfDay:$minute"

        try {
            val sdf = SimpleDateFormat("H:mm")
            val dateObj = sdf.parse(time)
            System.out.println(dateObj)
            println(SimpleDateFormat("K:mm a").format(dateObj))
            viewMain.pickUpTimeTextView.text = SimpleDateFormat("K:mm a").format(dateObj).toString()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun onDateSet(
        view: com.wdullaer.materialdatetimepicker.date.DatePickerDialog?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        val monthOfYear2 = monthOfYear+1
        Log.e("time", year.toString())
        Log.e("time", monthOfYear2.toString())
        Log.e("time", dayOfMonth.toString())
        val now = Calendar.getInstance()
        val time = "$year/$monthOfYear2/$dayOfMonth-${now.get(Calendar.HOUR_OF_DAY)}/${now.get(Calendar.MINUTE)}"
        try {
            val sdf = SimpleDateFormat("yyyy/MM/dd-hh/mm")
            val dateObj = sdf.parse(time)
            System.out.println(dateObj)
            Log.e("time", dateObj.time.toString())
            dateLong = dateObj.time
            viewMain.pickUpDateTextView.text = "$dayOfMonth-$monthOfYear2-$year"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}