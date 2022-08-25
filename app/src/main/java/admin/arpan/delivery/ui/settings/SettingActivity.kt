package admin.arpan.delivery.ui.settings

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.LocationItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.SlidingItemAdapter
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.models.SlidingTextItem
import admin.arpan.delivery.models.Location
import admin.arpan.delivery.models.Setting
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.LocationViewModel
import admin.arpan.delivery.viewModels.NoticeViewModel
import admin.arpan.delivery.viewModels.SettingViewModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.shashank.sony.fancytoastlib.FancyToast
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_add_location.view.*
import kotlinx.android.synthetic.main.dialog_add_location.view.addLocationConfirmButton
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonBgColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonTextColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.edt_name
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.order
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.specialOfferTextView
import kotlinx.android.synthetic.main.dialog_add_time_based_banner.view.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {
  private var orderStartTimeLimitString = "00:00"
  private var orderEndTimeLimitString = "00:00"
  private var orderAllowOverTimeOrder = false

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

  var normalLocationsItemsArrayList = ArrayList<Location>()
  lateinit var normalLocationsItemsRecyclerAdapter: LocationItemRecyclerAdapter

  var normalBannerPopUpArrayList = ArrayList<SlidingTextItem>()
  lateinit var normalBannersPopUpAdapter: SlidingItemAdapter
  var timeBasedBannerPopUpArrayList = ArrayList<SlidingTextItem>()
  lateinit var slidingItemAdapter: SlidingItemAdapter

  private val locationViewModel: LocationViewModel by viewModels()
  private val noticeViewModel: NoticeViewModel by viewModels()
  private val settingViewModel: SettingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_setting)

    initLogics()
  }

  private fun initLogics() {
    initOrderDataLimitTimeLogic()
    initNormalDeliveryChargesLogic()
    initTimeBasedNotificationsPopUpLogic()
  }

  private fun initTimeBasedNotificationsPopUpLogic() {
    LiveDataUtil.observeOnce(noticeViewModel.getAllItems()) { gANotices ->
      if (gANotices.error != true) {
        timeBasedBannerPopUpArrayList.clear()
        normalBannerPopUpArrayList.clear()
        for (item in gANotices.results) {
          if (item.timeBased) {
            timeBasedBannerPopUpArrayList.add(item)
          } else {
            normalBannerPopUpArrayList.add(item)
          }
        }
        slidingItemAdapter =
          SlidingItemAdapter(this, timeBasedBannerPopUpArrayList, noticeViewModel, true)
        timeBasedNotifications.layoutManager = LinearLayoutManager(this)
        timeBasedNotifications.adapter = slidingItemAdapter
        normalBannersPopUpAdapter =
          SlidingItemAdapter(this, normalBannerPopUpArrayList, noticeViewModel, false)
        normalNotificationsPopUp.layoutManager = LinearLayoutManager(this)
        normalNotificationsPopUp.adapter = normalBannersPopUpAdapter
        addNormalNotifications.setOnClickListener {
          val alertDialog = AlertDialog.Builder(this).create()
          val locationAlertDialogViewMain = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_normal_banner, null)
          var textColor = "#ffffff"
          var bgColor = "#43A047"
          locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
            MaterialColorPickerDialog
              .Builder(this)          // Pass Activity Instance
              .setTitle("Pick Text Color")
              .setColors(
                arrayListOf(
                  "#FFFFFF",
                  "#000000",
                  "#3D3D3D",
                  "#29ABE2",
                  "#F7931E",
                  "#FFFF00",
                  "#ED1C24",
                  "#009245",
                  "#662D91",
                  "#D4145A"
                )
              )
              .setColorListener { color, colorHex ->
                textColor = colorHex
                locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
              }
              .show()
          }
          locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
            MaterialColorPickerDialog
              .Builder(this)          // Pass Activity Instance
              .setTitle("Pick Text Color")
              .setColors(
                arrayListOf(
                  "#FFFFFF",
                  "#000000",
                  "#3D3D3D",
                  "#29ABE2",
                  "#F7931E",
                  "#FFFF00",
                  "#ED1C24",
                  "#009245",
                  "#662D91",
                  "#D4145A"
                )
              )
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
            if (locationAlertDialogViewMain.edt_name.text.isNotEmpty()) {
              locationAlertDialogViewMain.buttonTextColor.isEnabled = false
              locationAlertDialogViewMain.buttonBgColor.isEnabled = false
              locationAlertDialogViewMain.edt_name.isEnabled = false
              locationAlertDialogViewMain.order.isEnabled = false
              locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
              val d = SlidingTextItem()
              d.enabled = true
              d.textTitle = locationAlertDialogViewMain.edt_name.text.toString()
              d.textDescription = ""
              d.timeBased = false
              d.backgroundColorHex = bgColor
              d.textColorHex = textColor
              d.order = if (locationAlertDialogViewMain.order.text.isEmpty()) {
                0
              } else {
                locationAlertDialogViewMain.order.text.toString().toLong()
              }
              LiveDataUtil.observeOnce(noticeViewModel.createItem(d)) { sItem ->
                if (sItem.id != null) {
                  normalBannerPopUpArrayList.add(sItem)
                  normalBannersPopUpAdapter.notifyItemInserted(normalBannerPopUpArrayList.size - 1)
                  alertDialog.dismiss()
                }
              }
            }
          }
          alertDialog.setView(locationAlertDialogViewMain)
          alertDialog.show()
        }
        addTimeBasedNotifications.setOnClickListener {
          val alertDialog = AlertDialog.Builder(this).create()
          val locationAlertDialogViewMain = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_time_based_banner, null)
          var textColor = "#ffffff"
          var bgColor = "#43A047"
          locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
            // Kotlin Code
            MaterialColorPickerDialog
              .Builder(this)          // Pass Activity Instance
              .setTitle("Pick Text Color")
              .setColors(
                arrayListOf(
                  "#FFFFFF",
                  "#000000",
                  "#3D3D3D",
                  "#29ABE2",
                  "#F7931E",
                  "#FFFF00",
                  "#ED1C24",
                  "#009245",
                  "#662D91",
                  "#D4145A"
                )
              )
              .setColorListener { color, colorHex ->
                textColor = colorHex
                locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
              }
              .show()
          }
          locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
            MaterialColorPickerDialog
              .Builder(this)          // Pass Activity Instance
              .setTitle("Pick Text Color")
              .setColors(
                arrayListOf(
                  "#FFFFFF",
                  "#000000",
                  "#3D3D3D",
                  "#29ABE2",
                  "#F7931E",
                  "#FFFF00",
                  "#ED1C24",
                  "#009245",
                  "#662D91",
                  "#D4145A"
                )
              )
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
            if (locationAlertDialogViewMain.edt_name.text.isNotEmpty() &&
              locationAlertDialogViewMain.edt_start_time.text.isNotEmpty() &&
              locationAlertDialogViewMain.edt_end_time.text.isNotEmpty()
            ) {
              locationAlertDialogViewMain.buttonTextColor.isEnabled = false
              locationAlertDialogViewMain.buttonBgColor.isEnabled = false
              locationAlertDialogViewMain.edt_name.isEnabled = false
              locationAlertDialogViewMain.edt_start_time.isEnabled = false
              locationAlertDialogViewMain.edt_end_time.isEnabled = false
              locationAlertDialogViewMain.order.isEnabled = false
              locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
              val d = SlidingTextItem()
              d.enabled = true
              d.textTitle = locationAlertDialogViewMain.edt_name.text.toString()
              d.startTimeString = locationAlertDialogViewMain.edt_start_time.text.toString()
              d.endTimeString = locationAlertDialogViewMain.edt_end_time.text.toString()
              d.textDescription = ""
              d.timeBased = true
              d.backgroundColorHex = bgColor
              d.textColorHex = textColor
              d.order = if (locationAlertDialogViewMain.order.text.isEmpty()) {
                0
              } else {
                locationAlertDialogViewMain.order.text.toString().toLong()
              }
              LiveDataUtil.observeOnce(noticeViewModel.createItem(d)) { sItem ->
                if (sItem.id != null) {
                  timeBasedBannerPopUpArrayList.add(sItem)
                  slidingItemAdapter.notifyItemInserted(timeBasedBannerPopUpArrayList.size - 1)
                  alertDialog.dismiss()
                }
              }
            }
          }
          alertDialog.setView(locationAlertDialogViewMain)
          alertDialog.show()
        }
      }
    }
  }

  private fun initNormalDeliveryChargesLogic() {
    LiveDataUtil.observeOnce(locationViewModel.getAllItems()) {
      if (it.error == true) {
        showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        normalLocationsItemsArrayList.clear()
        normalLocationsItemsArrayList.addAll(it.results)
        deliveryChargeRecyclerView.layoutManager = LinearLayoutManager(this)
        normalLocationsItemsRecyclerAdapter =
          LocationItemRecyclerAdapter(
            this@SettingActivity,
            normalLocationsItemsArrayList, "delivery_charges", locationViewModel
          )
        deliveryChargeRecyclerView.adapter = normalLocationsItemsRecyclerAdapter
        addNormalDeliveryCharge.setOnClickListener {
          val alertDialog = AlertDialog.Builder(this).create()
          val locationAlertDialogViewMain = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_location, null)
          locationAlertDialogViewMain.edt_client_delivery_charge_container.visibility = View.VISIBLE
          locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
            val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
            val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
            val clientDeliveryCharge =
              locationAlertDialogViewMain.edt_client_delivery_charge.text.toString()
            val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
            if (locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
              && daCharge.isNotEmpty() && clientDeliveryCharge.isNotEmpty()
            ) {
              locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
              locationAlertDialogViewMain.edt_location_name.isEnabled = false
              locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
              locationAlertDialogViewMain.edt_da_charge.isEnabled = false
              locationAlertDialogViewMain.edt_client_delivery_charge.isEnabled = false
              alertDialog.setCancelable(false)
              alertDialog.setCanceledOnTouchOutside(false)
              val locationItem = Location()
              locationItem.locationName = locationName
              locationItem.deliveryCharge = deliveryCharge.toInt()
              locationItem.daCharge = daCharge.toInt()
              locationItem.deliveryChargeClient = clientDeliveryCharge.toInt()
              LiveDataUtil.observeOnce(locationViewModel.createItem(locationItem)) { itLoc ->
                if (itLoc.id != null) {
                  normalLocationsItemsArrayList.add(
                    Location(
                      id = itLoc.id!!,
                      locationName = locationName,
                      deliveryCharge = deliveryCharge.toInt(),
                      deliveryChargeClient = clientDeliveryCharge.toInt(),
                      daCharge = daCharge.toInt()
                    )
                  )
                  normalLocationsItemsRecyclerAdapter.notifyItemInserted(
                    normalLocationsItemsArrayList.size - 1
                  )
                  alertDialog.dismiss()
                }
              }
            }
          }
          alertDialog.setView(locationAlertDialogViewMain)
          alertDialog.show()
        }
      }
    }
  }

  private fun initOrderDataLimitTimeLogic() {
    saveOrderTimeButton.isEnabled = false
    startTimeOrder.isEnabled = false
    endTimeOrder.isEnabled = false
    allowOverTimeCheckBox.isEnabled = false
    LiveDataUtil.observeOnce(settingViewModel.getSettings(Constants.SETTING_ID)) {
      if (it.id != null) {
        orderStartTimeLimitString = it.orderStartTime!!
        orderEndTimeLimitString = it.orderEndTime!!
        orderAllowOverTimeOrder = it.orderOverTimeAllowed == true
        placeDataOnEdittextsOrderLimitsCheckbox()
        initListenersForOrderLimitEdittextsAndCheckBoxes()
        initCustomOrderMaxLimitTimeLogic(it)
        initShopPerMaxOrderDataLimitTimeLogic(it)
        initDialogEmergencyMainLogic(it)
      } else {
        showToast(it.message.toString(), FancyToast.ERROR)
      }
    }
  }

  private fun placeDataOnEdittextsOrderLimitsCheckbox() {
    startTimeOrder.setText(orderStartTimeLimitString)
    endTimeOrder.setText(orderEndTimeLimitString)
    allowOverTimeCheckBox.isChecked = orderAllowOverTimeOrder
    startTimeOrder.isEnabled = true
    endTimeOrder.isEnabled = true
    allowOverTimeCheckBox.isEnabled = true
  }

  private fun initListenersForOrderLimitEdittextsAndCheckBoxes() {
    startTimeOrder.setOnClickListener {
      var timePicker = TimePickerDialog.newInstance(
        { view, hourOfDay, minute, second ->
          startTimeOrder.setText("$hourOfDay:$minute")
        }, false
      )
      timePicker.show(supportFragmentManager, "Start_Time")
    }
    endTimeOrder.setOnClickListener {
      var timePicker = TimePickerDialog.newInstance(
        { view, hourOfDay, minute, second ->
          endTimeOrder.setText("$hourOfDay:$minute")
        }, false
      )
      timePicker.show(supportFragmentManager, "End_Time")
    }
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
    saveOrderTimeButton.isEnabled = startTimeOrder.text.toString() != orderStartTimeLimitString ||
            endTimeOrder.text.toString() != orderEndTimeLimitString ||
            allowOverTimeCheckBox.isChecked != orderAllowOverTimeOrder

    saveOrderTimeButton.setOnClickListener {
      saveOrderTimeButton.isEnabled = false
      startTimeOrder.isEnabled = false
      endTimeOrder.isEnabled = false
      allowOverTimeCheckBox.isEnabled = false
      val hashMap = HashMap<String, Any>()
      hashMap["orderStartTime"] = startTimeOrder.text.toString()
      hashMap["orderEndTime"] = endTimeOrder.text.toString()
      hashMap["orderOverTimeAllowed"] = allowOverTimeCheckBox.isChecked
      LiveDataUtil.observeOnce(settingViewModel.updateItem(Constants.SETTING_ID, hashMap)) {
        startTimeOrder.isEnabled = true
        endTimeOrder.isEnabled = true
        allowOverTimeCheckBox.isEnabled = true
        if (it.id != null) {
          orderStartTimeLimitString = startTimeOrder.text.toString()
          orderEndTimeLimitString = endTimeOrder.text.toString()
          orderAllowOverTimeOrder = allowOverTimeCheckBox.isChecked
        } else {
          saveOrderTimeButton.isEnabled = true
        }
      }
    }
  }

  private fun initCustomOrderMaxLimitTimeLogic(setting: Setting) {
    saveCustomCategoryOrderLimitsButton.isEnabled = false
    customCategoryMaxOrderLimitEdittext.isEnabled = false
    medicineCategoryMaxOrderLimitEdittext.isEnabled = false
    parcelCategoryMaxOrderLimitEdittext.isEnabled = false
    totalCategoryMaxOrderLimitEdittext.isEnabled = false
    parcelOrdersMaxTimeLimitString = setting.parcelMaxOrders!!
    customOrdersMaxTimeLimitString = setting.customMaxOrders!!
    medicineOrdersMaxTimeLimitString = setting.medicineMaxOrders!!
    totalCustomOrdersMaxTimeLimitString = setting.totalCustomMaxOrders!!
    placeDataOnEdittextsMaxCustomOrderLimitsCheckbox()
    initListenersForOrderMaxCustomLimitEdittextsAndCheckBoxes()
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
      customCategoryMaxOrderLimitEdittext.text.toString() != customOrdersMaxTimeLimitString.toString() ||
              medicineCategoryMaxOrderLimitEdittext.text.toString() != medicineOrdersMaxTimeLimitString.toString() ||
              parcelCategoryMaxOrderLimitEdittext.text.toString() != parcelOrdersMaxTimeLimitString.toString() ||
              totalCategoryMaxOrderLimitEdittext.text.toString() != totalCustomOrdersMaxTimeLimitString.toString()

    saveCustomCategoryOrderLimitsButton.setOnClickListener {
      saveCustomCategoryOrderLimitsButton.isEnabled = false
      customCategoryMaxOrderLimitEdittext.isEnabled = false
      medicineCategoryMaxOrderLimitEdittext.isEnabled = false
      parcelCategoryMaxOrderLimitEdittext.isEnabled = false
      totalCategoryMaxOrderLimitEdittext.isEnabled = false
      val hashMap = HashMap<String, Any>()
      hashMap["parcelMaxOrders"] = parcelCategoryMaxOrderLimitEdittext.text.toString()
      hashMap["customMaxOrders"] = customCategoryMaxOrderLimitEdittext.text.toString()
      hashMap["medicineMaxOrders"] = medicineCategoryMaxOrderLimitEdittext.text.toString()
      hashMap["totalCustomMaxOrders"] = totalCategoryMaxOrderLimitEdittext.text.toString()
      LiveDataUtil.observeOnce(settingViewModel.updateItem(Constants.SETTING_ID, hashMap)) {
        customCategoryMaxOrderLimitEdittext.isEnabled = true
        medicineCategoryMaxOrderLimitEdittext.isEnabled = true
        parcelCategoryMaxOrderLimitEdittext.isEnabled = true
        totalCategoryMaxOrderLimitEdittext.isEnabled = true
        if (it.id != null) {
          customOrdersMaxTimeLimitString =
            customCategoryMaxOrderLimitEdittext.text.toString().toInt()
          medicineOrdersMaxTimeLimitString =
            medicineCategoryMaxOrderLimitEdittext.text.toString().toInt()
          parcelOrdersMaxTimeLimitString =
            parcelCategoryMaxOrderLimitEdittext.text.toString().toInt()
          totalCustomOrdersMaxTimeLimitString =
            totalCategoryMaxOrderLimitEdittext.text.toString().toInt()
        } else {
          saveCustomCategoryOrderLimitsButton.isEnabled = true
        }
      }
    }
  }

  private fun initShopPerMaxOrderDataLimitTimeLogic(setting: Setting) {
    saveShopOrderExtraLimitButton.isEnabled = false
    maxOrderFromEachShopEdittext.isEnabled = false
    extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
    extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
    allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
    maxShopPerOrderInt = setting.maxShopPerOrder!!
    maxChargeAfterPershopMaxOrder = setting.maxChargeAfterPershopMaxOrder!!
    maxDaChargeAfterPershopMaxOrder = setting.maxDaChargeAfterPershopMaxOrder!!
    allowOrderingMoreThanMaxShops = setting.allowOrderingMoreThanMaxShops!!
    placeDataOnEdittextsPerShopMaxOrderLimitsCheckbox()
    initListenersForPerShopMaxOrderLimitEdittextsAndCheckBoxes()
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
    saveShopOrderExtraLimitButton.isEnabled =
      maxOrderFromEachShopEdittext.text.toString() != maxShopPerOrderInt.toString() ||
              extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString() != maxChargeAfterPershopMaxOrder.toString() ||
              extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString() != maxDaChargeAfterPershopMaxOrder.toString() ||
              allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked != allowOrderingMoreThanMaxShops

    saveShopOrderExtraLimitButton.setOnClickListener {
      saveShopOrderExtraLimitButton.isEnabled = false
      maxOrderFromEachShopEdittext.isEnabled = false
      extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
      extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = false
      allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = false
      val hashMap = HashMap<String, Any>()
      hashMap["maxShopPerOrder"] = maxOrderFromEachShopEdittext.text.toString().toInt()
      hashMap["maxChargeAfterPershopMaxOrder"] =
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
      hashMap["maxDaChargeAfterPershopMaxOrder"] =
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
      hashMap["allowOrderingMoreThanMaxShops"] = allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked

      LiveDataUtil.observeOnce(settingViewModel.updateItem(Constants.SETTING_ID, hashMap)) {
        maxOrderFromEachShopEdittext.isEnabled = true
        extraChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
        extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.isEnabled = true
        allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isEnabled = true
        if (it.id != null) {
          maxShopPerOrderInt = maxOrderFromEachShopEdittext.text.toString().toInt()
          maxChargeAfterPershopMaxOrder =
            extraChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
          maxDaChargeAfterPershopMaxOrder =
            extraDaChargeAfterCrossingMaxOrderFromEachShopEdittext.text.toString().toInt()
          allowOrderingMoreThanMaxShops =
            allowExtraOrderAfterCrossingMaxFromEachShopCheckBox.isChecked
        } else {
          saveShopOrderExtraLimitButton.isEnabled = true
        }
      }
    }
  }

  private fun initDialogEmergencyMainLogic(setting: Setting) {
    emergencyModeDialogSaveButton.isEnabled = false
    emergencyModeDialogTitleEditText.isEnabled = false
    emergencyModeDialogMessageEdtittext.isEnabled = false
    emergencyModeDialogActivityCheckBox.isEnabled = false
    alertDialogEmergencyTitleText = setting.alertDialogEmergencyTitleText!!
    alertDialogEmergencyMessageText = setting.alertDialogEmergencyMessageText!!
    alertDialogeEmergencyStatus = setting.alertDialogeEmergencyStatus!!
    placeDataOnEdittextsDialogEdittextEmergencyCheckbox()
    initListenersForDialogEmegencyEdittextsAndCheckBoxes()
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
      emergencyModeDialogTitleEditText.text.toString() != alertDialogEmergencyTitleText ||
              emergencyModeDialogMessageEdtittext.text.toString() != alertDialogEmergencyMessageText ||
              emergencyModeDialogActivityCheckBox.isChecked != (alertDialogeEmergencyStatus == "active")

    emergencyModeDialogSaveButton.setOnClickListener {
      emergencyModeDialogSaveButton.isEnabled = false
      emergencyModeDialogActivityCheckBox.isEnabled = false
      emergencyModeDialogMessageEdtittext.isEnabled = false
      emergencyModeDialogTitleEditText.isEnabled = false
      val hashMap = HashMap<String, Any>()
      hashMap["alertDialogEmergencyTitleText"] = emergencyModeDialogTitleEditText.text.toString()
      hashMap["alertDialogEmergencyMessageText"] = emergencyModeDialogMessageEdtittext.text.toString()
      hashMap["alertDialogeEmergencyStatus"] = if (emergencyModeDialogActivityCheckBox.isChecked) {
        "active"
      } else {
        "openapp"
      }
      LiveDataUtil.observeOnce(settingViewModel.updateItem(Constants.SETTING_ID, hashMap)) {
        emergencyModeDialogMessageEdtittext.isEnabled = true
        emergencyModeDialogTitleEditText.isEnabled = true
        emergencyModeDialogActivityCheckBox.isEnabled = true
        if (it.id != null) {
          alertDialogEmergencyTitleText = startTimeOrder.text.toString()
          alertDialogEmergencyMessageText = endTimeOrder.text.toString()
          alertDialogeEmergencyStatus = if (emergencyModeDialogActivityCheckBox.isChecked) {
            "active"
          } else {
            "openapp"
          }
        } else {
          emergencyModeDialogSaveButton.isEnabled = true
        }
      }
    }
  }
}