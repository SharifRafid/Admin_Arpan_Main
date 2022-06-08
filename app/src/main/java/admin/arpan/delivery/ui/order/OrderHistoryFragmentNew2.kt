package admin.arpan.delivery.ui.order

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.home.HomeViewModel
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.callPermissionCheck
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
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
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
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
import kotlinx.android.synthetic.main.fragment_edit_order.view.autofillAllTextBoxesId
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


class OrderHistoryFragmentNew2 : Fragment() {
    private var newMainShopItemHashMap = ArrayList<MainShopCartItem>()
    private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
    private val allOrderItemArrayList = ArrayList<MainShopCartItem>()
    private lateinit var productRecyclerViewAdapter : OrderProductItemRecyclerAdapter
    private lateinit var progressDialog : Dialog
    private lateinit var firebaseFirestore: FirebaseFirestore
    private val TAG = "OdrHisFgmntNw2"
    private var orderId = ""
    private var customerId = ""
    private var listenerRegistration : ListenerRegistration? = null
    private var eventListener : EventListener<DocumentSnapshot>? = null
    private var orderItemMain : OrderItemMain? = null
    private var currentCalc = 0
    lateinit var contextMain : Context
    lateinit var viewMain : View
    private lateinit var homeViewModelMainData: HomeViewModelMainData
    private lateinit var homeMainNewInterface: HomeMainNewInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextMain  = context
        try{
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_order_history_new, container, false)
    }

    override fun onStop() {
        super.onStop()
        if (listenerRegistration != null) {
            listenerRegistration!!.remove()
        }
    }
    override fun onStart() {
        super.onStart()
        if(eventListener!=null){
            listenerRegistration = FirebaseFirestore.getInstance()
                .collection("users")
                .document(customerId)
                .collection("users_order_collection")
                .document(orderId)
                .addSnapshotListener(eventListener!!)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewMain = view
        initVars(view)
        if(FirebaseAuth.getInstance().currentUser == null){
            view.orderHistoryProgressBarContainer.visibility = View.GONE
            view.noDataFoundLinearLayoutContainer.visibility = View.VISIBLE
            view.mainOrderDetailsDataContainerLinearLayout.visibility = View.GONE
        }else{
            if (listenerRegistration == null ) {
                progressDialog.show()
                listenerRegistration = FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId).addSnapshotListener(eventListener!!)
            }
        }

    }

    private fun initVars(view: View) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        progressDialog = contextMain.createProgressDialog()
        productRecyclerViewAdapter = OrderProductItemRecyclerAdapter(contextMain, mainShopItemHashMap)
        eventListener = EventListener<DocumentSnapshot> { snapshot, e ->
            e?.printStackTrace()
            progressDialog.dismiss()
            if (snapshot != null && snapshot.exists()) {
                view.orderHistoryProgressBarContainer.visibility = View.GONE
                view.noDataFoundLinearLayoutContainer.visibility = View.GONE
                view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
                workWithTheDocumentData(view, snapshot)
            }else{
                view.orderHistoryProgressBarContainer.visibility = View.GONE
                view.noDataFoundLinearLayoutContainer.visibility = View.VISIBLE
                view.mainOrderDetailsDataContainerLinearLayout.visibility = View.GONE
            }
        }
        homeViewModelMainData = ViewModelProvider(requireActivity()).get(HomeViewModelMainData::class.java)
        orderId = arguments?.getString("orderID").toString()
        customerId = arguments?.getString("customerId").toString()
        view.title_text_view.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }
    }

    private fun workWithTheDocumentData(view: View, snapshot: DocumentSnapshot) {
        // Here we have the snapshot of the "OrderItemMain" and can freely work with it
        // null check and empty check is already done in the previous stage
        orderItemMain = snapshot.toObject(OrderItemMain::class.java) // Parsing to model class
        orderItemMain!!.docID = snapshot.id

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

    private fun setLogicForCardsAndCountsOnStatistics(view: View, orderItemMain: OrderItemMain) {
        view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
        view.orderDeliveryPrice.text = orderItemMain.deliveryCharge.toString()
        view.orderDaPrice.text = orderItemMain.daCharge.toString()

    //Order Was Successfully completed so we can calculate this value
    if(orderItemMain.products.size == 1 && !orderItemMain.products.any { it.product_item }){
        // This is  a custom order so it'll have separate calculation

        // The amount that the customer gives to the delivery agent
        val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

        // The amount the delivery agent pays to the shop/store before collecting the product
        val shopAmount = orderItemMain.totalPrice

        var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

        if(orderItemMain.paymentMethod == "bKash"){
            val bkashChargeExtra = roundNumberPriceTotal((orderItemMain.totalPrice+orderItemMain.deliveryCharge)
                    *CalculationLogics().getBkashChargePercentage()).toInt()
            deliveryAgentsDueToArpan -= bkashChargeExtra
        }


        view.orderArpanChargePrice.text = deliveryAgentsDueToArpan.toString()
    }else{
        // This is a shop order so this will also have another type of calculation

        // The amount that the customer gives to the delivery agent
        val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

        // The amount the delivery agent pays to the shop/store before collecting the product
        val shopAmount = calculateShopAmount(orderItemMain.products)

        var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

        if(orderItemMain.paymentMethod == "bKash"){
            val bkashChargeExtra = roundNumberPriceTotal((orderItemMain.totalPrice+orderItemMain.deliveryCharge)
                    *CalculationLogics().getBkashChargePercentage()).toInt()
            deliveryAgentsDueToArpan -= bkashChargeExtra
        }

        view.orderArpanChargePrice.text = deliveryAgentsDueToArpan.toString()
    }
    }

    private fun calculateShopAmount(products: List<CartProductEntity>): Int {
        var shopAmount = 0
        for(product in products){
            shopAmount += (product.product_item_price-product.product_arpan_profit)*product.product_item_amount
        }
        return shopAmount
    }
    private fun roundNumberPriceTotal(d: Float): Int {
        //This  is a special round function exclusively for this  page of the app
        //not usable for general parts and other parts of   the code or apps
        return if(d > d.toInt()){
            d.toInt()+1
        }else{
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

    private fun setLogicForPlacingOrderItemDetailsAndSeparatingProducts(view: View, orderItemMain: OrderItemMain) {
        if(orderItemMain.pickDropOrder){
            initPickDropDataLogicAndPlaceDataOnUi(view, orderItemMain)
            }else{
            if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
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
            }else{
                workWithTheArrayList(orderItemMain.products, view)
            }
        }
    }

    private fun placeCustomOrderItemData(view: View, orderItemMain: OrderItemMain) {
        view.orderTitleEdittextContainerLayout.visibility = View.GONE
        view.orderItemsMainRecyclerView.visibility = View.GONE

        if(orderItemMain.products[0].custom_order_text.trim().isNotEmpty()){
            view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
            view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
            view.orderDetailsEdittext.setText(orderItemMain.products[0].custom_order_text)
        }else{
            view.orderDetailsEdittextContainerLayout.visibility = View.GONE
        }
        if(orderItemMain.products[0].custom_order_image.isNotEmpty()){
            view.orderImagePicture.visibility = View.VISIBLE
            val firebaseStorage = FirebaseStorage.getInstance()
                .reference.child("ORDER_IMAGES")
                .child(orderItemMain.key)
                .child(orderItemMain.products[0].custom_order_image)
            setOrderImageOnView(view, firebaseStorage)
        }else{
            view.orderImagePicture.visibility = View.GONE
        }
    }
    private fun placeMedicineItemData(view: View, orderItemMain: OrderItemMain) {
        view.orderItemsMainRecyclerView.visibility = View.GONE
        if(orderItemMain.products[0].medicine_order_text.trim().isNotEmpty()){
            view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
            view.orderTitleEdittextContainerLayout.hint = "ফার্মেসি"
            view.orderTitleEdittext.setText(orderItemMain.products[0].medicine_order_text)
        }else{
            view.orderTitleEdittextContainerLayout.visibility = View.GONE
        }
        if(orderItemMain.products[0].medicine_order_text_2.trim().isNotEmpty()){
            view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
            view.orderDetailsEdittextContainerLayout.hint = "ঔষধ"
            view.orderDetailsEdittext.setText(orderItemMain.products[0].medicine_order_text_2)
        }else{
            view.orderDetailsEdittextContainerLayout.visibility = View.GONE
        }
        if(orderItemMain.products[0].medicine_order_image.isNotEmpty()){
            view.orderImagePicture.visibility = View.VISIBLE
            val firebaseStorage = FirebaseStorage.getInstance()
                .reference.child("ORDER_IMAGES")
                .child(orderItemMain.key)
                .child(orderItemMain.products[0].medicine_order_image)
            setOrderImageOnView(view, firebaseStorage)
        }else{
            view.orderImagePicture.visibility = View.GONE
        }
    }
    private fun placeParcelItemData(view: View, orderItemMain: OrderItemMain) {
        view.orderItemsMainRecyclerView.visibility = View.GONE

        if(orderItemMain.products[0].parcel_order_text.trim().isNotEmpty()){
            view.orderTitleEdittextContainerLayout.visibility = View.VISIBLE
            view.orderTitleEdittextContainerLayout.hint = "কুরিয়ার নেম"
            view.orderTitleEdittext.setText(orderItemMain.products[0].parcel_order_text)
        }else{
            view.orderTitleEdittextContainerLayout.visibility = View.GONE
        }
        if(orderItemMain.products[0].parcel_order_text_2.trim().isNotEmpty()){
            view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
            view.orderDetailsEdittextContainerLayout.hint = "ডিটেইলস"
            view.orderDetailsEdittext.setText(orderItemMain.products[0].parcel_order_text_2)
        }else{
            view.orderDetailsEdittextContainerLayout.visibility = View.GONE
        }

        if(orderItemMain.products[0].parcel_order_image.isNotEmpty()){
            view.orderImagePicture.visibility = View.VISIBLE
            val firebaseStorage = FirebaseStorage.getInstance()
                .reference.child("ORDER_IMAGES")
                .child(orderItemMain.key)
                .child(orderItemMain.products[0].parcel_order_image)
            setOrderImageOnView(view, firebaseStorage)
        }else{
            view.orderImagePicture.visibility = View.GONE
        }
    }
    private fun initPickDropDataLogicAndPlaceDataOnUi(view: View, orderItemMain: OrderItemMain) {
        view.orderTitleEdittextContainerLayout.visibility = View.GONE
        view.orderDetailsEdittextContainerLayout.visibility = View.VISIBLE
        view.orderItemsMainRecyclerView.visibility = View.GONE
        view.orderDetailsEdittextContainerLayout.hint = "পিক-আপ এন্ড ড্রপ"
        view.callUserNowReciverImageButton.setOnClickListener {
            callNow(orderItemMain.pickDropOrderItem.senderPhone)
        }
        view.userNameEditText.setText(orderItemMain.pickDropOrderItem.senderName)
        view.userMobileEdittext.setText(orderItemMain.pickDropOrderItem.senderPhone)
        view.userLocationEdittext.setText(orderItemMain.pickDropOrderItem.senderName)
        if(orderItemMain.pickDropOrderItem.senderAddress.trim().isNotEmpty()){
            view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
            view.userAddressEdittext.setText(orderItemMain.pickDropOrderItem.senderAddress)
        }else{
            view.userAddressEdittextContainerLayout.visibility = View.GONE
        }
        view.orderDetailsEdittext.setText(
            StringBuilder()
            .append("প্রেরকের তথ্যঃ ")
            .append("\n")
            .append("নামঃ ")
            .append(orderItemMain.pickDropOrderItem.senderName)
            .append("\n")
            .append("মোবাইলঃ ")
            .append(orderItemMain.pickDropOrderItem.senderPhone)
            .append("\n")
            .append("ঠিকানাঃ ")
            .append(orderItemMain.pickDropOrderItem.senderAddress)
            .append("\n")
            .append("লোকেশনঃ")
            .append(orderItemMain.pickDropOrderItem.senderLocation)
            .append("\n")
            .append("\n")
            .append("প্রাপকের তথ্যঃ ")
            .append("\n")
            .append("নামঃ ")
            .append(orderItemMain.pickDropOrderItem.recieverName)
            .append("\n")
            .append("মোবাইলঃ ")
            .append(orderItemMain.pickDropOrderItem.recieverPhone)
            .append("\n")
            .append("ঠিকানাঃ ")
            .append(orderItemMain.pickDropOrderItem.recieverAddress)
                .append("\n")
                .append("লোকেশনঃ")
                .append(orderItemMain.pickDropOrderItem.recieverLocation)
            .append("\n")
            .append("\n")
            .append("পার্সেলের তথ্যঃ ")
            .append("\n")
            .append(orderItemMain.pickDropOrderItem.parcelDetails))

        if(orderItemMain.pickDropOrderItem.recieverPhone.isNotEmpty()){
            view.recieverMobileNumberContainer.visibility = View.VISIBLE
            view.userReciverMobileEdittext.setText(orderItemMain.pickDropOrderItem.recieverPhone)
            view.callUserNowReciverImageButton.setOnClickListener {
                callNow(orderItemMain.pickDropOrderItem.recieverPhone)
            }
        }else{
            view.recieverMobileNumberContainer.visibility = View.GONE
        }

        if(orderItemMain.pickDropOrderItem.parcelImage.isNotEmpty()){
            view.orderImagePicture.visibility = View.VISIBLE
            val firebaseStorage = FirebaseStorage.getInstance()
                .reference.child("ORDER_IMAGES")
                .child(orderItemMain.key)
                .child(orderItemMain.pickDropOrderItem.parcelImage)
            setOrderImageOnView(view, firebaseStorage)
        }else{
            view.orderImagePicture.visibility = View.GONE
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

    private fun setOrderImageOnView(view: View, firebaseStorage: StorageReference) {
        Glide.with(contextMain)
            .load(firebaseStorage)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerInside()
            .placeholder(R.drawable.loading_image_glide)
            .into(view.orderImagePicture)

        view.orderImagePicture.setOnClickListener {
            val dialog = AlertDialog.Builder(contextMain, R.style.Theme_AdminArpan).create()
            val view2 = LayoutInflater.from(contextMain).inflate(R.layout.product_image_big_view,null)
            view2.floatingActionButton.setOnClickListener{
                dialog.dismiss()
            }
            Glide.with(contextMain)
                .load(firebaseStorage)
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
        for(cartItemEntity in products){
            val filteredArray = mainShopItemHashMap.filter { it -> it.shop_doc_id == cartItemEntity.product_item_shop_key }
            if(filteredArray.isEmpty()){
                val shopItem = MainShopCartItem()
                shopItem.shop_doc_id = cartItemEntity.product_item_shop_key
                shopItem.cart_products.add(cartItemEntity)
                mainShopItemHashMap.add(shopItem)
            }else{
                mainShopItemHashMap[mainShopItemHashMap.indexOf(filteredArray[0])]
                    .cart_products.add(cartItemEntity)
            }
        }
        if(mainShopItemHashMap.isNotEmpty()){
            // This has shop specific products in the order :D
            currentCalc = 0
            fillUpShopDetailsValueInMainShopItemList(view)
        }
    }
    private fun fillUpShopDetailsValueInMainShopItemList(view: View) {
        Log.e(TAG, mainShopItemHashMap[currentCalc].shop_doc_id)
        Log.e(TAG, mainShopItemHashMap.size.toString())
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
            .document(mainShopItemHashMap[currentCalc].shop_doc_id)
            .get(Source.CACHE).addOnSuccessListener { document ->
                mainShopItemHashMap[currentCalc].shop_details =
                    if(document.data == null){
                        ShopItem(
                            key = "",
                            name = "NAME NOT FOUND",
                            categories = "",
                            image = "",
                            cover_image = "",
                            da_charge = "",
                            deliver_charge = "",
                            location = "",
                            username = "",
                            password = "",
                            order = 0,
                            status = ""
                        )
                    }else{
                        ShopItem(
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
                            status = document.getString(Constants.FIELD_FD_SM_STATUS).toString()
                        )
                    }
                if(currentCalc+1 >= mainShopItemHashMap.size){
                    newMainShopItemHashMap.clear()
                    newMainShopItemHashMap = ArrayList()
                    for(item in mainShopItemHashMap){
                        val mainShopCartItem = MainShopCartItem()
                        mainShopCartItem.key = item.key
                        mainShopCartItem.shop_details = item.shop_details
                        mainShopCartItem.shop_doc_id = item.shop_doc_id
                        for(cartItem in item.cart_products){
                            val i = cartItem.copy()
                            i.product_item_price -= i.product_arpan_profit
                            mainShopCartItem.cart_products.add(i)
                        }
                        newMainShopItemHashMap.add(mainShopCartItem)
                    }
                    // The data is downloaded all of those
                    view.orderItemsMainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
                    view.orderItemsMainRecyclerView.adapter = productRecyclerViewAdapter
                }else{
                    currentCalc ++
                    fillUpShopDetailsValueInMainShopItemList(view)
                }
            }
    }

    private fun setTextOnRestOrderDetailsTextView(view: View, orderItemMain: OrderItemMain) {
        view.pickUpTimeTV.text = if(orderItemMain.pickUpTime.isNotEmpty()){
            orderItemMain.pickUpTime
        }else{
            "Now"
        }
        view.dateOrder.text = getDate(orderItemMain.orderPlacingTimeStamp, "dd/MM/yyyy")
        if(orderItemMain.promoCodeApplied){
            view.appliedPromoCodeButton.visibility = View.VISIBLE
            view.appliedPromoCodeButton.text = "প্রোমোকোড অ্যাড করা হয়েছেঃ ${orderItemMain.promoCode.promoCodeName}"
        }else{
            view.appliedPromoCodeButton.visibility = View.GONE
        }
        if(orderItemMain.paymentMethod == "COD"){
            view.pricePaymentStatusMain.text = "COD"
        }else{
            view.pricePaymentStatusMain.text = "bKash"
        }
        view.totalPriceTextViewMain.text = "Total: ${orderItemMain.totalPrice}+${orderItemMain.deliveryCharge} = ${orderItemMain.totalPrice+orderItemMain.deliveryCharge}"
    }

    private fun setLogicForOrderStatusOnThirdRow(view: View, orderItemMain: OrderItemMain) {
        view.step_view_order_progress.visibility = View.VISIBLE
        view.orderStatusTopButton.text = orderItemMain.orderStatus
        view.cancelOrderButton.visibility = View.GONE
        when(orderItemMain.orderStatus){
            "PENDING" -> {
                stop()
                val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
                val stepsBeanList: MutableList<StepBean> = ArrayList()
                val stepBean4 = StepBean("     PENDING     ", 1)
                val stepBean0 = StepBean("     VERIFIED     ", -1)
                val stepBean1 = StepBean("     PROCESSING     ", -1)
                val stepBean2 = StepBean("     PICKED UP     ", -1)
                val stepBean3 = StepBean("     COMPLETED     ", -1)
                if(orderItemMain.orderPlacingTimeStamp!=0L){
                    stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp,"hh:mm")})"
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
                if(orderItemMain.verifiedTimeStampMillis!=0L){
                    stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis,"hh:mm")})"
                }
                if(orderItemMain.orderPlacingTimeStamp!=0L){
                    stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp,"hh:mm")})"
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

                if(orderItemMain.daID.isNotEmpty()){
                    view.linearAssignedDA.visibility = View.VISIBLE
                    view.assignedToDaTextView.text = "Waiting For "+orderItemMain.daDetails.da_name+" ("+getDate((System.currentTimeMillis() - orderItemMain.assignedToDaTimeStampMillis), "mm:ss")+")"
                    start()
                    view.callDaNowImageView.setOnClickListener {
                        callNow(orderItemMain.daDetails.da_mobile)
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
                }else{
                    stop()
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
                stop()
                val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
                val stepsBeanList: MutableList<StepBean> = ArrayList()
                val stepBean4 = StepBean("     PENDING     ", 1)
                val stepBean0 = StepBean("     VERIFIED     ", 1)
                val stepBean1 = StepBean("     PROCESSING     ", 1)
                val stepBean2 = StepBean("     PICKED UP     ", -1)
                val stepBean3 = StepBean("     COMPLETED     ", -1)
                if(orderItemMain.processingTimeStampMillis!=0L){
                    stepBean1.name = "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis,"hh:mm")})"
                }
                if(orderItemMain.verifiedTimeStampMillis!=0L){
                    stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis,"hh:mm")})"
                }
                if(orderItemMain.orderPlacingTimeStamp!=0L){
                    stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp,"hh:mm")})"
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

                if(orderItemMain.daID.isNotEmpty()){
                    view.linearAssignedDA.visibility = View.VISIBLE
                    view.assignedToDaTextView.text = "Assigned to "+orderItemMain.daDetails.da_name+" ("+getDate((orderItemMain.processingTimeStampMillis - orderItemMain.assignedToDaTimeStampMillis), "mm:ss")+")"
                    view.callDaNowImageView.setOnClickListener {
                        callNow(orderItemMain.daDetails.da_mobile)
                    }
                    view.acceptOrderButton.visibility = View.GONE
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
                stop()
                view.step_view_order_progress.visibility = View.VISIBLE
                val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
                val stepsBeanList: MutableList<StepBean> = ArrayList()
                val stepBean4 = StepBean("     PENDING     ", 1)
                val stepBean0 = StepBean("     VERIFIED     ", 1)
                val stepBean1 = StepBean("     PROCESSING     ", 1)
                val stepBean2 = StepBean("     PICKED UP     ", 1)
                val stepBean3 = StepBean("     COMPLETED     ", -1)
                if(orderItemMain.processingTimeStampMillis!=0L){
                    stepBean1.name = "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis,"hh:mm")})"
                }
                if(orderItemMain.verifiedTimeStampMillis!=0L){
                    stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis,"hh:mm")})"
                }
                if(orderItemMain.orderPlacingTimeStamp!=0L){
                    stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp,"hh:mm")})"
                }
                if(orderItemMain.pickedUpTimeStampMillis!=0L){
                    stepBean2.name = "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis,"hh:mm")})"
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

                if(orderItemMain.daID.isNotEmpty()){
                    view.linearAssignedDA.visibility = View.VISIBLE
                    view.assignedToDaTextView.text = "Assigned to "+orderItemMain.daDetails.da_name+" ("+getDate((orderItemMain.processingTimeStampMillis - orderItemMain.assignedToDaTimeStampMillis), "mm:ss")+")"
                    view.callDaNowImageView.setOnClickListener {
                        callNow(orderItemMain.daDetails.da_mobile)
                    }
                    view.acceptOrderButton.visibility = View.GONE
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
                stop()
                if(orderItemMain.daID.isNotEmpty()) {
                    view.linearAssignedDA.visibility = View.VISIBLE
                    view.assignedToDaTextView.text =
                        "Assigned to " + orderItemMain.daDetails.da_name
                    view.callDaNowImageView.setOnClickListener {
                        callNow(orderItemMain.daDetails.da_mobile)
                    }
                    view.acceptOrderButton.visibility = View.GONE
                    view.cancelOrderButton.visibility = View.GONE
                }
                if(orderItemMain.orderCompletedStatus=="CANCELLED"){
                    view.step_view_order_progress.visibility = View.GONE
                    view.orderStatusTopButton.text = "CANCELLED"
                    view.cancelOrderButton.visibility = View.VISIBLE
                    view.cancelOrderButton.text = "Delete"
                    view.cancelOrderButton.setOnClickListener {
                        showDeleteOrderCustomDialog(view, orderItemMain)
                    }
                    if(orderItemMain.cancelledOrderReasonFromAdmin.trim().isNotEmpty()){
                        view.linearLayoutCancelReasonContainer.visibility = View.VISIBLE
                        view.orderCancellationReasonDetails.text = orderItemMain.cancelledOrderReasonFromAdmin
                    }else{
                        view.linearLayoutCancelReasonContainer.visibility = View.GONE
                    }
                    view.orderStatusTopButton.setBackgroundColor(Color.parseColor("#EA594D"))
                }else{
                    val setpview5 = view.findViewById<View>(R.id.step_view_order_progress) as HorizontalStepView
                    val stepsBeanList: MutableList<StepBean> = ArrayList()
                    val stepBean4 = StepBean("     PENDING     ", 1)
                    val stepBean0 = StepBean("     VERIFIED     ", 1)
                    val stepBean1 = StepBean("     PROCESSING     ", 1)
                    val stepBean2 = StepBean("     PICKED UP     ", 1)
                    val stepBean3 = StepBean("     COMPLETED     ", 1)
                    if(orderItemMain.processingTimeStampMillis!=0L){
                        stepBean1.name = "PROCESSING\n(${getDate(orderItemMain.processingTimeStampMillis,"hh:mm")})"
                    }
                    if(orderItemMain.verifiedTimeStampMillis!=0L){
                        stepBean0.name = "VERIFIED\n(${getDate(orderItemMain.verifiedTimeStampMillis,"hh:mm")})"
                    }
                    if(orderItemMain.orderPlacingTimeStamp!=0L){
                        stepBean4.name = "PENDING\n(${getDate(orderItemMain.orderPlacingTimeStamp,"hh:mm")})"
                    }
                    if(orderItemMain.pickedUpTimeStampMillis!=0L){
                        stepBean2.name = "PICKED UP\n(${getDate(orderItemMain.pickedUpTimeStampMillis,"hh:mm")})"
                    }
                    if(orderItemMain.completedTimeStampMillis!=0L){
                        stepBean3.name = "COMPLETED\n(${getDate(orderItemMain.completedTimeStampMillis,"hh:mm")})"
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

    private var started = false
    private val handler: Handler = Handler()

    private val runnable = Runnable {
        if(orderItemMain != null){
            viewMain.assignedToDaTextView.text = "Waiting For "+ orderItemMain!!.daDetails.da_name+" ("+getDate((System.currentTimeMillis() - orderItemMain!!.assignedToDaTimeStampMillis), "mm:ss")+")"
        }
        if (started) {
            start()
        }
    }

    fun stop() {
        started = false
        handler.removeCallbacks(runnable)
    }

    fun start() {
        started = true
        handler.postDelayed(runnable, 1000)
    }


    private fun showAssignOrderToDaListDialog(view: View, orderItemMain: OrderItemMain) {
        val alertDialogForDa = AlertDialog.Builder(context).create()
        val alertDialogForDaView = LayoutInflater.from(context).inflate(R.layout.assign_da_list_view, null)
        val arrayListDaStatus = homeViewModelMainData.getDaRealtimeStatusList().value!!
        val arrayListDaRemove = ArrayList<DaStatusItem>()
        val arrayListDaList = homeViewModelMainData.getDaMainListData().value!!
        for(item in arrayListDaStatus){
            for(daItem in arrayListDaList){
                if(daItem.da_name == item.name){
                    if(!daItem.da_status_active){
                        arrayListDaRemove.add(item)
                    }
                    break
                }
            }
        }
        arrayListDaStatus.removeAll(arrayListDaRemove)
        val arrayListDaStatusString = ArrayList<String>()
        for(daStatus in arrayListDaStatus){
            var daListItem = ""
            if(daStatus.status == "Active"){
                daListItem += "🟢 "+daStatus.name+" "
            }else{
                daListItem += "🔴 "+daStatus.name+" "
            }
            var daCompleted = 0
            var daProcessing = 0
            var daPickedUp = 0
            var daAssigned = 0
            for(order in homeViewModelMainData.getOrdersOneDayDataMainList().value!!){
                if(order.daID == daStatus.key){
                    when(order.orderStatus){
                        "VERIFIED" -> daAssigned += 1
                        "PROCESSING" -> daProcessing += 1
                        "PICKED UP" -> daProcessing += 1
                        "COMPLETED" -> daCompleted += 1
                    }
                }
            }
            daListItem += "[$daProcessing] [$daCompleted] \n ${daStatus.statusTextTitle}"
            arrayListDaStatusString.add(daListItem)
        }
        alertDialogForDaView.listView.adapter = ArrayAdapter(requireContext(),R.layout.custom_spinner_item_view, arrayListDaStatusString)
        alertDialogForDa.setView(alertDialogForDaView)
        alertDialogForDaView.listView.setOnItemClickListener { _, _, position, _ ->
            progressDialog.show()
            val daDetails = HashMap<String, Any>()
            FirebaseFirestore.getInstance()
                .collection("da_agents_main_list_collection")
                .document(arrayListDaStatus[position].key)
                .get().addOnCompleteListener {
                    if(it.isSuccessful){
                        val daAgent = it.result!!.toObject(DaAgent::class.java) as DaAgent
                        daAgent.key = it.result!!.id
                        daDetails["daDetails"] = daAgent
                        daDetails["daID"] = it.result!!.id
                        daDetails["assignedToDaTimeStampMillis"] = System.currentTimeMillis()
                        FirebaseFirestore.getInstance().collection("users")
                            .document(customerId)
                            .collection("users_order_collection")
                            .document(orderId)
                            .update(daDetails)
                            .addOnCompleteListener {_->
                                sendNotificationToDa(
                                    orderItemMain.userId,
                                    it.result!!.id,
                                    "আপনি একটি অর্ডার ${orderItemMain.orderId} পেয়েছেন ।",
                                    "আপনি একটি অর্ডার পেয়েছেন দ্রুত অর্ডারটি রিসিভ করুন ।",
                                    orderId
                                )
                                alertDialogForDa.dismiss()
                                progressDialog.dismiss()
                                requireContext().showToast("SUCCESS", FancyToast.SUCCESS)
                            }
                    }else{
                        progressDialog.dismiss()
                        requireContext().showToast("FAILED", FancyToast.SUCCESS)
                        it.exception!!.printStackTrace()
                    }
                }
        }
        alertDialogForDa.show()
    }
    private fun removeDaFromThisOrder(view: View, orderItemMain: OrderItemMain) {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to reverse this order?")
            .setMessage("By clicking yes you're removing this da and making the order status verified")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                progressDialog.show()
                var daDetails = HashMap<String,Any>()
                daDetails["daDetails"] = DaAgent()
                daDetails["daID"] = ""
                daDetails["orderStatus"] = "VERIFIED"
                daDetails["orderCompletedStatus"] = ""
                FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId)
                    .update(daDetails)
                    .addOnCompleteListener {_->
                        progressDialog.dismiss()
                        requireContext().showToast("CANCELLED", FancyToast.SUCCESS)
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
                val hashMap = HashMap<String,Any>()
                hashMap["orderStatus"] = "VERIFIED"
                hashMap["verifiedTimeStampMillis"] = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId)
                    .update(hashMap)
                    .addOnCompleteListener {task2 ->
                        if(task2.isSuccessful){
                            sendNotification(
                                orderItemMain.userId,
                                "আপনার অর্ডার ${orderItemMain.orderId} টি কনফার্ম করা হয়েছে ।",
                                "আপনার অর্ডারটি কনফার্ম করা হয়েছে, দ্রুতই অর্ডারটি আপনার কাছে পৌছে যাবে ।",
                                orderId)
                        }else{
                            task2.exception!!.printStackTrace()
                        }
                        progressDialog.dismiss()
                    }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create().show()
    }

    private fun showDeleteOrderCustomDialog(view: View, orderItemMain: OrderItemMain) {
        val alertDialogToDeleteUserData = AlertDialog.Builder(contextMain).create()
        val alertDialogToDeleteUserDataView = LayoutInflater.from(contextMain).inflate(R.layout.dialog_ask_password, null)
        alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.setOnClickListener {
            alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.isEnabled = false
            if(alertDialogToDeleteUserDataView.edt_enter_password_field.text.trim().isNotEmpty()){
                FirebaseAuth.getInstance().currentUser!!
                    .reauthenticate(
                        EmailAuthProvider.getCredential(
                            FirebaseAuth.getInstance().currentUser!!.email!!,
                            alertDialogToDeleteUserDataView.edt_enter_password_field.text.trim().toString()
                        )
                    ).addOnCompleteListener {
                        if(it.isSuccessful){
                            alertDialogToDeleteUserData.dismiss()
                            startDeletingProcess(view, orderItemMain)
                        }else{
                            contextMain.showToast("Wrong Password", FancyToast.ERROR)
                            alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.isEnabled = true
                        }
                    }
            }else{
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
        FirebaseFirestore.getInstance().collection("users")
            .document(customerId)
            .collection("users_order_collection")
            .document(orderId).delete().addOnCompleteListener {
                if(it.isSuccessful){
                    contextMain.showToast("Delete Success", FancyToast.ERROR)
                    homeMainNewInterface.callOnBackPressed()
                }else{
                    contextMain.showToast("Failed To Delete", FancyToast.ERROR)
                    view.orderHistoryProgressBarContainer.visibility = View.GONE
                    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
                    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
                }
            }
    }

    private fun setStepView(view:View ,setpview5: HorizontalStepView, stepsBeanList: MutableList<StepBean>) {
        setpview5.setStepViewTexts(stepsBeanList) //总步骤
            .setTextSize(10) //set textSize
            .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(contextMain,
                R.color.colorPrimary
            )) //设置StepsViewIndicator完成线的颜色
            .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(contextMain, R.color.colorPrimary)) //设置StepsViewIndicator未完成线的颜色
            .setStepViewComplectedTextColor(ContextCompat.getColor(contextMain!!, R.color.grey_normal)) //设置StepsView text完成线的颜色
            .setStepViewUnComplectedTextColor(ContextCompat.getColor(contextMain, R.color.grey_normal)) //设置StepsView text未完成线的颜色
            .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(contextMain,
                R.drawable.ic_baseline_checked
            )) //设置StepsViewIndicator CompleteIcon
            .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(contextMain,
                R.drawable.unchecked_bg_stroked
            )) //设置StepsViewIndicator DefaultIcon
            .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(contextMain, R.drawable.attention)) //设置StepsViewIndicator AttentionIcon
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
        val alertDialogToCancelUserDataView = LayoutInflater.from(contextMain).inflate(R.layout.dialog_ask_cancellation_reason, null)
        alertDialogToCancelUserDataView.deleteOrderItemMainDialogButton.text = "Confirm Cancellation"
        alertDialogToCancelUserDataView.edt_enter_password_field_container.hint = "Cancellation Reason"
        alertDialogToCancelUserDataView.addRspnceImageButton.setOnClickListener {
            val arrayListPrefs = ArrayList<String>()
            val arrayListPrefsMain= ArrayList<String>()
            FirebaseDatabase.getInstance().reference.child("cancellationResponses").get()
                .addOnSuccessListener {
                    for(item in it.children){
                        item.key?.let { it1 -> arrayListPrefs.add(it1) }
                        arrayListPrefsMain.add(item.value as String)
                    }
                    val arrayAdapter = ArrayAdapter(contextMain, R.layout.custom_spinner_item_view, arrayListPrefsMain)
                    val alertDialogPrefs = AlertDialog.Builder(contextMain).create()
                    val alertDialogPrefsView = LayoutInflater.from(contextMain).inflate(R.layout.assign_da_list_view, null)
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
        alertDialogToCancelUserDataView.addRspnceImageButton.setOnLongClickListener{
            val savedPrefClientTf = SavedPrefClientTf()
            savedPrefClientTf.key = "SPC"+System.currentTimeMillis()
            savedPrefClientTf.user_name = alertDialogToCancelUserDataView.edt_enter_password_field.text.toString()
            if(savedPrefClientTf.key.isNotEmpty()&&savedPrefClientTf.user_name.isNotEmpty()){
                FirebaseDatabase.getInstance().reference
                    .child("cancellationResponses")
                    .child(savedPrefClientTf.key)
                    .setValue(savedPrefClientTf.user_name)
                    .addOnCompleteListener {
                        contextMain.showToast("Success", FancyToast.SUCCESS)
                    }
            }else{
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
            val hashMap = HashMap<String,Any>()
            hashMap["orderStatus"] = "COMPLETED"
            hashMap["orderCompletedStatus"] = "CANCELLED"
            hashMap["cancelledOrderReasonFromAdmin"] = alertDialogToCancelUserDataView.edt_enter_password_field.text.toString().trim()
            FirebaseFirestore.getInstance().collection("users")
                .document(customerId)
                .collection("users_order_collection")
                .document(orderId)
                .update(hashMap)
                .addOnCompleteListener {task2 ->
                    if(task2.isSuccessful){
                        sendNotification(
                            orderItemMain.userId,
                            "আপনার অর্ডার ${orderItemMain.orderId} টি ক্যান্সেল করা হয়েছে ।",
                            "অর্পণের সাথে থাকার জন্য ধন্যবাদ ।",
                            orderId)
                    }else{
                        task2.exception!!.printStackTrace()
                    }
                    progressDialog.dismiss()
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
        val mediaType: MediaType =
            MediaType.parse("application/json; charset=utf-8")
        val data: MutableMap<String, String> =
            java.util.HashMap()
        data["userId"] = userId
        data["apititle"] = apititle
        data["apibody"] = apibody
        data["orderID"] = orderID
        data["click_action"] = ".ui.home.HomeActivity"
        val json = Gson().toJson(data)
        val body: RequestBody =
            RequestBody.create(
                mediaType,
                json
            )
        val request: Request = Request.Builder()
            .url("https://admin.arpan.delivery/api/notification/send-order-status-changed-notification")
            .post(body)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback{
            override fun onFailure(request: Request?, e: IOException?) {
                e!!.printStackTrace()
            }

            override fun onResponse(response: Response?) {
                Log.e("notifiication response" , response!!.message())
            }

        })
    }

    fun sendNotificationToDa(
        userId: String,
        daId: String,
        apititle: String,
        apibody: String,
        orderID: String
    ) {
        val mediaType: MediaType =
            MediaType.parse("application/json; charset=utf-8")
        val data: MutableMap<String, String> =
            java.util.HashMap()
        data["userId"] = userId
        data["daId"] = daId
        data["apititle"] = apititle
        data["apibody"] = apibody
        data["orderID"] = orderID
        data["click_action"] = ".ui.home.HomeActivity"
        val json = Gson().toJson(data)
        val body: RequestBody =
            RequestBody.create(
                mediaType,
                json
            )

        Log.e("notifiication response" , json)

        val request: Request = Request.Builder()
            .url("https://admin.arpan.delivery/api/notification/send-notification-to-da-about-a-new-order-that-he-recieved")
            .post(body)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback{
            override fun onFailure(request: Request?, e: IOException?) {
                Log.e("notifiication response" , e!!.message.toString())
                e!!.printStackTrace()
            }

            override fun onResponse(response: Response?) {
                Log.e("notifiication response" , response!!.message())
            }

        })
    }

    private fun setTextOnTextViewsOnMainUi(view: View, orderItemMain: OrderItemMain) {
        view.orderIdTextView.text = "Order# "+orderItemMain.orderId

        view.userNameEditText.setText(orderItemMain.userName)

        view.userMobileEdittext.setText(orderItemMain.userNumber)

        view.callUserNowImageButton.setOnClickListener {
            callNow(orderItemMain.userNumber)
        }
        view.callUserNowImageButtonPrivate.setOnClickListener {
            callNow(orderItemMain.userPhoneAccount)
        }

        if(orderItemMain.locationItem.locationName.trim()=="মাগুরা সদর"){
            if(orderItemMain.userAddress.trim().isNotEmpty()){
                view.userAddressEdittextContainerLayout.visibility = View.VISIBLE
                view.userAddressEdittext.setText(orderItemMain.userAddress)
            }else{
                view.userAddressEdittextContainerLayout.visibility = View.GONE
            }
        }else{
            view.userAddressEdittextContainerLayout.visibility = View.GONE
        }

        view.userLocationEdittext.setText(orderItemMain.locationItem.locationName)

        view.userNameEditText.setText(orderItemMain.userName)

        if(orderItemMain.userNote.trim().isNotEmpty()){
            view.orderNoteEdittextContainerLayout.visibility = View.VISIBLE
            view.orderNoteEdittext.setText(orderItemMain.userNote)
        }else{
            view.orderNoteEdittextContainerLayout.visibility = View.GONE
        }
        if(orderItemMain.adminOrderNote.trim().isNotEmpty()){
            view.orderAdminNoteEdittextContainerLayout.visibility = View.VISIBLE
            view.orderAdminNoteEdittext.setText(orderItemMain.adminOrderNote)
        }else{
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
        val dialogToForceChangeOrderStatusView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_force_change_order_status, null)
        when(orderItemMain.orderStatus){
            "PENDING" -> {
                dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.pendingRadioButton)
                dialogToForceChangeOrderStatusView.processingRadioButton.visibility = View.GONE
                dialogToForceChangeOrderStatusView.pickedRadioButton.visibility = View.GONE
                dialogToForceChangeOrderStatusView.completedRadio.visibility = View.GONE
            }
            "VERIFIED" -> {
                dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.verifiedRadioButton)
                if(orderItemMain.daID.isEmpty()){
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
                if(orderItemMain.orderCompletedStatus=="CANCELLED"){
                    dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.cancelledRadio)
                    dialogToForceChangeOrderStatusView.processingRadioButton.visibility = View.GONE
                    dialogToForceChangeOrderStatusView.pickedRadioButton.visibility = View.GONE
                    dialogToForceChangeOrderStatusView.completedRadio.visibility = View.GONE
                }else{
                    dialogToForceChangeOrderStatusView.statusRadioButtonGroup.check(R.id.completedRadio)
                }
            }
        }
        dialogToForceChangeOrderStatusView.saveForceOrderChangeStatusButton.setOnClickListener {
            requireContext().showToast("Long Click To Force Save", FancyToast.CONFUSING)
        }
        dialogToForceChangeOrderStatusView.saveForceOrderChangeStatusButton.setOnLongClickListener{
            val hashMap = HashMap<String,Any>()
            when(dialogToForceChangeOrderStatusView.statusRadioButtonGroup.checkedRadioButtonId) {
                R.id.pendingRadioButton -> {
                    hashMap["orderStatus"] = "PENDING"
                    hashMap["orderCompletedStatus"] = ""
                    hashMap["daDetails"] = DaAgent()
                    hashMap["daID"] = ""
                }
                R.id.cancelledRadio -> {
                    hashMap["orderStatus"] = "COMPLETED"
                    hashMap["orderCompletedStatus"] = "CANCELLED"
                    hashMap["daDetails"] = DaAgent()
                    hashMap["daID"] = ""
                }
                else -> {
                    hashMap["orderStatus"] = (dialogToForceChangeOrderStatusView
                        .findViewById(dialogToForceChangeOrderStatusView
                            .statusRadioButtonGroup.checkedRadioButtonId) as MaterialRadioButton).text
                        .toString()
                    hashMap["orderCompletedStatus"] = ""
                }
            }
            dialogToForceChangeOrderStatus.dismiss()
            FirebaseFirestore.getInstance().collection("users")
                .document(customerId)
                .collection("users_order_collection")
                .document(orderId)
                .update(hashMap)
                .addOnCompleteListener {task2 ->
                    if(task2.isSuccessful){
                        requireContext().showToast("Successfully Changed", FancyToast.SUCCESS)
                    }else{
                        task2.exception!!.printStackTrace()
                    }
                }
            true
        }
        dialogToForceChangeOrderStatus.setView(dialogToForceChangeOrderStatusView)
        dialogToForceChangeOrderStatus.show()
    }

    private fun showEditOrderDetailsDialog(view: View, orderItemMain: OrderItemMain) {
        homeViewModelMainData.currentSelectedOrderItemToEdit = orderItemMain
        val editOrderFragment = EditOrderFragment()
        val bundle = Bundle()
        bundle.putString("orderID",orderId)
        bundle.putString("customerId",customerId)
        editOrderFragment.arguments = bundle
        editOrderFragment.show(parentFragmentManager, "")
//
//        val alertDialogToEditOrderDetails = AlertDialog.Builder(contextMain).create()
//        val alertDialogToEditOrderDetailsView = LayoutInflater.from(contextMain).inflate(R.layout.edit_order_item, null)
//
//        // Setting the prefetched or previous values of the order item
//        alertDialogToEditOrderDetailsView.nameEdittextEditOrder.setText(orderItemMain.userName)
//        alertDialogToEditOrderDetailsView.phoneEdittextEditOrder.setText(orderItemMain.userNumber)
//        alertDialogToEditOrderDetailsView.addressEdittextEditOrder.setText(orderItemMain.userAddress)
//        alertDialogToEditOrderDetailsView.noteEdittextEditOrder.setText(orderItemMain.userNote)
//        alertDialogToEditOrderDetailsView.deliveryChargeTotalEditOrder.setText(orderItemMain.deliveryCharge.toString())
//        alertDialogToEditOrderDetailsView.daChargeTotalEditOrder.setText(orderItemMain.daCharge.toString())
//        alertDialogToEditOrderDetailsView.adminNoteEdittextEditOrder.setText(orderItemMain.adminOrderNote)
//        alertDialogToEditOrderDetailsView.cancellationReasonEdittext.setText(orderItemMain.cancelledOrderReasonFromAdmin)
//
//        //The total value should only be changed in case of custom orders and pickup and drops
//        // this field will not be enabled for shop orders because that might mess things up
//        // at least for now
//        var changeTotalValue = false
//
//        // Variables required for making the order item separation easier
//        var customOrder = false
//        var medicineOrder = false
//        var parcelOrder = false
//        var shopOrder = false
//
//        // Pick up and drop remains in a entirely separate category than the other shops and
//        // Custom orders, so this check is for that
//        if(orderItemMain.pickDropOrder){
//            alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrder.visibility = View.VISIBLE
//            changeTotalValue = true
//        }else{
//            /*This check basically helps separate between product orders and custom orders
//            Custom orders (Parcel,Medicine,Custom) are right now only one per each OrderItemMain
//            so we check when the products size is 1 and also there are 4(Product Item, Parcel,
//            Custom Order, Medicine) booleans in the orderItemMain
//            class which determines the type of product it is
//             */
//            if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
//                //In case of custom orders the total value is easily editable and mandatory to be edited
//                // be cause the admins later decide the price of the order
//                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.VISIBLE
//                changeTotalValue = true
//                when {
//                    orderItemMain.products[0].parcel_item -> {
//                        parcelOrder = true
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
//                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "কুরিয়ার নেম"
//                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text)
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text_2)
//                    }
//                    orderItemMain.products[0].medicine_item -> {
//                        medicineOrder = true
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ঔষোধ"
//                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "ফার্মেসি "
//                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text)
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text_2)
//                    }
//                    orderItemMain.products[0].custom_order_item -> {
//                        customOrder = true
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
//                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].custom_order_text)
//                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
//                    }
//                }
//            }else{
//                // in shop order the total value will not be editable and the title and details field will be empty
//                shopOrder = true
//                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.GONE
//                alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
//                alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.visibility = View.GONE
//            }
//        }
//        alertDialogToEditOrderDetailsView.saveNowEditOrderButton.setOnClickListener {
//            alertDialogToEditOrderDetails.dismiss()
//            val updateOrderItem = HashMap<String,Any>()
//            if(orderItemMain.pickDropOrder){
//
//            }else{
//                updateOrderItem["userName"] = alertDialogToEditOrderDetailsView.nameEdittextEditOrder.text.toString()
//                updateOrderItem["userNumber"] = alertDialogToEditOrderDetailsView.phoneEdittextEditOrder.text.toString()
//                updateOrderItem["userAddress"] = alertDialogToEditOrderDetailsView.addressEdittextEditOrder.text.toString()
//                updateOrderItem["userNote"] = alertDialogToEditOrderDetailsView.noteEdittextEditOrder.text.toString()
//            }
//            updateOrderItem["daCharge"] = alertDialogToEditOrderDetailsView.daChargeTotalEditOrder.text.toString().toInt()
//            updateOrderItem["deliveryCharge"] = alertDialogToEditOrderDetailsView.deliveryChargeTotalEditOrder.text.toString().toInt()
//            updateOrderItem["cancelledOrderReasonFromAdmin"] = alertDialogToEditOrderDetailsView.cancellationReasonEdittext.text.toString()
//            updateOrderItem["adminOrderNote"] = alertDialogToEditOrderDetailsView.adminNoteEdittextEditOrder.text.toString()
//            val products = ArrayList<CartProductEntity>()
//            if(orderItemMain.products.isNotEmpty()){
//                for(itemPrd in orderItemMain.products){
//                    products.add(itemPrd.copy())
//                }
//            }
//            if(changeTotalValue){
//                updateOrderItem["totalPrice"] = alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrder.text.toString().toInt()
//            }
//            if(parcelOrder){
//                products[0].parcel_order_text = alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.text.toString()
//                products[0].parcel_order_text_2 = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
//                updateOrderItem["products"] = products
//            }
//            if(medicineOrder){
//                products[0].medicine_order_text = alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.text.toString()
//                products[0].medicine_order_text_2 = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
//                updateOrderItem["products"] = products
//            }
//            if(customOrder){
//                products[0].custom_order_text = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
//                updateOrderItem["products"] = products
//            }
//            FirebaseFirestore.getInstance().collection("users")
//                .document(customerId)
//                .collection("users_order_collection")
//                .document(orderId)
//                .update(updateOrderItem)
//        }
//        alertDialogToEditOrderDetails.setView(alertDialogToEditOrderDetailsView)
//        alertDialogToEditOrderDetails.show()
    }
}