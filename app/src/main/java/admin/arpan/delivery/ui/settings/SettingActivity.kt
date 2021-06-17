package admin.arpan.delivery.ui.settings

import admin.arpan.delivery.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_setting.*

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
    private var allowOrderingMoreThanMaxShops = false

    private var alertDialogEmergencyTitleText = ""
    private var alertDialogEmergencyMessageText = ""
    private var alertDialogeEmergencyStatus = "openapp"

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
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
        firebaseDatabase.reference.child("data").child("order_shop_limits")
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    maxShopPerOrderInt = (it.result!!.child("max_shops").value as Long).toInt()
                    maxChargeAfterPershopMaxOrder = (it.result!!.child("delivery_charge_extra").value as Long).toInt()
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
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked = allowOrderingMoreThanMaxShops
        maxOrderFromEachShopEdittext.isEnabled = true
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = true
    }
    private fun initListenersForPerShopMaxOrderLimitEdittextsAndCheckBoxes() {
        maxOrderFromEachShopEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.doOnTextChanged { text, start, before, count ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus()
        }
    }
    private fun checkButtonShopPerMaxOrderShouldBeEnabledOrNotStatus() {
        saveShopOrderExtraLimitButton.isEnabled = maxOrderFromEachShopEdittext.text.toString()!=maxShopPerOrderInt.toString() ||
                extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString()!=maxChargeAfterPershopMaxOrder.toString() ||
                allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked != allowOrderingMoreThanMaxShops

        saveShopOrderExtraLimitButton.setOnClickListener {
            saveShopOrderExtraLimitButton.isEnabled = false
            maxOrderFromEachShopEdittext.isEnabled = false
            extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
            allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
            val hashMap = HashMap<String,Any>()
            hashMap["max_shops"] = maxOrderFromEachShopEdittext.text.toString().toInt()
            hashMap["delivery_charge_extra"] = extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
            hashMap["allow_more"] = allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked
            firebaseDatabase.reference.child("data").child("order_shop_limits")
                .setValue(hashMap).addOnCompleteListener {
                    maxOrderFromEachShopEdittext.isEnabled = true
                    extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
                    allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = true
                    if(it.isSuccessful){
                        maxShopPerOrderInt = maxOrderFromEachShopEdittext.text.toString().toInt()
                        maxChargeAfterPershopMaxOrder = extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
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