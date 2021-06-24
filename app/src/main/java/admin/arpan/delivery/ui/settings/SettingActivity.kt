package admin.arpan.delivery.ui.settings

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.LocationItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.NormalBannersPopUpAdapter
import admin.arpan.delivery.db.adapter.TimeBasedBannersPopUpAdapter
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.db.model.SlidingTextItem
import admin.arpan.delivery.utils.Constants
import android.net.ParseException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_add_location.view.*
import kotlinx.android.synthetic.main.dialog_add_location.view.addLocationConfirmButton
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonBgColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonTextColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.edt_name
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.order
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.specialOfferTextView
import kotlinx.android.synthetic.main.dialog_add_time_based_banner.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SettingActivity : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseDatabase: FirebaseDatabase

    private var orderStartTimeLimitString = "00:00"
    private var orderEndTimeLimitString = "00:00"
    private var orderAllowOverTimeOrder = "no"

    private var customOrdersMaxTimeLimitString = 0
    private var medicineOrdersMaxTimeLimitString = 0
    private var parcelOrdersMaxTimeLimitString = 0
    private var totalCustomOrdersMaxTimeLimitString = 0

    private var maxShopPerOrderInt = 0
    private var maxChargeAfterPershopMaxOrder = 0
    private var maxDaChargeAfterPershopMaxOrder = 0
    private var allowOrderingMoreThanMaxShops = false

    private var alertDialogEmergencyTitleText = ""
    private var alertDialogEmergencyMessageText = ""
    private var alertDialogeEmergencyStatus = "openapp"

    var normalLocationsItemsArrayList = ArrayList<LocationItem>()
    lateinit var normalLocationsItemsRecyclerAdapter : LocationItemRecyclerAdapter
    var pickDropLocationsItemsArrayList = ArrayList<LocationItem>()
    lateinit var pickDropLocationsItemsRecyclerAdapter : LocationItemRecyclerAdapter

    var normalBannerPopUpArrayList = ArrayList<SlidingTextItem>()
    lateinit var normalBannersPopUpAdapter: NormalBannersPopUpAdapter
    var timeBasedBannerPopUpArrayList = ArrayList<SlidingTextItem>()
    lateinit var timeBasedBannersPopUpAdapter : TimeBasedBannersPopUpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initVars()
        initLogics()
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun initLogics() {
        initOrderDataLimitTimeLogic()
        initCustomOrderMaxLimitTimeLogic()
        initShopPerMaxOrderDataLimitTimeLogic()
        initDialogEmergencyMainLogic()
        initNormalDeliveryChargesLogic()
        initPickDropNormalDeliveryChargesLogic()
        initTimeBasedNotificationsPopUpLogic()
    }

    private fun initTimeBasedNotificationsPopUpLogic() {
        firebaseFirestore.collection(Constants.FC_OFFERS_OI)
            .document("timebased_notifications_document")
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result!!.data!!.entries.isNotEmpty()) {
                        timeBasedBannerPopUpArrayList.clear()
                        val map = it.result!!.data!! as HashMap<String, HashMap<String, Any>>
                        for (docField in map.entries) {
                            val d = SlidingTextItem()
                            d.key = docField.key
                            d.enabled = docField.value["enabled"] as Boolean
                            d.textTitle = docField.value["textTitle"] as String
                            d.textDescription = docField.value["textDescription"] as String
                            d.timeBased = docField.value["timeBased"] as Boolean
                            d.startTime = docField.value["startTime"] as Long
                            d.endTime = docField.value["endTime"] as Long
                            d.backgroundColorHex = docField.value["backgroundColorHex"] as String
                            d.textColorHex = docField.value["textColorHex"] as String
                            d.order = docField.value["order"] as Long
                            d.startTimeString = docField.value["startTimeString"] as String
                            d.endTimeString = docField.value["endTimeString"] as String
                            timeBasedBannerPopUpArrayList.add(d)
                        }
                        timeBasedBannersPopUpAdapter = TimeBasedBannersPopUpAdapter(this, timeBasedBannerPopUpArrayList)
                        timeBasedNotifications.layoutManager = LinearLayoutManager(this)
                        timeBasedNotifications.adapter = timeBasedBannersPopUpAdapter

                        addTimeBasedNotifications.setOnClickListener {
                            val alertDialog = AlertDialog.Builder(this).create()
                            val locationAlertDialogViewMain = LayoutInflater.from(this)
                                .inflate(R.layout.dialog_add_time_based_banner, null)
                            var textColor = "#ffffff"
                            var bgColor = "#43A047"
                            locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
                                ColorPickerDialog
                                    .Builder(this)        				// Pass Activity Instance
                                    .setTitle("Pick Text Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(textColor)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        textColor = colorHex
                                        locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
                                    }
                                    .show()
                            }
                            locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
                                ColorPickerDialog
                                    .Builder(this)        				// Pass Activity Instance
                                    .setTitle("Pick Background Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(bgColor)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        bgColor = colorHex
                                        locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(color)
                                    }
                                    .show()
                            }
                            locationAlertDialogViewMain.edt_name.doOnTextChanged { text, start, before, count ->
                                locationAlertDialogViewMain.specialOfferTextView.text = text
                            }
                            locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                                if(locationAlertDialogViewMain.edt_name.text.isNotEmpty()&&
                                    locationAlertDialogViewMain.edt_start_time.text.isNotEmpty()&&
                                    locationAlertDialogViewMain.edt_end_time.text.isNotEmpty()){
                                    locationAlertDialogViewMain.buttonTextColor.isEnabled = false
                                    locationAlertDialogViewMain.buttonBgColor.isEnabled = false
                                    locationAlertDialogViewMain.edt_name.isEnabled = false
                                    locationAlertDialogViewMain.edt_start_time.isEnabled = false
                                    locationAlertDialogViewMain.edt_end_time.isEnabled = false
                                    locationAlertDialogViewMain.order.isEnabled = false
                                    locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                                    val d = SlidingTextItem()
                                    d.key = "STI"+System.currentTimeMillis()
                                    d.enabled = true
                                    d.textTitle = locationAlertDialogViewMain.edt_name.text.toString()
                                    d.startTimeString = locationAlertDialogViewMain.edt_start_time.text.toString()
                                    d.endTimeString = locationAlertDialogViewMain.edt_end_time.text.toString()
                                    d.textDescription = ""
                                    d.timeBased = true
                                    d.backgroundColorHex = bgColor
                                    d.textColorHex = textColor
                                    d.order = if(locationAlertDialogViewMain.order.text.isEmpty()){
                                        0
                                    }else{
                                        locationAlertDialogViewMain.order.text.toString().toLong()
                                    }
                                    val hashMap = HashMap<String,Any>()
                                    hashMap[d.key] = d
                                    FirebaseFirestore.getInstance()
                                        .collection(Constants.FC_OFFERS_OI)
                                        .document("timebased_notifications_document")
                                        .update(hashMap)
                                        .addOnCompleteListener {
                                            timeBasedBannerPopUpArrayList.add(d)
                                            timeBasedBannersPopUpAdapter.notifyItemInserted(timeBasedBannerPopUpArrayList.size-1)
                                            alertDialog.dismiss()
                                        }
                                }
                            }
                            alertDialog.setView(locationAlertDialogViewMain)
                            alertDialog.show()
                        }
                    }
                } else {
                    it.exception!!.printStackTrace()
                }
                initNormalBannerNotificationsPopUpLogic()
            }
    }

    private fun initNormalBannerNotificationsPopUpLogic() {
        firebaseFirestore.collection(Constants.FC_OFFERS_OI)
            .document("normal_notifications_document")
            .get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    if(task.result!!.data!!.entries.isNotEmpty()){
                        normalBannerPopUpArrayList.clear()
                        val map = task.result!!.data!! as HashMap<String, HashMap<String,Any>>
                        for(docField in map.entries) {
                            val d = SlidingTextItem()
                            d.key = docField.key
                            d.enabled = docField.value["enabled"] as Boolean
                            d.textTitle = docField.value["textTitle"] as String
                            d.textDescription = docField.value["textDescription"] as String
                            d.timeBased = docField.value["timeBased"] as Boolean
                            d.startTime = docField.value["startTime"] as Long
                            d.endTime = docField.value["endTime"] as Long
                            d.backgroundColorHex = docField.value["backgroundColorHex"] as String
                            d.textColorHex = docField.value["textColorHex"] as String
                            d.order = docField.value["order"] as Long
                            normalBannerPopUpArrayList.add(d)
                        }
                        normalBannersPopUpAdapter = NormalBannersPopUpAdapter(this, normalBannerPopUpArrayList)
                        normalNotificationsPopUp.layoutManager = LinearLayoutManager(this)
                        normalNotificationsPopUp.adapter = normalBannersPopUpAdapter

                        addNormalNotifications.setOnClickListener {
                            val alertDialog = AlertDialog.Builder(this).create()
                            val locationAlertDialogViewMain = LayoutInflater.from(this)
                                .inflate(R.layout.dialog_add_normal_banner, null)
                            var textColor = "#ffffff"
                            var bgColor = "#43A047"
                            locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
                                ColorPickerDialog
                                    .Builder(this)        				// Pass Activity Instance
                                    .setTitle("Pick Text Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(textColor)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        textColor = colorHex
                                        locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
                                    }
                                    .show()
                            }
                            locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
                                ColorPickerDialog
                                    .Builder(this)        				// Pass Activity Instance
                                    .setTitle("Pick Background Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(bgColor)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        bgColor = colorHex
                                        locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(color)
                                    }
                                    .show()
                            }
                            locationAlertDialogViewMain.edt_name.doOnTextChanged { text, start, before, count ->
                                locationAlertDialogViewMain.specialOfferTextView.text = text
                            }
                            locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                                if(locationAlertDialogViewMain.edt_name.text.isNotEmpty()){
                                    locationAlertDialogViewMain.buttonTextColor.isEnabled = false
                                    locationAlertDialogViewMain.buttonBgColor.isEnabled = false
                                    locationAlertDialogViewMain.edt_name.isEnabled = false
                                    locationAlertDialogViewMain.order.isEnabled = false
                                    locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                                    val d = SlidingTextItem()
                                    d.key = "STI"+System.currentTimeMillis()
                                    d.enabled = true
                                    d.textTitle = locationAlertDialogViewMain.edt_name.text.toString()
                                    d.textDescription = ""
                                    d.timeBased = false
                                    d.backgroundColorHex = bgColor
                                    d.textColorHex = textColor
                                    d.order = if(locationAlertDialogViewMain.order.text.isEmpty()){
                                        0
                                    }else{
                                        locationAlertDialogViewMain.order.text.toString().toLong()
                                    }
                                    val hashMap = HashMap<String,Any>()
                                    hashMap[d.key] = d
                                    FirebaseFirestore.getInstance()
                                        .collection(Constants.FC_OFFERS_OI)
                                        .document("normal_notifications_document")
                                        .update(hashMap)
                                        .addOnCompleteListener {
                                            normalBannerPopUpArrayList.add(d)
                                            normalBannersPopUpAdapter.notifyItemInserted(normalBannerPopUpArrayList.size-1)
                                            alertDialog.dismiss()
                                        }
                                }
                            }
                            alertDialog.setView(locationAlertDialogViewMain)
                            alertDialog.show()
                        }
                    }
                }else{
                    task.exception!!.printStackTrace()
                }
            }
    }

    private fun initNormalDeliveryChargesLogic() {
        FirebaseDatabase.getInstance()
            .reference
            .child("data")
            .child("delivery_charges")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    normalLocationsItemsArrayList.clear()
                    for (snap in it.result!!.children) {
                        normalLocationsItemsArrayList.add(
                            LocationItem(
                                key = snap.key.toString(),
                                locationName = snap.child("name").value.toString(),
                                deliveryCharge = snap.child("deliveryCharge").value.toString().toInt(),
                                daCharge = snap.child("daCharge").value.toString().toInt(),
                            )
                        )
                    }
                    deliveryChargeRecyclerView.layoutManager = LinearLayoutManager(this)
                    normalLocationsItemsRecyclerAdapter =
                        LocationItemRecyclerAdapter(this@SettingActivity,
                            normalLocationsItemsArrayList, "delivery_charges")
                    deliveryChargeRecyclerView.adapter = normalLocationsItemsRecyclerAdapter
                    addNormalDeliveryCharge.setOnClickListener{
                        val alertDialog = AlertDialog.Builder(this).create()
                        val locationAlertDialogViewMain = LayoutInflater.from(this)
                            .inflate(R.layout.dialog_add_location, null)
                        locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                            val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
                            val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
                            val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
                            if(
                                locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
                                && daCharge.isNotEmpty()
                            ){
                                locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                                locationAlertDialogViewMain.edt_location_name.isEnabled = false
                                locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
                                locationAlertDialogViewMain.edt_da_charge.isEnabled = false
                                alertDialog.setCancelable(false)
                                alertDialog.setCanceledOnTouchOutside(false)
                                val key = "NLI"+System.currentTimeMillis()
                                val locationItem = HashMap<String,String>()
                                locationItem["name"] = locationName
                                locationItem["deliveryCharge"] = deliveryCharge
                                locationItem["daCharge"] = daCharge
                                FirebaseDatabase.getInstance()
                                    .reference
                                    .child("data")
                                    .child("delivery_charges")
                                    .child(key)
                                    .setValue(locationItem)
                                    .addOnCompleteListener {
                                        normalLocationsItemsArrayList.add(
                                            LocationItem(
                                                key,
                                                locationName,
                                                deliveryCharge.toInt(),
                                                daCharge.toInt()
                                            )
                                        )
                                        normalLocationsItemsRecyclerAdapter.notifyItemInserted(normalLocationsItemsArrayList.size-1)
                                        alertDialog.dismiss()
                                    }
                            }
                        }
                        alertDialog.setView(locationAlertDialogViewMain)
                        alertDialog.show()
                    }
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }
    private fun initPickDropNormalDeliveryChargesLogic() {
        FirebaseDatabase.getInstance()
            .reference
            .child("data")
            .child("delivery_charges_pick_drop")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    pickDropLocationsItemsArrayList.clear()
                    for (snap in it.result!!.children) {
                        pickDropLocationsItemsArrayList.add(
                            LocationItem(
                                key = snap.key.toString(),
                                locationName = snap.child("name").value.toString(),
                                deliveryCharge = snap.child("deliveryCharge").value.toString().toInt(),
                                daCharge = snap.child("daCharge").value.toString().toInt(),
                            )
                        )
                    }
                    pickDropDeliveryChargeRecyclerView.layoutManager = LinearLayoutManager(this)
                    pickDropLocationsItemsRecyclerAdapter =
                        LocationItemRecyclerAdapter(this@SettingActivity,
                            pickDropLocationsItemsArrayList, "delivery_charges_pick_drop")
                    pickDropDeliveryChargeRecyclerView.adapter = pickDropLocationsItemsRecyclerAdapter
                    addPickDropDeliveryCharge.setOnClickListener{
                        val alertDialog = AlertDialog.Builder(this).create()
                        val locationAlertDialogViewMain = LayoutInflater.from(this)
                            .inflate(R.layout.dialog_add_location, null)
                        locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                            val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
                            val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
                            val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
                            if(
                                locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
                                && daCharge.isNotEmpty()
                            ){
                                locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                                locationAlertDialogViewMain.edt_location_name.isEnabled = false
                                locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
                                locationAlertDialogViewMain.edt_da_charge.isEnabled = false
                                alertDialog.setCancelable(false)
                                alertDialog.setCanceledOnTouchOutside(false)
                                val key = "NLI"+System.currentTimeMillis()
                                val locationItem = HashMap<String,String>()
                                locationItem["name"] = locationName
                                locationItem["deliveryCharge"] = deliveryCharge
                                locationItem["daCharge"] = daCharge
                                FirebaseDatabase.getInstance()
                                    .reference
                                    .child("data")
                                    .child("delivery_charges_pick_drop")
                                    .child(key)
                                    .setValue(locationItem)
                                    .addOnCompleteListener {
                                        pickDropLocationsItemsArrayList.add(
                                            LocationItem(
                                                key,
                                                locationName,
                                                deliveryCharge.toInt(),
                                                daCharge.toInt()
                                            )
                                        )
                                        pickDropLocationsItemsRecyclerAdapter.notifyItemInserted(pickDropLocationsItemsArrayList.size-1)
                                        alertDialog.dismiss()
                                    }
                            }
                        }
                        alertDialog.setView(locationAlertDialogViewMain)
                        alertDialog.show()
                    }
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }

    private fun initOrderDataLimitTimeLogic() {
        saveOrderTimeButton.isEnabled = false
        startTimeOrder.isEnabled = false
        endTimeOrder.isEnabled = false
        allowOverTimeCheckBox.isEnabled = false
        firebaseDatabase.reference.child("ORDER_TAKING_TIME")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    orderStartTimeLimitString = it.result!!.child("start_time").value.toString()
                    orderEndTimeLimitString = it.result!!.child("end_time").value.toString()
                    orderAllowOverTimeOrder = it.result!!.child("over_time_orders").value.toString()
                    placeDataOnEdittextsOrderLimitsCheckbox()
                    initListenersForOrderLimitEdittextsAndCheckBoxes()
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }
    private fun placeDataOnEdittextsOrderLimitsCheckbox() {
        startTimeOrder.setText(orderStartTimeLimitString)
        endTimeOrder.setText(orderEndTimeLimitString)
        allowOverTimeCheckBox.isChecked = orderAllowOverTimeOrder == "yes"
        startTimeOrder.isEnabled = true
        endTimeOrder.isEnabled = true
        allowOverTimeCheckBox.isEnabled = true
    }
    private fun initListenersForOrderLimitEdittextsAndCheckBoxes() {
        startTimeOrder.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatus()
        }
        endTimeOrder.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatus()
        }
        allowOverTimeCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkButtonShouldBeEnabledOrNotStatus()
        }
    }
    private fun checkButtonShouldBeEnabledOrNotStatus() {
        saveOrderTimeButton.isEnabled = startTimeOrder.text.toString()!=orderStartTimeLimitString ||
                endTimeOrder.text.toString()!=orderEndTimeLimitString ||
                allowOverTimeCheckBox.isChecked != (orderAllowOverTimeOrder == "yes")

        saveOrderTimeButton.setOnClickListener {
            saveOrderTimeButton.isEnabled = false
            startTimeOrder.isEnabled = false
            endTimeOrder.isEnabled = false
            allowOverTimeCheckBox.isEnabled = false
            val hashMap = HashMap<String,String>()
            hashMap["start_time"] = startTimeOrder.text.toString()
            hashMap["end_time"] = endTimeOrder.text.toString()
            hashMap["over_time_orders"] = if(allowOverTimeCheckBox.isChecked){ "yes" }else{ "no" }
            firebaseDatabase.reference.child("ORDER_TAKING_TIME")
                .setValue(hashMap).addOnCompleteListener {
                    startTimeOrder.isEnabled = true
                    endTimeOrder.isEnabled = true
                    allowOverTimeCheckBox.isEnabled = true
                    if(it.isSuccessful){
                        orderStartTimeLimitString = startTimeOrder.text.toString()
                        orderEndTimeLimitString = endTimeOrder.text.toString()
                        orderAllowOverTimeOrder = if(allowOverTimeCheckBox.isChecked){ "yes" }else{ "no" }
                    }else{
                        saveOrderTimeButton.isEnabled = true
                    }
                }
        }
    }

    private fun initCustomOrderMaxLimitTimeLogic() {
        saveCustomCategoryOrderLimitsButton.isEnabled = false
        customCategoryMaxOrderLimitEdittext.isEnabled = false
        medicineCategoryMaxOrderLimitEdittext.isEnabled = false
        parcelCategoryMaxOrderLimitEdittext.isEnabled = false
        totalCategoryMaxOrderLimitEdittext.isEnabled = false
        FirebaseDatabase.getInstance().reference
            .child("data")
            .child("order_custom_limits")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    parcelOrdersMaxTimeLimitString = it.result!!.child("parcel").value.toString().toInt()
                    customOrdersMaxTimeLimitString = it.result!!.child("custom_cat").value.toString().toInt()
                    medicineOrdersMaxTimeLimitString = it.result!!.child("medicine").value.toString().toInt()
                    totalCustomOrdersMaxTimeLimitString = it.result!!.child("max_categories").value.toString().toInt()
                    placeDataOnEdittextsMaxCustomOrderLimitsCheckbox()
                    initListenersForOrderMaxCustomLimitEdittextsAndCheckBoxes()
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }
    private fun placeDataOnEdittextsMaxCustomOrderLimitsCheckbox() {
        customCategoryMaxOrderLimitEdittext.setText(customOrdersMaxTimeLimitString.toString())
        medicineCategoryMaxOrderLimitEdittext.setText(medicineOrdersMaxTimeLimitString.toString())
        parcelCategoryMaxOrderLimitEdittext.setText(parcelOrdersMaxTimeLimitString.toString())
        totalCategoryMaxOrderLimitEdittext.setText(totalCustomOrdersMaxTimeLimitString.toString())

        customCategoryMaxOrderLimitEdittext.isEnabled = true
        medicineCategoryMaxOrderLimitEdittext.isEnabled = true
        parcelCategoryMaxOrderLimitEdittext.isEnabled = true
        totalCategoryMaxOrderLimitEdittext.isEnabled = true
    }
    private fun initListenersForOrderMaxCustomLimitEdittextsAndCheckBoxes() {
        customCategoryMaxOrderLimitEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatusOrderMain()
        }
        medicineCategoryMaxOrderLimitEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatusOrderMain()
        }
        parcelCategoryMaxOrderLimitEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatusOrderMain()
        }
        totalCategoryMaxOrderLimitEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShouldBeEnabledOrNotStatusOrderMain()
        }
    }
    private fun checkButtonShouldBeEnabledOrNotStatusOrderMain() {
        saveCustomCategoryOrderLimitsButton.isEnabled =
            customCategoryMaxOrderLimitEdittext.text.toString()!=customOrdersMaxTimeLimitString.toString() ||
                    medicineCategoryMaxOrderLimitEdittext.text.toString()!=medicineOrdersMaxTimeLimitString.toString() ||
                    parcelCategoryMaxOrderLimitEdittext.text.toString()!=parcelOrdersMaxTimeLimitString.toString() ||
                    totalCategoryMaxOrderLimitEdittext.text.toString()!=totalCustomOrdersMaxTimeLimitString.toString()

        saveCustomCategoryOrderLimitsButton.setOnClickListener {
            saveCustomCategoryOrderLimitsButton.isEnabled = false
            customCategoryMaxOrderLimitEdittext.isEnabled = false
            medicineCategoryMaxOrderLimitEdittext.isEnabled = false
            parcelCategoryMaxOrderLimitEdittext.isEnabled = false
            totalCategoryMaxOrderLimitEdittext.isEnabled = false
            val hashMap = HashMap<String,String>()
            hashMap["parcel"] = parcelCategoryMaxOrderLimitEdittext.text.toString()
            hashMap["custom_cat"] = customCategoryMaxOrderLimitEdittext.text.toString()
            hashMap["medicine"] = medicineCategoryMaxOrderLimitEdittext.text.toString()
            hashMap["max_categories"] = totalCategoryMaxOrderLimitEdittext.text.toString()
            FirebaseDatabase.getInstance().reference
                .child("data")
                .child("order_custom_limits")
                .setValue(hashMap).addOnCompleteListener {
                    customCategoryMaxOrderLimitEdittext.isEnabled = true
                    medicineCategoryMaxOrderLimitEdittext.isEnabled = true
                    parcelCategoryMaxOrderLimitEdittext.isEnabled = true
                    totalCategoryMaxOrderLimitEdittext.isEnabled = true
                    if(it.isSuccessful){
                        customOrdersMaxTimeLimitString = customCategoryMaxOrderLimitEdittext.text.toString().toInt()
                        medicineOrdersMaxTimeLimitString = medicineCategoryMaxOrderLimitEdittext.text.toString().toInt()
                        parcelOrdersMaxTimeLimitString = parcelCategoryMaxOrderLimitEdittext.text.toString().toInt()
                        totalCustomOrdersMaxTimeLimitString = totalCategoryMaxOrderLimitEdittext.text.toString().toInt()
                    }else{
                        saveCustomCategoryOrderLimitsButton.isEnabled = true
                    }
                }
        }
    }

    private fun initShopPerMaxOrderDataLimitTimeLogic() {
        saveShopOrderExtraLimitButton.isEnabled = false
        maxOrderFromEachShopEdittext.isEnabled = false
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
        firebaseDatabase.reference.child("data").child("order_shop_limits")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    maxShopPerOrderInt = (it.result!!.child("max_shops").value as Long).toInt()
                    maxChargeAfterPershopMaxOrder = (it.result!!.child("delivery_charge_extra").value as Long).toInt()
                    maxDaChargeAfterPershopMaxOrder = (it.result!!.child("da_charge_extra").value as Long).toInt()
                    allowOrderingMoreThanMaxShops = it.result!!.child("allow_more").value as Boolean
                    placeDataOnEdittextsPerShopMaxOrderLimitsCheckbox()
                    initListenersForPerShopMaxOrderLimitEdittextsAndCheckBoxes()
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }
    private fun placeDataOnEdittextsPerShopMaxOrderLimitsCheckbox() {
        maxOrderFromEachShopEdittext.setText(maxShopPerOrderInt.toString())
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.setText(maxChargeAfterPershopMaxOrder.toString())
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.setText(maxDaChargeAfterPershopMaxOrder.toString())
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked = allowOrderingMoreThanMaxShops
        maxOrderFromEachShopEdittext.isEnabled = true
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = true
    }
    private fun initListenersForPerShopMaxOrderLimitEdittextsAndCheckBoxes() {
        maxOrderFromEachShopEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
    }
    private fun checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus() {
        saveShopOrderExtraLimitButton.isEnabled = maxOrderFromEachShopEdittext.text.toString()!=maxShopPerOrderInt.toString() ||
                extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString()!=maxChargeAfterPershopMaxOrder.toString() ||
                extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString()!=maxDaChargeAfterPershopMaxOrder.toString() ||
                allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked != allowOrderingMoreThanMaxShops

        saveShopOrderExtraLimitButton.setOnClickListener {
            saveShopOrderExtraLimitButton.isEnabled = false
            maxOrderFromEachShopEdittext.isEnabled = false
            extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
            extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
            allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
            val hashMap = HashMap<String,Any>()
            hashMap["max_shops"] = maxOrderFromEachShopEdittext.text.toString().toInt()
            hashMap["delivery_charge_extra"] = extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
            hashMap["da_charge_extra"] = extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
            hashMap["allow_more"] = allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked
            firebaseDatabase.reference.child("data").child("order_shop_limits")
                .setValue(hashMap).addOnCompleteListener {
                    maxOrderFromEachShopEdittext.isEnabled = true
                    extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
                    extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
                    allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = true
                    if(it.isSuccessful){
                        maxShopPerOrderInt = maxOrderFromEachShopEdittext.text.toString().toInt()
                        maxChargeAfterPershopMaxOrder = extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
                        maxDaChargeAfterPershopMaxOrder = extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
                        allowOrderingMoreThanMaxShops = allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked
                    }else{
                        saveShopOrderExtraLimitButton.isEnabled = true
                    }
                }
        }
    }

    private fun initDialogEmergencyMainLogic() {
        emergencyModeDialogSaveButton.isEnabled = false
        emergencyModeDialogTitleEditText.isEnabled = false
        emergencyModeDialogMessageEdtittext.isEnabled = false
        emergencyModeDialogActivityCheckBox.isEnabled = false
        firebaseDatabase.reference.child("emergency_dialog_data")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    alertDialogEmergencyTitleText = it.result!!.child("title").value.toString()
                    alertDialogEmergencyMessageText = it.result!!.child("body").value.toString()
                    alertDialogeEmergencyStatus = it.result!!.child("state").value.toString()
                    placeDataOnEdittextsDialogEdittextEmergencyCheckbox()
                    initListenersForDialogEmegencyEdittextsAndCheckBoxes()
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }
    private fun placeDataOnEdittextsDialogEdittextEmergencyCheckbox() {
        emergencyModeDialogTitleEditText.setText(alertDialogEmergencyTitleText)
        emergencyModeDialogMessageEdtittext.setText(alertDialogEmergencyMessageText)
        emergencyModeDialogActivityCheckBox.isChecked = alertDialogeEmergencyStatus == "active"
        emergencyModeDialogTitleEditText.isEnabled = true
        emergencyModeDialogMessageEdtittext.isEnabled = true
        emergencyModeDialogActivityCheckBox.isEnabled = true
    }
    private fun initListenersForDialogEmegencyEdittextsAndCheckBoxes() {
        emergencyModeDialogTitleEditText.doOnTextChanged { text, start, before, count ->
            checkEmergencyDialogMainButtonShouldBeEnabledOrNotStatus()
        }
        emergencyModeDialogMessageEdtittext.doOnTextChanged { text, start, before, count ->
            checkEmergencyDialogMainButtonShouldBeEnabledOrNotStatus()
        }
        emergencyModeDialogActivityCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkEmergencyDialogMainButtonShouldBeEnabledOrNotStatus()
        }
    }
    private fun checkEmergencyDialogMainButtonShouldBeEnabledOrNotStatus() {
        emergencyModeDialogSaveButton.isEnabled =
            emergencyModeDialogTitleEditText.text.toString()!=alertDialogEmergencyTitleText ||
                    emergencyModeDialogMessageEdtittext.text.toString()!=alertDialogEmergencyMessageText ||
                    emergencyModeDialogActivityCheckBox.isChecked != (alertDialogeEmergencyStatus == "active")

        emergencyModeDialogSaveButton.setOnClickListener {
            emergencyModeDialogSaveButton.isEnabled = false
            emergencyModeDialogActivityCheckBox.isEnabled = false
            emergencyModeDialogMessageEdtittext.isEnabled = false
            emergencyModeDialogTitleEditText.isEnabled = false
            val hashMap = HashMap<String,String>()
            hashMap["title"] = emergencyModeDialogTitleEditText.text.toString()
            hashMap["body"] = emergencyModeDialogMessageEdtittext.text.toString()
            hashMap["state"] = if(emergencyModeDialogActivityCheckBox.isChecked){ "active" }else{ "openapp" }
            firebaseDatabase.reference.child("emergency_dialog_data")
                .setValue(hashMap).addOnCompleteListener {
                    emergencyModeDialogMessageEdtittext.isEnabled = true
                    emergencyModeDialogTitleEditText.isEnabled = true
                    emergencyModeDialogActivityCheckBox.isEnabled = true
                    if(it.isSuccessful){
                        alertDialogEmergencyTitleText = startTimeOrder.text.toString()
                        alertDialogEmergencyMessageText = endTimeOrder.text.toString()
                        alertDialogeEmergencyStatus = if(emergencyModeDialogActivityCheckBox.isChecked){ "active" }else{ "openapp" }
                    }else{
                        emergencyModeDialogSaveButton.isEnabled = true
                    }
                }
        }
    }
}