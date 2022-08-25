package admin.arpan.delivery.ui.order

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.models.enums.OrderStatus
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.*
import admin.arpan.delivery.utils.networking.requests.SendNotificationRequest
import admin.arpan.delivery.viewModels.DAViewModel
import admin.arpan.delivery.viewModels.NotificationViewModel
import admin.arpan.delivery.viewModels.OrderViewModel
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.baoyachi.stepview.HorizontalStepView
import com.baoyachi.stepview.bean.StepBean
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.*
import kotlinx.android.synthetic.main.dialog_alert_layout_main.view.*
import kotlinx.android.synthetic.main.dialog_ask_cancellation_reason.view.*
import kotlinx.android.synthetic.main.dialog_ask_password.view.*
import kotlinx.android.synthetic.main.dialog_ask_password.view.deleteOrderItemMainDialogButton
import kotlinx.android.synthetic.main.dialog_ask_password.view.edt_enter_password_field
import kotlinx.android.synthetic.main.dialog_ask_password.view.edt_enter_password_field_container
import kotlinx.android.synthetic.main.dialog_force_change_order_status.view.*
import kotlinx.android.synthetic.main.edit_order_item.view.*
import kotlinx.android.synthetic.main.fragment_add_custom_order.view.*
import kotlinx.android.synthetic.main.fragment_edit_order.view.*
import kotlinx.android.synthetic.main.fragment_order_history_new.view.*
import kotlinx.android.synthetic.main.fragment_order_history_new.view.mainOrderDetailsDataContainerLinearLayout
import kotlinx.android.synthetic.main.fragment_order_history_new.view.noDataFoundLinearLayoutContainer
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderArpanChargePrice
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderDaPrice
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderDeliveryPrice
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderHistoryProgressBarContainer
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderIdTextView
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderImagePicture
import kotlinx.android.synthetic.main.fragment_order_history_new.view.orderTotalPrice
import kotlinx.android.synthetic.main.fragment_order_history_new.view.title_text_view
import kotlinx.android.synthetic.main.product_image_big_view.view.*
import kotlinx.android.synthetic.main.product_image_big_view.view.imageView
import java.io.IOException
import java.lang.ClassCastException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

@AndroidEntryPoint
class OrderHistoryFragment : Fragment() {
  private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
  private lateinit var productRecyclerViewAdapter: OrderProductItemRecyclerAdapter
  private lateinit var progressDialog: Dialog
  private val TAG = "OdrHisFgmntNw2"
  private var orderId = ""
  private var customerId = ""
  private var orderItemMain: OrderItemMain? = null
  private var currentCalc = 0
  lateinit var contextMain: Context
  private lateinit var homeViewModelMainData: HomeViewModelMainData
  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val orderViewModel: OrderViewModel by viewModels()
  private val daViewModel: DAViewModel by viewModels()
  private val notificationViewModel: NotificationViewModel by viewModels()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    contextMain = context
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_order_history_new, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initVars(view)
  }

  private fun initVars(view: View) {
    progressDialog = contextMain.createProgressDialog()
    productRecyclerViewAdapter = OrderProductItemRecyclerAdapter(contextMain, mainShopItemHashMap)
    homeViewModelMainData =
      ViewModelProvider(requireActivity()).get(HomeViewModelMainData::class.java)
    orderId = arguments?.getString("orderID").toString()
    customerId = arguments?.getString("customerId").toString()
    view.title_text_view.setOnClickListener {
      homeMainNewInterface.callOnBackPressed()
    }
    fetchOrderData(orderId, view)
  }

  private fun fetchOrderData(orderID: String, view: View) {
    progressDialog.show()
    LiveDataUtil.observeOnce(orderViewModel.getItemById(orderID)) {
      progressDialog.dismiss()
      if (it.error == true) {
        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.VISIBLE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.GONE
      } else {
        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.GONE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
        workWithTheDocumentData(view, it)
      }
    }
  }

  private fun workWithTheDocumentData(view: View, orderItem: OrderItemMain) {
    // Here we have the snapshot of the "OrderItemMain" and can freely work with it
    // null check and empty check is already done in the previous stage
    orderItemMain = orderItem// Parsing to model class

    view.orderHistoryProgressBarContainer.visibility = View.GONE
    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE

    initDefaultViewStates(view)
    setTextOnTextViewsOnMainUi(view, orderItemMain!!)
    setLogicForOrderStatusOnThirdRow(view, orderItemMain!!)
    setTextOnRestOrderDetailsTextView(view, orderItemMain!!)
    setLogicForPlacingOrderItemDetailsAndSeparatingProducts(view, orderItemMain!!)
    setLogicForCardsAndCountsOnStatistics(view, orderItemMain!!)

    view.orderHistoryProgressBarContainer.visibility = View.GONE
    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
  }

  //    private fun setLogicForCardsAndCountsOnStatistics(view: View, orderItemMain: OrderItemMain) {
//        view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
//        view.orderDeliveryPrice.text = orderItemMain.deliveryCharge.toString()
//        view.orderDaPrice.text = orderItemMain.daCharge.toString()
//        var arpanProfit = orderItemMain.deliveryCharge - orderItemMain.daCharge
//        for(productItem in orderItemMain.products){
//            arpanProfit += (productItem.product_arpan_profit*productItem.product_item_amount)
//        }
//        if(orderItemMain.promoCodeApplied){
//            var totalcalculated = 0
//            for(productItem in orderItemMain.products){
//                totalcalculated += (productItem.product_item_price*productItem.product_item_amount)
//            }
//            arpanProfit -= (totalcalculated-orderItemMain.totalPrice)
//        }
//        view.orderArpanChargePrice.text = arpanProfit.toString()
//    }
  private fun setLogicForCardsAndCountsOnStatistics(view: View, orderItemMain: OrderItemMain) {
    view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
    view.orderDeliveryPrice.text = orderItemMain.deliveryCharge.toString()
    view.orderDaPrice.text = orderItemMain.daCharge.toString()

    //Order Was Successfully completed so we can calculate this value
    if (orderItemMain.products.size == 1 && !orderItemMain.products.any { it.product_item }) {
      // This is  a custom order so it'll have separate calculation

      // The amount that the customer gives to the delivery agent
      val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

      // The amount the delivery agent pays to the shop/store before collecting the product
      val shopAmount = orderItemMain.totalPrice

      var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

      if (orderItemMain.paymentMethod == "bKash") {
        val bkashChargeExtra = roundNumberPriceTotal(
          (orderItemMain.totalPrice + orderItemMain.deliveryCharge)
                  * CalculationLogics().getBkashChargePercentage()
        ).toInt()
        deliveryAgentsDueToArpan -= bkashChargeExtra
      }


      view.orderArpanChargePrice.text = deliveryAgentsDueToArpan.toString()
    } else {
      // This is a shop order so this will also have another type of calculation

      // The amount that the customer gives to the delivery agent
      val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

      // The amount the delivery agent pays to the shop/store before collecting the product
      val shopAmount = calculateShopAmount(orderItemMain.products)

      var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

      if (orderItemMain.paymentMethod == "bKash") {
        val bkashChargeExtra = roundNumberPriceTotal(
          (orderItemMain.totalPrice + orderItemMain.deliveryCharge)
                  * CalculationLogics().getBkashChargePercentage()
        ).toInt()
        deliveryAgentsDueToArpan -= bkashChargeExtra
      }

      view.orderArpanChargePrice.text = deliveryAgentsDueToArpan.toString()
    }
  }

  private fun calculateShopAmount(products: List<CartProductEntity>): Int {
    var shopAmount = 0
    for (product in products) {
      shopAmount += (product.product_item_price - product.product_arpan_profit) * product.product_item_amount
    }
    return shopAmount
  }

  private fun roundNumberPriceTotal(d: Float): Int {
    //This  is a special round function exclusively for this  page of the app
    //not usable for general parts and other parts of   the code or apps
    return if (d > d.toInt()) {
      d.toInt() + 1
    } else {
      d.roundToInt()
    }
  }

  private fun initDefaultViewStates(view: View) {
    view.orderImagePicture.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderNoteEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.GONE
    view.appliedPromoCodeButton.visibility = View.GONE
    view.cancelOrderButton.visibility = View.GONE
    view.orderAdminNoteEdittextContainerLayout.visibility = View.GONE
    view.acceptOrderButton.visibility = View.GONE
    view.linearLayoutCancelReasonContainer.visibility = View.GONE
    view.linearAssignedDA.visibility = View.GONE
    view.recieverMobileNumberContainer.visibility = View.GONE
  }

  private fun setLogicForPlacingOrderItemDetailsAndSeparatingProducts(
    view: View,
    orderItemMain: OrderItemMain
  ) {
    if (orderItemMain.pickDropOrder) {
      initPickDropDataLogicAndPlaceDataOnUi(view, orderItemMain)
    } else {
      if (orderItemMain.products.size == 1 && !orderItemMain.products[0].product_item) {
        when {
          orderItemMain.products[0].parcel_item -> {
            placeParcelItemData(view, orderItemMain)
          }
          orderItemMain.products[0].medicine_item -> {
            placeMedicineItemData(view, orderItemMain)
          }
          orderItemMain.products[0].custom_order_item -> {
            placeCustomOrderItemData(view, orderItemMain)
          }
        }
      } else {
        workWithTheArrayList(orderItemMain.products, view)
      }
    }
  }

  private fun placeCustomOrderItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.GONE

    if (orderItemMain.products[0].custom_order_text.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].custom_order_text)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].custom_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].custom_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun placeMedicineItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderItemsMainRecyclerView.visibility = View.GONE
    if (orderItemMain.products[0].medicine_order_text.trim().isNotEmpty()) {
      view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
      view.orderTitleEdittextContainerLayout.hint = "ফার্মেসি"
      view.orderTitleEdittext.setText(orderItemMain.products[0].medicine_order_text)
    } else {
      view.orderTitleEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].medicine_order_text_2.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ঔষধ"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].medicine_order_text_2)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].medicine_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].medicine_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun placeParcelItemData(view: View, orderItemMain: OrderItemMain) {
    view.orderItemsMainRecyclerView.visibility = View.GONE

    if (orderItemMain.products[0].parcel_order_text.trim().isNotEmpty()) {
      view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
      view.orderTitleEdittextContainerLayout.hint = "কুরিয়ার নেম"
      view.orderTitleEdittext.setText(orderItemMain.products[0].parcel_order_text)
    } else {
      view.orderTitleEdittextContainerLayout.visibility = View.GONE
    }
    if (orderItemMain.products[0].parcel_order_text_2.trim().isNotEmpty()) {
      view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
      view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
      view.orderDetailsEdittext.setText(orderItemMain.products[0].parcel_order_text_2)
    } else {
      view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    }

    if (orderItemMain.products[0].parcel_order_image.isNotEmpty()) {
      view.orderImagePicture.visibility = View.VISIBLE
      setOrderImageOnView(view, orderItemMain.products[0].parcel_order_image)
    } else {
      view.orderImagePicture.visibility = View.GONE
    }
  }

  private fun initPickDropDataLogicAndPlaceDataOnUi(view: View, orderItemMain: OrderItemMain) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
    view.orderItemsMainRecyclerView.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.hint = "পিক-আপ এন্ড ড্রপ"
    view.callUserNowReciverImageButton.setOnClickListener {
      callNow(orderItemMain.pickDropOrderItem!!.senderPhone)
    }
    view.userNameEditText.setText(orderItemMain.pickDropOrderItem!!.senderName)
    view.userMobileEdittext.setText(orderItemMain.pickDropOrderItem!!.senderPhone)
    view.userLocationEdittext.setText(orderItemMain.pickDropOrderItem!!.senderName)
    if (orderItemMain.pickDropOrderItem != null) {
      if (orderItemMain.pickDropOrderItem!!.senderAddress.trim().isNotEmpty()) {
        view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
        view.userAddressEdittext.setText(orderItemMain.pickDropOrderItem!!.senderAddress)
      } else {
        view.userAddressEdittextContainerLayout.visibility = View.GONE
      }
    }
    view.orderDetailsEdittext.setText(
      StringBuilder()
        .append("প্রেরকের তথ্যঃ ")
        .append("\n")
        .append("নামঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderName)
        .append("\n")
        .append("মোবাইলঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderPhone)
        .append("\n")
        .append("ঠিকানাঃ ")
        .append(orderItemMain.pickDropOrderItem!!.senderAddress)
        .append("\n")
        .append("লোকেশনঃ")
        .append(orderItemMain.pickDropOrderItem!!.senderLocation)
        .append("\n")
        .append("\n")
        .append("প্রাপকের তথ্যঃ ")
        .append("\n")
        .append("নামঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverName)
        .append("\n")
        .append("মোবাইলঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverPhone)
        .append("\n")
        .append("ঠিকানাঃ ")
        .append(orderItemMain.pickDropOrderItem!!.recieverAddress)
        .append("\n")
        .append("লোকেশনঃ")
        .append(orderItemMain.pickDropOrderItem!!.recieverLocation)
        .append("\n")
        .append("\n")
        .append("পার্সেলের তথ্যঃ ")
        .append("\n")
        .append(orderItemMain.pickDropOrderItem!!.parcelDetails)
    )
    if (orderItemMain.pickDropOrderItem != null) {
      if (orderItemMain.pickDropOrderItem!!.recieverPhone.isNotEmpty()) {
        view.recieverMobileNumberContainer.visibility = View.VISIBLE
        view.userReciverMobileEdittext.setText(orderItemMain.pickDropOrderItem!!.recieverPhone)
        view.callUserNowReciverImageButton.setOnClickListener {
          callNow(orderItemMain.pickDropOrderItem!!.recieverPhone)
        }
      } else {
        view.recieverMobileNumberContainer.visibility = View.GONE
      }
      if (orderItemMain.pickDropOrderItem!!.parcelImage.isNotEmpty()) {
        view.orderImagePicture.visibility = View.VISIBLE
        setOrderImageOnView(view, orderItemMain.pickDropOrderItem!!.parcelImage)
      } else {
        view.orderImagePicture.visibility = View.GONE
      }
    }

  }

  private fun callNow(recieverPhone: String) {
    if (callPermissionCheck(contextMain, contextMain as Activity)) {
      val callIntent = Intent(
        Intent.ACTION_CALL,
        Uri.parse("tel:" + recieverPhone)
      )
      startActivity(callIntent)
    }
  }

  private fun setOrderImageOnView(view: View, path: String) {
    Glide.with(requireActivity())
      .load(Constants.SERVER_FILES_BASE_URL + path)
      .diskCacheStrategy(DiskCacheStrategy.ALL)
      .centerInside()
      .placeholder(R.drawable.loading_image_glide)
      .into(view.orderImagePicture)

    view.orderImagePicture.setOnClickListener {
      val dialog = AlertDialog.Builder(contextMain, R.style.Theme_AdminArpan).create()
      val view2 = LayoutInflater.from(contextMain).inflate(R.layout.product_image_big_view, null)
      view2.floatingActionButton.setOnClickListener {
        dialog.dismiss()
      }
      Glide.with(contextMain)
        .load(Constants.SERVER_FILES_BASE_URL + path)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerInside()
        .into(view2.imageView)
      dialog.setView(view2)
      //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      dialog.show()
    }
  }

  private fun workWithTheArrayList(products: List<CartProductEntity>, view: View) {
    view.orderTitleEdittextContainerLayout.visibility = View.GONE
    view.orderDetailsEdittextContainerLayout.visibility = View.GONE
    view.orderItemsMainRecyclerView.visibility = View.VISIBLE

    mainShopItemHashMap.clear()
    for (cartItemEntity in products) {
      val filteredArray =
        mainShopItemHashMap.filter { it -> it.shop_doc_id == cartItemEntity.product_item_shop_key }
      if (filteredArray.isEmpty()) {
        val shopItem = MainShopCartItem()
        shopItem.shop_doc_id = cartItemEntity.product_item_shop_key
        shopItem.shop_details = Shop(name = cartItemEntity.product_item_shop_name)
        shopItem.cart_products.add(cartItemEntity)
        mainShopItemHashMap.add(shopItem)
      } else {
        mainShopItemHashMap[mainShopItemHashMap.indexOf(filteredArray[0])]
          .cart_products.add(cartItemEntity)
      }
    }
    if (mainShopItemHashMap.isNotEmpty()) {
      // This has shop specific products in the order :D
      view.orderItemsMainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
      view.orderItemsMainRecyclerView.adapter = productRecyclerViewAdapter
    }
  }

  private fun setTextOnRestOrderDetailsTextView(view: View, orderItemMain: OrderItemMain) {
    view.pickUpTimeTV.text = orderItemMain.pickUpTime
    view.dateOrder.text = getDate(orderItemMain.orderPlacingTimeStamp, "dd/MM/yyyy")
    if (orderItemMain.promoCodeApplied) {
      view.appliedPromoCodeButton.visibility = View.VISIBLE
      view.appliedPromoCodeButton.text =
        "প্রোমোকোড অ্যাড করা হয়েছেঃ ${orderItemMain.promoCode!!.promoCodeName}"
    } else {
      view.appliedPromoCodeButton.visibility = View.GONE
    }
    if (orderItemMain.paymentMethod == "COD") {
      view.pricePaymentStatusMain.text = "COD"
    } else {
      view.pricePaymentStatusMain.text = "bKash"
    }
    view.totalPriceTextViewMain.text =
      "Total: ${orderItemMain.totalPrice}+${orderItemMain.deliveryCharge} = ${orderItemMain.totalPrice + orderItemMain.deliveryCharge}"
  }

  private fun setLogicForOrderStatusOnThirdRow(view: View, orderItemMain: OrderItemMain) {
    view.step_view_order_progress.visibility = View.VISIBLE
    view.orderStatusTopButton.text = orderItemMain.orderStatus
    view.cancelOrderButton.visibility = View.GONE
    when (orderItemMain.orderStatus) {
      "PENDING" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", -1)
        val stepBean1 = StepBean("     PROCESSING     ", -1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)
        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#FA831B"))

        view.cancelOrderButton.visibility = View.VISIBLE
        view.cancelOrderButton.text = "Cancel"
        view.cancelOrderButton.setOnClickListener {
          // CANCEL ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          cancelOrderItem(view, orderItemMain)
        }

        view.acceptOrderButton.text = "Verify Order"
        view.acceptOrderButton.visibility = View.VISIBLE
        view.acceptOrderButton.setOnClickListener {
          // ACCEPT ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          verifyUsersOrder(view, orderItemMain)
        }
      }
      "VERIFIED" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", -1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)
        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.cancelOrderButton.visibility = View.VISIBLE

        view.acceptOrderButton.text = "Verify Order"
        view.acceptOrderButton.visibility = View.VISIBLE

        if (!orderItemMain.daID.isNullOrEmpty()) {
          view.linearAssignedDA.visibility = View.VISIBLE
          view.assignedToDaTextView.text = "Waiting For " + orderItemMain.daDetails!!.name!!
          view.callDaNowImageView.setOnClickListener {
            callNow(orderItemMain.daDetails!!.phone!!)
          }
          view.acceptOrderButton.visibility = View.GONE
          view.cancelOrderButton.text = "Cancel Order"
          view.cancelOrderButton.setOnClickListener {
            cancelOrderItem(view, orderItemMain)
          }
          view.assignedToDaTextView.setOnClickListener {
            showAssignOrderToDaListDialog(view, orderItemMain)
          }
          view.assignedToDaTextView.setOnLongClickListener {
            removeDaFromThisOrder(view, orderItemMain)
            true
          }
        } else {
          view.linearAssignedDA.visibility = View.GONE
          view.acceptOrderButton.text = "Assign DA"
          view.acceptOrderButton.setOnClickListener {
            showAssignOrderToDaListDialog(view, orderItemMain)
          }
          view.cancelOrderButton.text = "Cancel Order"
          view.cancelOrderButton.setOnClickListener {
            cancelOrderItem(view, orderItemMain)
          }
        }
      }
      "PROCESSING" -> {
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", 1)
        val stepBean2 = StepBean("     PICKED UP     ", -1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.processingTimeStampMillis != 0L) {
          stepBean1.name =
            "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)

        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.acceptOrderButton.visibility = View.GONE

        view.cancelOrderButton.visibility = View.VISIBLE
        view.cancelOrderButton.text = "Force Cancel"
        view.cancelOrderButton.setOnClickListener {
          // CANCEL ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          cancelOrderItem(view, orderItemMain)
        }

        if (orderItemMain.daID!!.isNotEmpty()) {
          view.linearAssignedDA.visibility = View.VISIBLE
          view.assignedToDaTextView.text = "Assigned to " + orderItemMain.daDetails!!.name!!
          view.callDaNowImageView.setOnClickListener {
            callNow(orderItemMain.daDetails!!.phone!!)
          }
          view.acceptOrderButton.visibility = View.GONE
          view.cancelOrderButton.visibility = View.GONE
          view.assignedToDaTextView.setOnClickListener {
            showAssignOrderToDaListDialog(view, orderItemMain)
          }
          view.assignedToDaTextView.setOnLongClickListener {
            removeDaFromThisOrder(view, orderItemMain)
            true
          }
        }
      }
      "PICKED UP" -> {
        view.step_view_order_progress.visibility = View.VISIBLE
        val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
        val stepsBeanList: MutableList<StepBean> = ArrayList()
        val stepBean4 = StepBean("     PENDING     ", 1)
        val stepBean0 = StepBean("     VERIFIED     ", 1)
        val stepBean1 = StepBean("     PROCESSING     ", 1)
        val stepBean2 = StepBean("     PICKED UP     ", 1)
        val stepBean3 = StepBean("     COMPLETED     ", -1)
        if (orderItemMain.processingTimeStampMillis != 0L) {
          stepBean1.name =
            "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.verifiedTimeStampMillis != 0L) {
          stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
        }
        if (orderItemMain.orderPlacingTimeStamp != 0L) {
          stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
        }
        if (orderItemMain.pickedUpTimeStampMillis != 0L) {
          stepBean2.name = "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis, "hh:mm")})"
        }
        stepsBeanList.add(stepBean4)
        stepsBeanList.add(stepBean0)
        stepsBeanList.add(stepBean1)
        stepsBeanList.add(stepBean2)
        stepsBeanList.add(stepBean3)

        setStepView(view, setpview5, stepsBeanList)
        view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#ED9D34"))

        view.acceptOrderButton.visibility = View.GONE
        view.cancelOrderButton.visibility = View.VISIBLE
        view.cancelOrderButton.text = "Force Cancel"
        view.cancelOrderButton.setOnClickListener {
          // CANCEL ORDER FOR DA - TOTALLY A SEPARATE OPERATION
          cancelOrderItem(view, orderItemMain)
        }

        if (!orderItemMain.daID.isNullOrEmpty()) {
          view.linearAssignedDA.visibility = View.VISIBLE
          view.assignedToDaTextView.text = "Assigned to " + orderItemMain.daDetails!!.name!!
          view.callDaNowImageView.setOnClickListener {
            callNow(orderItemMain.daDetails!!.phone!!)
          }
          view.acceptOrderButton.visibility = View.GONE
          view.cancelOrderButton.visibility = View.GONE
          view.assignedToDaTextView.setOnClickListener {
            showAssignOrderToDaListDialog(view, orderItemMain)
          }
          view.assignedToDaTextView.setOnLongClickListener {
            removeDaFromThisOrder(view, orderItemMain)
            true
          }
        }
      }
      "COMPLETED" -> {
        if (!orderItemMain.daID.isNullOrEmpty()) {
          view.linearAssignedDA.visibility = View.VISIBLE
          if (orderItemMain.daDetails != null) {
            view.assignedToDaTextView.text =
              "Assigned to " + orderItemMain.daDetails!!.name.toString()
            view.callDaNowImageView.setOnClickListener {
              callNow(orderItemMain.daDetails!!.phone.toString())
            }
          }
          view.acceptOrderButton.visibility = View.GONE
          view.cancelOrderButton.visibility = View.GONE
        }
        if (orderItemMain.orderCompletedStatus == "CANCELLED") {
          view.step_view_order_progress.visibility = View.GONE
          view.orderStatusTopButton.text = "CANCELLED"
          view.cancelOrderButton.visibility = View.VISIBLE
          view.cancelOrderButton.text = "Delete"
          view.cancelOrderButton.setOnClickListener {
            showDeleteOrderCustomDialog(view, orderItemMain)
          }
          if (!orderItemMain.cancelledOrderReasonFromAdmin.isNullOrEmpty()) {
            view.linearLayoutCancelReasonContainer.visibility = View.VISIBLE
            view.orderCancellationReasonDetails.text = orderItemMain.cancelledOrderReasonFromAdmin
          } else {
            view.linearLayoutCancelReasonContainer.visibility = View.GONE
          }
          view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#EA594D"))
        } else {
          val setpview5 =
            view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
          val stepsBeanList: MutableList<StepBean> = ArrayList()
          val stepBean4 = StepBean("     PENDING     ", 1)
          val stepBean0 = StepBean("     VERIFIED     ", 1)
          val stepBean1 = StepBean("     PROCESSING     ", 1)
          val stepBean2 = StepBean("     PICKED UP     ", 1)
          val stepBean3 = StepBean("     COMPLETED     ", 1)
          if (orderItemMain.processingTimeStampMillis != 0L) {
            stepBean1.name =
              "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.verifiedTimeStampMillis != 0L) {
            stepBean0.name =
              "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.orderPlacingTimeStamp != 0L) {
            stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp, "hh:mm")})"
          }
          if (orderItemMain.pickedUpTimeStampMillis != 0L) {
            stepBean2.name =
              "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis, "hh:mm")})"
          }
          if (orderItemMain.completedTimeStampMillis != 0L) {
            stepBean3.name =
              "COMPLETED\n(${getDate(orderItemMain.completedTimeStampMillis, "hh:mm")})"
          }
          stepsBeanList.add(stepBean4)
          stepsBeanList.add(stepBean0)
          stepsBeanList.add(stepBean1)
          stepsBeanList.add(stepBean2)
          stepsBeanList.add(stepBean3)
          setStepView(view, setpview5, stepsBeanList)
          view.cancelOrderButton.visibility = View.VISIBLE
          view.cancelOrderButton.text = "Force Cancel"
          view.cancelOrderButton.setOnClickListener {
            cancelOrderItem(view, orderItemMain)
          }
          view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#43A047"))
        }
      }
    }
  }

  private fun showAssignOrderToDaListDialog(view: View, orderItemMain: OrderItemMain) {
    val alertDialogForDa = AlertDialog.Builder(context).create()
    val alertDialogForDaView =
      LayoutInflater.from(context).inflate(R.layout.assign_da_list_view, null)
    progressDialog.show()
    LiveDataUtil.observeOnce(daViewModel.getActiveDas()) {
      progressDialog.dismiss()
      if (it.error == true) {
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        val arrayListDaStatus = it.results
        val arrayListDaStatusString = ArrayList<String>()
        for (daStatus in arrayListDaStatus) {
          var daListItem = ""
          if (daStatus.activeNow == true) {
            daListItem += "🟢 " + daStatus.name + " "
          } else {
            daListItem += "🔴 " + daStatus.name + " "
          }
          var daCompleted = 0
          var daProcessing = 0
          var daPickedUp = 0
          var daAssigned = 0
          for (order in homeViewModelMainData.getOrdersOneDayDataMainList().value!!) {
            if (order.daID == daStatus.id!!) {
              when (order.orderStatus) {
                "VERIFIED" -> daAssigned += 1
                "PROCESSING" -> daProcessing += 1
                "PICKED UP" -> daProcessing += 1
                "COMPLETED" -> daCompleted += 1
              }
            }
          }
          if (daStatus.daStatusTitle == null) {
            daListItem += "[$daProcessing] [$daCompleted]"
          } else {
            daListItem += "[$daProcessing] [$daCompleted] \n ${daStatus.daStatusTitle}"
          }
          arrayListDaStatusString.add(daListItem)
        }
        alertDialogForDaView.listView.adapter =
          ArrayAdapter(requireContext(), R.layout.custom_spinner_item_view, arrayListDaStatusString)
        alertDialogForDaView.listView.setOnItemClickListener { _, _, position, _ ->
          progressDialog.show()
          val updateDaDetails = HashMap<String, Any>()
          updateDaDetails["daDetails"] = User(
            id = arrayListDaStatus[position].id,
            name = arrayListDaStatus[position].name,
            phone = arrayListDaStatus[position].phone,
          )
          updateDaDetails["daID"] = arrayListDaStatus[position].id!!
          updateDaDetails["assignedToDaTimeStampMillis"] = System.currentTimeMillis()
          LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, updateDaDetails)) { itOrder ->
            progressDialog.dismiss()
            if (itOrder.id != null) {
              sendNotificationToDa(
                orderItemMain.userId!!,
                arrayListDaStatus[position].id!!,
                "আপনি একটি অর্ডার ${orderItemMain.orderId} পেয়েছেন ।",
                "আপনি একটি অর্ডার পেয়েছেন দ্রুত অর্ডারটি রিসিভ করুন ।",
                orderId
              )
              alertDialogForDa.dismiss()
              requireContext().showToast("SUCCESS", FancyToast.SUCCESS)
              workWithTheDocumentData(view, itOrder)
            } else {
              requireContext().showToast("Failed to assign", FancyToast.ERROR)
            }
          }
        }
        alertDialogForDa.setView(alertDialogForDaView)
        alertDialogForDa.show()
      }
    }
  }

  private fun removeDaFromThisOrder(view: View, orderItemMain: OrderItemMain) {
    AlertDialog.Builder(requireContext())
      .setTitle("Are you sure you want to reverse this order?")
      .setMessage("By clicking yes you're removing this da and making the order status verified")
      .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
        progressDialog.show()
        var daDetails = HashMap<String, Any>()
        daDetails["daID"] = ""
        daDetails["orderStatus"] = OrderStatus.VERIFIED
        daDetails["orderCompletedStatus"] = ""
        LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, daDetails)) { itOrder ->
          progressDialog.dismiss()
          if (itOrder.id != null) {
            requireContext().showToast("Removed DA", FancyToast.SUCCESS)
            workWithTheDocumentData(view, itOrder)
          } else {
            requireContext().showToast("Failed to assign", FancyToast.ERROR)
          }
        }
      })
      .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
      })
      .create().show()
  }

  private fun verifyUsersOrder(view: View, orderItemMain: OrderItemMain) {
    AlertDialog.Builder(requireContext())
      .setTitle("Are you sure you want to verify this order?")
      .setMessage("By clicking yes you're approving this order and verifying it's existence")
      .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
        progressDialog.show()
        val hashMap = HashMap<String, Any>()
        hashMap["orderStatus"] = OrderStatus.VERIFIED
        hashMap["verifiedTimeStampMillis"] = System.currentTimeMillis()
        LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, hashMap)) { itOrder ->
          progressDialog.dismiss()
          if (itOrder.id != null) {
            requireContext().showToast("Verified Successfully", FancyToast.SUCCESS)
            workWithTheDocumentData(view, itOrder)
            sendNotification(
              orderItemMain.userId!!,
              "আপনার অর্ডার ${orderItemMain.orderId} টি কনফার্ম করা হয়েছে ।",
              "আপনার অর্ডারটি কনফার্ম করা হয়েছে, দ্রুতই অর্ডারটি আপনার কাছে পৌছে যাবে ।",
              orderId
            )
          } else {
            requireContext().showToast("Failed to verify", FancyToast.ERROR)
          }
        }
      })
      .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
      })
      .create().show()
  }

  private fun showDeleteOrderCustomDialog(view: View, orderItemMain: OrderItemMain) {
    val alertDialogToDeleteUserData = AlertDialog.Builder(contextMain).create()
    val alertDialogToDeleteUserDataView =
      LayoutInflater.from(contextMain).inflate(R.layout.dialog_ask_password, null)
    alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.setOnClickListener {
      alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.isEnabled = false
      if (alertDialogToDeleteUserDataView.edt_enter_password_field.text.toString()
          .trim() == "TESTPASS"
      ) {
        alertDialogToDeleteUserData.dismiss()
        startDeletingProcess(view, orderItemMain)
      } else {
        contextMain.showToast("Cannot be empty", FancyToast.ERROR)
        alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.isEnabled = true
      }
    }
    alertDialogToDeleteUserData.setView(alertDialogToDeleteUserDataView)
    alertDialogToDeleteUserData.show()
  }

  private fun startDeletingProcess(view: View, orderItemMain: OrderItemMain) {
    view.orderHistoryProgressBarContainer.visibility = View.VISIBLE
    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.GONE
    LiveDataUtil.observeOnce(orderViewModel.deleteItem(orderId)) {
      if (it.error != true) {
        contextMain.showToast("Delete Success", FancyToast.ERROR)
        homeMainNewInterface.callOnBackPressed()
      } else {
        contextMain.showToast("Failed To Delete", FancyToast.ERROR)
        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.GONE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
      }
    }
  }

  private fun setStepView(
    view: View,
    setpview5: HorizontalStepView,
    stepsBeanList: MutableList<StepBean>
  ) {
    setpview5.setStepViewTexts(stepsBeanList) //总步骤
      .setTextSize(10) //set textSize
      .setStepsViewIndicatorCompletedLineColor(
        ContextCompat.getColor(
          contextMain,
          R.color.colorPrimary
        )
      ) //设置StepsViewIndicator完成线的颜色
      .setStepsViewIndicatorUnCompletedLineColor(
        ContextCompat.getColor(
          contextMain,
          R.color.colorPrimary
        )
      ) //设置StepsViewIndicator未完成线的颜色
      .setStepViewComplectedTextColor(
        ContextCompat.getColor(
          contextMain!!,
          R.color.grey_normal
        )
      ) //设置StepsView text完成线的颜色
      .setStepViewUnComplectedTextColor(
        ContextCompat.getColor(
          contextMain,
          R.color.grey_normal
        )
      ) //设置StepsView text未完成线的颜色
      .setStepsViewIndicatorCompleteIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.ic_baseline_checked
        )
      ) //设置StepsViewIndicator CompleteIcon
      .setStepsViewIndicatorDefaultIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.unchecked_bg_stroked
        )
      ) //设置StepsViewIndicator DefaultIcon
      .setStepsViewIndicatorAttentionIcon(
        ContextCompat.getDrawable(
          contextMain,
          R.drawable.attention
        )
      ) //设置StepsViewIndicator AttentionIcon
  }

  fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTimeInMillis(milliSeconds)
    return formatter.format(calendar.getTime())
  }

  private fun cancelOrderItem(view: View, orderItemMain: OrderItemMain) {
    val alertDialogToCancelUserData = AlertDialog.Builder(contextMain).create()
    val alertDialogToCancelUserDataView =
      LayoutInflater.from(contextMain).inflate(R.layout.dialog_ask_cancellation_reason, null)
    alertDialogToCancelUserDataView.deleteOrderItemMainDialogButton.text = "Confirm Cancellation"
    alertDialogToCancelUserDataView.edt_enter_password_field_container.hint = "Cancellation Reason"
    alertDialogToCancelUserDataView.addRspnceImageButton.setOnClickListener {
      val arrayListPrefs = ArrayList<String>()
      val arrayListPrefsMain = ArrayList<String>()
      FirebaseDatabase.getInstance().reference.child("cancellationResponses").get()
        .addOnSuccessListener {
          for (item in it.children) {
            item.key?.let { it1 -> arrayListPrefs.add(it1) }
            arrayListPrefsMain.add(item.value as String)
          }
          val arrayAdapter =
            ArrayAdapter(contextMain, R.layout.custom_spinner_item_view, arrayListPrefsMain)
          val alertDialogPrefs = AlertDialog.Builder(contextMain).create()
          val alertDialogPrefsView =
            LayoutInflater.from(contextMain).inflate(R.layout.assign_da_list_view, null)
          alertDialogPrefsView.txtAllPrice.text = "Select Cancellation Reason"
          alertDialogPrefsView.listView.adapter = arrayAdapter
          alertDialogPrefsView.listView.setOnItemClickListener { parent, view2, position, id ->
            alertDialogToCancelUserDataView.edt_enter_password_field.setText(arrayListPrefsMain[position])
            alertDialogPrefs.dismiss()
          }
          alertDialogPrefsView.listView.setOnItemLongClickListener { parent, view, position, id ->
            AlertDialog.Builder(contextMain)
              .setTitle("Delete ?")
              .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                FirebaseDatabase.getInstance().reference
                  .child("cancellationResponses")
                  .child(arrayListPrefs[position])
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
    }
    alertDialogToCancelUserDataView.addRspnceImageButton.setOnLongClickListener {
      val savedPrefClientTf = SavedPrefClientTf()
      savedPrefClientTf.key = "SPC" + System.currentTimeMillis()
      savedPrefClientTf.user_name =
        alertDialogToCancelUserDataView.edt_enter_password_field.text.toString()
      if (savedPrefClientTf.key.isNotEmpty() && savedPrefClientTf.user_name.isNotEmpty()) {
        FirebaseDatabase.getInstance().reference
          .child("cancellationResponses")
          .child(savedPrefClientTf.key)
          .setValue(savedPrefClientTf.user_name)
          .addOnCompleteListener {
            contextMain.showToast("Success", FancyToast.SUCCESS)
          }
      } else {
        contextMain.showToast("Is Empty", FancyToast.ERROR)
      }
      true
    }
    alertDialogToCancelUserDataView.deleteOrderItemMainDialogButton.setOnClickListener {
      contextMain.showToast("Long Click To Confirm", FancyToast.CONFUSING)
    }
    alertDialogToCancelUserDataView.deleteOrderItemMainDialogButton.setOnLongClickListener {
      alertDialogToCancelUserData.dismiss()
      progressDialog.show()
      val hashMap = HashMap<String, Any>()
      hashMap["orderStatus"] = "COMPLETED"
      hashMap["orderCompletedStatus"] = "CANCELLED"
      hashMap["cancelledOrderReasonFromAdmin"] =
        alertDialogToCancelUserDataView.edt_enter_password_field.text.toString().trim()
      LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, hashMap)) {
        progressDialog.dismiss()
        if (it.id != null) {
          contextMain.showToast("Success", FancyToast.SUCCESS)
                    sendNotification(
            orderItemMain.userId!!,
            "আপনার অর্ডার ${orderItemMain.orderId} টি ক্যান্সেল করা হয়েছে ।",
            "অর্পণের সাথে থাকার জন্য ধন্যবাদ ।",
            orderId
          )
          workWithTheDocumentData(view, it)
        } else {
          contextMain.showToast("Failed", FancyToast.ERROR)
        }
      }
      true
    }
    alertDialogToCancelUserData.setView(alertDialogToCancelUserDataView)
    alertDialogToCancelUserData.show()
  }

  fun sendNotification(
    userId: String,
    apititle: String,
    apibody: String,
    orderID: String
  ) {
    LiveDataUtil.observeOnce(notificationViewModel.sendNotificationToDA(
      SendNotificationRequest(
        userId = userId,
        title = apititle,
        body = apibody,
        orderId = orderID
      )
    )){
      Log.e("Notification", it.toString())
    }
  }

  fun sendNotificationToDa(
    userId: String,
    daId: String,
    apititle: String,
    apibody: String,
    orderID: String
  ) {
    LiveDataUtil.observeOnce(notificationViewModel.sendNotificationToDA(
      SendNotificationRequest(
        userId = userId,
        daId = daId,
        title = apititle,
        body = apibody,
        orderId = orderID
      )
    )){
      Log.e("Notification", it.toString())
    }
  }

  private fun setTextOnTextViewsOnMainUi(view: View, orderItemMain: OrderItemMain) {
    view.orderIdTextView.text = "Order# " + orderItemMain.orderId

    view.userNameEditText.setText(orderItemMain.userName)

    view.userMobileEdittext.setText(orderItemMain.userNumber)

    view.callUserNowImageButton.setOnClickListener {
      callNow(orderItemMain.userNumber!!)
    }
    view.callUserNowImageButtonPrivate.setOnClickListener {
      callNow(orderItemMain.userPhoneAccount!!)
    }

    if (orderItemMain.locationItem!!.locationName!!.trim() == "মাগুরা সদর") {
      if (!orderItemMain.userAddress.isNullOrEmpty()) {
        view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
        view.userAddressEdittext.setText(orderItemMain.userAddress)
      } else {
        view.userAddressEdittextContainerLayout.visibility = View.GONE
      }
    } else {
      view.userAddressEdittextContainerLayout.visibility = View.GONE
    }

    view.userLocationEdittext.setText(orderItemMain.locationItem!!.locationName)

    view.userNameEditText.setText(orderItemMain.userName)

    if (!orderItemMain.userNote.isNullOrEmpty()) {
      view.orderNoteEdittextContainerLayout.visibility = View.VISIBLE
      view.orderNoteEdittext.setText(orderItemMain.userNote)
    } else {
      view.orderNoteEdittextContainerLayout.visibility = View.GONE
    }
    if (!orderItemMain.adminOrderNote.isNullOrEmpty()) {
      view.orderAdminNoteEdittextContainerLayout.visibility = View.VISIBLE
      view.orderAdminNoteEdittext.setText(orderItemMain.adminOrderNote)
    } else {
      view.orderAdminNoteEdittextContainerLayout.visibility = View.GONE
    }

    view.orderStatusTopButton.setOnLongClickListener {
      forceChangeOrderStatusNow(view, orderItemMain)
      true
    }
    view.orderStatusTopButton.setOnClickListener {
      showEditOrderDetailsDialog(view, orderItemMain)
    }
  }

  private fun forceChangeOrderStatusNow(view: View, orderItemMain: OrderItemMain) {
    AlertDialog.Builder(requireContext())
      .setTitle("Are you sure you want to change order status?")
      .setMessage("By clicking yes you'll be able to change the order status and might make unexpected changes")
      .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
        showDialogToForceChangeOrderStatus(view, orderItemMain)
      })
      .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
      })
      .create().show()
  }

  private fun showDialogToForceChangeOrderStatus(view: View, orderItemMain: OrderItemMain) {
    val dialogToForceChangeOrderStatus = AlertDialog.Builder(requireContext()).create()
    val dialogToForceChangeOrderStatusView =
      LayoutInflater.from(requireContext()).inflate(R.layout.dialog_force_change_order_status, null)
    when (orderItemMain.orderStatus) {
      "PENDING" -> {
        dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.pendingRadioButton)
        dialogToForceChangeOrderStatusView.processingRadioButton.visibility = View.GONE
        dialogToForceChangeOrderStatusView.pickedRadioButton.visibility = View.GONE
        dialogToForceChangeOrderStatusView.completedRadio.visibility = View.GONE
      }
      "VERIFIED" -> {
        dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.verifiedRadioButton)
        if (!orderItemMain.daID.isNullOrEmpty()) {
          dialogToForceChangeOrderStatusView.processingRadioButton.visibility = View.GONE
          dialogToForceChangeOrderStatusView.pickedRadioButton.visibility = View.GONE
          dialogToForceChangeOrderStatusView.completedRadio.visibility = View.GONE
        }
      }
      "PROCESSING" -> {
        dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.processingRadioButton)
      }
      "PICKED UP" -> {
        dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.pickedRadioButton)
      }
      "COMPLETED" -> {
        if (orderItemMain.orderCompletedStatus == "CANCELLED") {
          dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.cancelledRadio)
          dialogToForceChangeOrderStatusView.processingRadioButton.visibility = View.GONE
          dialogToForceChangeOrderStatusView.pickedRadioButton.visibility = View.GONE
          dialogToForceChangeOrderStatusView.completedRadio.visibility = View.GONE
        } else {
          dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.completedRadio)
        }
      }
    }
    dialogToForceChangeOrderStatusView.saveForceOrderChangeStatusButton.setOnClickListener {
      requireContext().showToast("Long Click To Force Save", FancyToast.CONFUSING)
    }
    dialogToForceChangeOrderStatusView.saveForceOrderChangeStatusButton.setOnLongClickListener {
      val hashMap = HashMap<String, Any>()
      when (dialogToForceChangeOrderStatusView.statusRadioButtonGroup.checkedRadioButtonId) {
        R.id.pendingRadioButton -> {
          hashMap["orderStatus"] = "PENDING"
          hashMap["orderCompletedStatus"] = ""
          hashMap["daID"] = ""
        }
        R.id.cancelledRadio -> {
          hashMap["orderStatus"] = "COMPLETED"
          hashMap["orderCompletedStatus"] = "CANCELLED"
          hashMap["daID"] = ""
        }
        else -> {
          hashMap["orderStatus"] = (dialogToForceChangeOrderStatusView
            .findViewById(
              dialogToForceChangeOrderStatusView
                .statusRadioButtonGroup.checkedRadioButtonId
            ) as MaterialRadioButton).text
            .toString()
          hashMap["orderCompletedStatus"] = ""
        }
      }
      dialogToForceChangeOrderStatus.dismiss()
      LiveDataUtil.observeOnce(orderViewModel.updateItem(orderId, hashMap)) {
        progressDialog.dismiss()
        if (it.id != null) {
          requireContext().showToast("Successfully Changed", FancyToast.SUCCESS)
          workWithTheDocumentData(view, it)
        } else {
          requireContext().showToast(it.message.toString(), FancyToast.ERROR)
        }
      }
      true
    }
    dialogToForceChangeOrderStatus.setView(dialogToForceChangeOrderStatusView)
    dialogToForceChangeOrderStatus.show()
  }

  private fun showEditOrderDetailsDialog(view: View, orderItemMain: OrderItemMain) {
    homeViewModelMainData.currentSelectedOrderItemToEdit = orderItemMain
    val editOrderFragment = EditOrderFragment(orderItemMain)
    editOrderFragment.show(parentFragmentManager, "")
  }
}