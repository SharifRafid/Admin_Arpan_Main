package admin.arpan.delivery.ui.order

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.home.HomeViewModel
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.callPermissionCheck
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.*
import kotlinx.android.synthetic.main.dialog_ask_password.view.*
import kotlinx.android.synthetic.main.dialog_force_change_order_status.*
import kotlinx.android.synthetic.main.dialog_force_change_order_status.view.*
import kotlinx.android.synthetic.main.edit_order_item.view.*
import kotlinx.android.synthetic.main.fragment_order_history_copy.view.*
import kotlinx.android.synthetic.main.product_image_big_view.view.*
import kotlinx.android.synthetic.main.product_image_big_view.view.imageView
import java.io.IOException
import java.lang.StringBuilder

class OrderHistoryFragmentNew : DialogFragment() {

    private val mainShopItemArrayList = ArrayList<MainShopCartItem>()
    private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
    private val allOrderItemArrayList = ArrayList<MainShopCartItem>()
    private lateinit var productRecyclerViewAdapter : OrderProductItemRecyclerAdapter
    private lateinit var progressDialog : Dialog
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var orderId = ""
    private var customerId = ""
    private var listenerRegistration : ListenerRegistration? = null
    private var eventListener : EventListener<DocumentSnapshot>? = null
    private var orderItemMain : OrderItemMain? = null
    private lateinit var homeViewModel : HomeViewModel
    private var currentCalc = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_history_copy, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_AdminArpan)
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
        progressDialog = requireContext().createProgressDialog()
        productRecyclerViewAdapter = OrderProductItemRecyclerAdapter(requireContext(), mainShopItemHashMap)
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
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        orderId = arguments?.getString("orderID").toString()
        customerId = arguments?.getString("customerId").toString()
        view.backButtonImageView.setOnClickListener {
            dismiss()
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
        setLogicForImageButtonsOnSecondRow(view, orderItemMain!!)
        setLogicForOrderStatusOnThirdRow(view, orderItemMain!!)
        setLogicForCardsAndCountsOnStatistics(view, orderItemMain!!)
        setLogicForCheckingDaStatusAndPlaceDaName(view, orderItemMain!!)
        setTextOnRestOrderDetailsTextView(view, orderItemMain!!)
        setLogicForPlacingOrderItemDetailsAndSeparatingProducts(view, orderItemMain!!)

        view.orderHistoryProgressBarContainer.visibility = View.GONE
        view.noDataFoundLinearLayoutContainer.visibility = View.GONE
        view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
    }

    private fun initDefaultViewStates(view: View) {
        view.callPickDropRecieverButton.visibility = View.GONE
        view.orderImagePicture.visibility = View.GONE
        view.assignedDaContainerLinearLayout.visibility = View.GONE
        view.customOrderTitleTextViewMain.visibility = View.GONE
        view.customOrderDetailTextViewMain.visibility = View.GONE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.GONE
    }

    private fun setLogicForPlacingOrderItemDetailsAndSeparatingProducts(view: View, orderItemMain: OrderItemMain) {
        if(orderItemMain.pickDropOrder){
            initPickDropDataLogicAndPlaceDataOnUi(view, orderItemMain)
            }else{
            if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
                if(orderItemMain.products[0].parcel_item){
                    placeParcelItemData(view, orderItemMain)
                }else if(orderItemMain.products[0].medicine_item){
                    placeMedicineItemData(view, orderItemMain)
                }else if(orderItemMain.products[0].custom_order_item){
                    placeCustomOrderItemData(view, orderItemMain)
                }
            }else{
                workWithTheArrayList(orderItemMain.products, view)
            }
        }
    }

    private fun placeCustomOrderItemData(view: View, orderItemMain: OrderItemMain) {
        view.customOrderTitleTextViewMain.visibility = View.VISIBLE
        view.customOrderDetailTextViewMain.visibility = View.VISIBLE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.GONE

        if(orderItemMain.adminOrder){
            view.customOrderTitleTextViewMain.text = "অ্যাডমিন অর্ডার"
        }else{
            view.customOrderTitleTextViewMain.text = "কাস্টম অর্ডার"
        }
        view.customOrderDetailTextViewMain.text = "ডিটেইলসঃ "+ orderItemMain.products[0].custom_order_text

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
        view.customOrderTitleTextViewMain.visibility = View.VISIBLE
        view.customOrderDetailTextViewMain.visibility = View.VISIBLE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.GONE

        view.customOrderTitleTextViewMain.text = "মেডিসিন অর্ডার"
        view.customOrderDetailTextViewMain.text = "ফার্মেসিঃ "+
                orderItemMain.products[0].medicine_order_text + "\n\n" +
                "ঔষধঃ "+ orderItemMain.products[0].medicine_order_text_2

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
        view.customOrderTitleTextViewMain.visibility = View.VISIBLE
        view.customOrderDetailTextViewMain.visibility = View.VISIBLE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.GONE

        view.customOrderTitleTextViewMain.text = "পার্সেল অর্ডার"
        view.customOrderDetailTextViewMain.text = "কুরিয়ার নেমঃ "+
                orderItemMain.products[0].parcel_order_text + "\n\n" +
                "ডিটেইলসঃ "+ orderItemMain.products[0].parcel_order_text_2

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
        view.customOrderTitleTextViewMain.visibility = View.VISIBLE
        view.customOrderDetailTextViewMain.visibility = View.VISIBLE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.GONE

        view.customOrderTitleTextViewMain.text = "পিক-আপ এন্ড ড্রপ অর্ডার"

        view.customOrderDetailTextViewMain.text = StringBuilder()
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
            .append("\n")
            .append("পার্সেলের তথ্যঃ ")
            .append("\n")
            .append(orderItemMain.pickDropOrderItem.parcelDetails)

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
    private fun setOrderImageOnView(view: View, firebaseStorage: StorageReference) {
        Glide.with(requireActivity())
            .load(firebaseStorage)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerInside()
            .placeholder(R.drawable.loading_image_glide)
            .into(view.orderImagePicture)

        view.orderImagePicture.setOnClickListener {
            val dialog = AlertDialog.Builder(context, R.style.Theme_AdminArpan).create()
            val view2 = LayoutInflater.from(context).inflate(R.layout.product_image_big_view,null)
            view2.floatingActionButton.setOnClickListener{
                dialog.dismiss()
            }
            Glide.with(requireContext())
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
        view.customOrderTitleTextViewMain.visibility = View.GONE
        view.customOrderDetailTextViewMain.visibility = View.GONE
        view.shopProductsOrderItemRecyclerViewMain.visibility = View.VISIBLE

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
            currentCalc = 0
            fillUpShopDetailsValueInMainShopItemList(view)
        }
    }
    private fun fillUpShopDetailsValueInMainShopItemList(view: View) {
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
            .document(mainShopItemHashMap[currentCalc].shop_doc_id)
            .get().addOnSuccessListener { document ->
                mainShopItemHashMap[currentCalc].shop_details =
                    if(document.data == null){
                        ShopItem(
                            key = "",
                            name = "SHOP DELETED",
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
                    // The data is downloaded all of those
                    view.shopProductsOrderItemRecyclerViewMain.layoutManager = LinearLayoutManager(requireContext())
                    view.shopProductsOrderItemRecyclerViewMain.adapter = productRecyclerViewAdapter
                }else{
                    currentCalc ++
                    fillUpShopDetailsValueInMainShopItemList(view)
                }
            }
    }

    private fun setTextOnRestOrderDetailsTextView(view: View, orderItemMain: OrderItemMain) {
        val orderDetails = StringBuilder()

        if(orderItemMain.promoCodeApplied){
            orderDetails.append("Promo Applied : "+ orderItemMain.promoCode.promoCodeName)
            orderDetails.append("\n")
        }
        orderDetails.append("Payment Via : "+ orderItemMain.paymentMethod)
        orderDetails.append("\n")
        orderDetails.append("Note : "+ orderItemMain.userNote)
        orderDetails.append("\n")

        view.orderDetailsMainTitleTextView.text = orderDetails
    }

    private fun setLogicForCheckingDaStatusAndPlaceDaName(view: View, orderItemMain: OrderItemMain) {
        if(orderItemMain.daID.isNotEmpty()){
            view.assignedDaContainerLinearLayout.visibility = View.VISIBLE
            view.assignedDaNameTextView.text = orderItemMain.daDetails.da_name
            if(orderItemMain.daDetails.da_bkash.isNotEmpty()){
                view.callDaBkashNowImageButton.visibility = View.VISIBLE
            }else{
                view.callDaBkashNowImageButton.visibility = View.GONE
            }
            view.callDaNowImageButton.setOnClickListener {
                if (callPermissionCheck(requireContext(), requireActivity())) {
                    val callIntent = Intent(
                        Intent.ACTION_CALL,
                        Uri.parse("tel:" + orderItemMain.daDetails.da_mobile)
                    )
                    startActivity(callIntent)
                }
            }
            view.callDaBkashNowImageButton.setOnClickListener {
                if (callPermissionCheck(requireContext(), requireActivity())) {
                    val callIntent = Intent(
                        Intent.ACTION_CALL,
                        Uri.parse("tel:" + orderItemMain.daDetails.da_bkash)
                    )
                    startActivity(callIntent)
                }
            }
        }else{
            view.assignedDaContainerLinearLayout.visibility = View.GONE
        }
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
    private fun showAssignOrderToDaListDialog(view: View, orderItemMain: OrderItemMain) {
        val alertDialogForDa = AlertDialog.Builder(context).create()
        val alertDialogForDaView = LayoutInflater.from(context).inflate(R.layout.assign_da_list_view, null)
        val arrayListDaStatus = homeViewModel.getDaStatusList()
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
            for(order in homeViewModel.getOrderItemMainList()){
                if(order.daID == daStatus.key){
                    when(order.orderStatus){
                        "VERIFIED" -> daAssigned += 1
                        "PROCESSING" -> daProcessing += 1
                        "PICKED UP" -> daProcessing += 1
                        "COMPLETED" -> daCompleted += 1
                    }
                }
            }
            daListItem += "[$daProcessing] [$daCompleted]"
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
    private fun forceAssignDaNow(view: View, orderItemMain: OrderItemMain) {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to reassign this order?")
            .setMessage("By clicking yes you'll be able to reassign the order to another da and the order status will be set to VERIFIED")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                showAssignOrderToDaListDialogAndChangeOrderStatus(view, orderItemMain)
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
            .create().show()
    }
    private fun showAssignOrderToDaListDialogAndChangeOrderStatus(view: View, orderItemMain: OrderItemMain) {
        val alertDialogForDa = AlertDialog.Builder(context).create()
        val alertDialogForDaView = LayoutInflater.from(context).inflate(R.layout.assign_da_list_view, null)
        val arrayListDaStatus = homeViewModel.getDaStatusList()
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
            for(order in homeViewModel.getOrderItemMainList()){
                if(order.daID == daStatus.key){
                    when(order.orderStatus){
                        "VERIFIED" -> daAssigned += 1
                        "PROCESSING" -> daProcessing += 1
                        "PICKED UP" -> daProcessing += 1
                        "COMPLETED" -> daCompleted += 1
                    }
                }
            }
            daListItem += "[$daProcessing] [$daCompleted]"
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
                        daDetails["orderStatus"] = "VERIFIED"
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

    private fun setLogicForOrderStatusOnThirdRow(view: View, orderItemMain: OrderItemMain) {
        view.orderStatusMainTextView.text = orderItemMain.orderStatus
        when(orderItemMain.orderStatus){
            "PENDING" -> {
                view.changeOrderStatusButton.visibility = View.VISIBLE
                view.changeOrderStatusButton2.visibility = View.VISIBLE
                view.changeOrderStatusButton.setBackgroundColor(Color.parseColor("#FA831B"))
                view.orderStatusMainTextView.setTextColor(Color.parseColor("#262626"))
                view.changeOrderStatusButton.text = "VERIFY"
                view.changeOrderStatusButton2.text = "CANCEL"
                view.changeOrderStatusButton.setOnClickListener {
                    verifyUsersOrder(view, orderItemMain)
                }
                view.changeOrderStatusButton2.setOnClickListener {
                    cancelOrderItem(view, orderItemMain)
                }
            }
            "VERIFIED" -> {
                view.changeOrderStatusButton.visibility = View.VISIBLE
                view.changeOrderStatusButton2.visibility = View.VISIBLE
                view.changeOrderStatusButton2.text = "CANCEL"
                view.changeOrderStatusButton.setBackgroundColor(Color.parseColor("#ED9D34"))
                view.orderStatusMainTextView.setTextColor(Color.parseColor("#FA831B"))
                view.changeOrderStatusButton.setOnClickListener {
                    showAssignOrderToDaListDialog(view, orderItemMain)
                }
                if(orderItemMain.daID.isNotEmpty()){
                    view.orderStatusMainTextView.text = "Assign Request Sent To "+orderItemMain.daDetails.da_name
                    view.changeOrderStatusButton.text = "Assign Again"
                    view.changeOrderStatusButton2.setOnClickListener {
                        removeDaFromThisOrder(view, orderItemMain)
                    }
                }else{
                    view.changeOrderStatusButton.text = "Assign DA"
                    view.changeOrderStatusButton2.setOnClickListener {
                        cancelOrderItem(view, orderItemMain)
                    }
                }
            }
            "PROCESSING" -> {
                view.changeOrderStatusButton.visibility = View.GONE
                view.changeOrderStatusButton2.visibility = View.GONE
                view.changeOrderStatusButton.setBackgroundColor(Color.parseColor("#ED9D34"))
                view.orderStatusMainTextView.setTextColor(Color.parseColor("#ED9D34"))
            }
            "PICKED UP" -> {
                view.changeOrderStatusButton.visibility = View.GONE
                view.changeOrderStatusButton2.visibility = View.GONE
                view.changeOrderStatusButton.setBackgroundColor(Color.parseColor("#ED9D34"))
                view.orderStatusMainTextView.setTextColor(Color.parseColor("#ED9D34"))
            }
            "COMPLETED" -> {
                view.changeOrderStatusButton.visibility = View.GONE
                view.changeOrderStatusButton2.visibility = View.GONE
                if(orderItemMain.orderCompletedStatus=="CANCELLED"){
                    view.orderStatusMainTextView.text = "CANCELLED"
                    view.orderStatusMainTextView.setTextColor(Color.parseColor("#EA594D"))
                }else{
                    view.orderStatusMainTextView.setTextColor(Color.parseColor("#43A047"))
                }
            }
        }
    }

    private fun cancelOrderItem(view: View, orderItemMain: OrderItemMain) {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to cancel this order?")
            .setMessage("By clicking yes you're cancelling this order")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                progressDialog.show()
                val hashMap = HashMap<String,Any>()
                hashMap["orderStatus"] = "COMPLETED"
                hashMap["orderCompletedStatus"] = "CANCELLED"
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
                FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId)
                    .update("orderStatus", "VERIFIED")
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

    private fun setLogicForCardsAndCountsOnStatistics(view: View, orderItemMain: OrderItemMain) {
        view.orderTotalPrice.text = (orderItemMain.totalPrice + orderItemMain.deliveryCharge).toString()
        view.orderDeliveryPrice.text = orderItemMain.deliveryCharge.toString()
        view.orderDaPrice.text = orderItemMain.daCharge.toString()
        var arpanProfit = orderItemMain.deliveryCharge - orderItemMain.daCharge
        for(productItem in orderItemMain.products){
            arpanProfit += (productItem.product_arpan_profit*productItem.product_item_amount)
        }
        view.orderArpanChargePrice.text = arpanProfit.toString()
    }
    private fun setLogicForImageButtonsOnSecondRow(view: View, orderItemMain: OrderItemMain) {
        view.callUserNowImageButton.setOnClickListener {
            if (callPermissionCheck(requireContext(), requireActivity())) {
                val callIntent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + orderItemMain.userNumber)
                )
                startActivity(callIntent)
            }
        }
        view.callUserAccountNumberNowImageButton.setOnClickListener {
            if (callPermissionCheck(requireContext(), requireActivity())) {
                val callIntent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + orderItemMain.userPhoneAccount)
                )
                startActivity(callIntent)
            }
        }
        if (orderItemMain.lattitude.isNotEmpty() && orderItemMain.longtitude.isNotEmpty()) {
            if (orderItemMain.lattitude.trim().toFloat() != 0f && orderItemMain.longtitude.trim()
                    .toFloat() != 0f
            ) {
                view.showUsersLocationNowButton.visibility = View.VISIBLE
                view.showUsersLocationNowButton.setOnClickListener {
                    val navigationIntentUri = Uri.parse(
                        "google.navigation:q=" + orderItemMain.lattitude.trim()
                            .toFloat() + "," + orderItemMain.longtitude.trim().toFloat()
                    )
                    val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        startActivity(mapIntent)
                    } catch (e: ActivityNotFoundException) {
                        requireContext().showToast("Google Map Not Installed", FancyToast.ERROR)
                    }
                }
            } else {
                view.showUsersLocationNowButton.visibility = View.GONE
            }
        } else {
            view.showUsersLocationNowButton.visibility = View.GONE
        }
        view.editOrderDetailsImageButton.setOnClickListener {
            showEditOrderDetailsDialog(view, orderItemMain)
        }
        view.deleteOrderItemImageButton.setOnClickListener {
            showDeleteOrderCustomDialog(view, orderItemMain)
        }
        view.forceChangeOrderStatusImageButton.setOnClickListener {

        }
        view.forceAssignDaImageButton.setOnClickListener {
            requireContext().showToast("Long Click To Force Reassign Da", FancyToast.CONFUSING)
        }
        view.forceAssignDaImageButton.setOnLongClickListener{
            forceAssignDaNow(view, orderItemMain)
            true
        }
        view.forceChangeOrderStatusImageButton.setOnClickListener {
            requireContext().showToast("Long Click To Force Change Status", FancyToast.CONFUSING)
        }
        view.forceChangeOrderStatusImageButton.setOnLongClickListener{
            forceChangeOrderStatusNow(view, orderItemMain)
            true
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

    private fun showDeleteOrderCustomDialog(view: View, orderItemMain: OrderItemMain) {
        val alertDialogToDeleteUserData = AlertDialog.Builder(requireContext()).create()
        val alertDialogToDeleteUserDataView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ask_password, null)
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
                            requireContext().showToast("Wrong Password", FancyToast.ERROR)
                            alertDialogToDeleteUserDataView.deleteOrderItemMainDialogButton.isEnabled = true
                        }
                    }
            }else{
                requireContext().showToast("Cannot be empty", FancyToast.ERROR)
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
                    requireContext().showToast("Delete Success", FancyToast.ERROR)
                    dismiss()
                }else{
                    requireContext().showToast("Failed To Delete", FancyToast.ERROR)
                    view.orderHistoryProgressBarContainer.visibility = View.GONE
                    view.noDataFoundLinearLayoutContainer.visibility = View.GONE
                    view.mainOrderDetailsDataContainerLinearLayout.visibility = View.VISIBLE
                }
            }
    }

    private fun showEditOrderDetailsDialog(view: View, orderItemMain: OrderItemMain) {
        val alertDialogToEditOrderDetails = AlertDialog.Builder(requireContext()).create()
        val alertDialogToEditOrderDetailsView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_order_item, null)

        // Setting the prefetched or previous values of the order item
        alertDialogToEditOrderDetailsView.nameEdittextEditOrder.setText(orderItemMain.userName)
        alertDialogToEditOrderDetailsView.phoneEdittextEditOrder.setText(orderItemMain.userNumber)
        alertDialogToEditOrderDetailsView.addressEdittextEditOrder.setText(orderItemMain.userAddress)
        alertDialogToEditOrderDetailsView.noteEdittextEditOrder.setText(orderItemMain.userNote)
        alertDialogToEditOrderDetailsView.deliveryChargeTotalEditOrder.setText(orderItemMain.deliveryCharge.toString())
        alertDialogToEditOrderDetailsView.daChargeTotalEditOrder.setText(orderItemMain.daCharge.toString())

        //The total value should only be changed in case of custom orders and pickup and drops
        // this field will not be enabled for shop orders because that might mess things up
        // at least for now
        var changeTotalValue = false

        // Variables required for making the order item separation easier
        var customOrder = false
        var medicineOrder = false
        var parcelOrder = false
        var shopOrder = false

        // Pick up and drop remains in a entirely separate category than the other shops and
        // Custom orders, so this check is for that
        if(orderItemMain.pickDropOrder){
            alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrder.visibility = View.VISIBLE
            changeTotalValue = true
        }else{
            /*This check basically helps separate between product orders and custom orders
            Custom orders (Parcel,Medicine,Custom) are right now only one per each OrderItemMain
            so we check when the products size is 1 and also there are 4(Product Item, Parcel,
            Custom Order, Medicine) booleans in the orderItemMain
            class which determines the type of product it is
             */
            if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
                //In case of custom orders the total value is easily editable and mandatory to be edited
                // be cause the admins later decide the price of the order
                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.VISIBLE
                changeTotalValue = true
                when {
                    orderItemMain.products[0].parcel_item -> {
                        parcelOrder = true
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "কুরিয়ার নেম"
                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text)
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text_2)
                    }
                    orderItemMain.products[0].medicine_item -> {
                        medicineOrder = true
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ঔষোধ"
                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "ফার্মেসি "
                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text)
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text_2)
                    }
                    orderItemMain.products[0].custom_order_item -> {
                        customOrder = true
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
                        alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].custom_order_text)
                        alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
                    }
                }
            }else{
                // in shop order the total value will not be editable and the title and details field will be empty
                shopOrder = true
                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.GONE
                alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
                alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.visibility = View.GONE
            }
        }
        alertDialogToEditOrderDetailsView.saveNowEditOrderButton.setOnClickListener {
            alertDialogToEditOrderDetails.dismiss()
            val updateOrderItem = HashMap<String,Any>()
            updateOrderItem["userName"] = alertDialogToEditOrderDetailsView.nameEdittextEditOrder.text.toString()
            updateOrderItem["userNumber"] = alertDialogToEditOrderDetailsView.phoneEdittextEditOrder.text.toString()
            updateOrderItem["userAddress"] = alertDialogToEditOrderDetailsView.addressEdittextEditOrder.text.toString()
            updateOrderItem["userNote"] = alertDialogToEditOrderDetailsView.noteEdittextEditOrder.text.toString()
            updateOrderItem["daCharge"] = alertDialogToEditOrderDetailsView.daChargeTotalEditOrder.text.toString().toInt()
            updateOrderItem["deliveryCharge"] = alertDialogToEditOrderDetailsView.deliveryChargeTotalEditOrder.text.toString().toInt()
            val products = ArrayList<CartProductEntity>()
            products.add(orderItemMain.products[0].copy())
            if(changeTotalValue){
                updateOrderItem["totalPrice"] = alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrder.text.toString().toInt()
            }
            if(parcelOrder){
                products[0].parcel_order_text = alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.text.toString()
                products[0].parcel_order_text_2 = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
            }
            if(medicineOrder){
                products[0].medicine_order_text = alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.text.toString()
                products[0].medicine_order_text_2 = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
            }
            if(customOrder){
                products[0].custom_order_text = alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.text.toString()
            }
            updateOrderItem["products"] = products
            FirebaseFirestore.getInstance().collection("users")
                .document(customerId)
                .collection("users_order_collection")
                .document(orderId)
                .update(updateOrderItem)
        }
        alertDialogToEditOrderDetails.setView(alertDialogToEditOrderDetailsView)
        alertDialogToEditOrderDetails.show()
    }

    private fun setTextOnTextViewsOnMainUi(view: View, orderItemMain: OrderItemMain) {
        view.orderIdTextView.text = orderItemMain.orderId

        val userDetailsString = StringBuilder()
        userDetailsString.append("Name : ")
        userDetailsString.append(orderItemMain.userName)
        userDetailsString.append("\n")
        userDetailsString.append("Address : ")
        userDetailsString.append(orderItemMain.userAddress)
        userDetailsString.append("\n")
        userDetailsString.append("Selected Location : ")
        userDetailsString.append(orderItemMain.locationItem.locationName)
        userDetailsString.append("\n")
        userDetailsString.append("Phone : ")
        userDetailsString.append(orderItemMain.userNumber)
        userDetailsString.append("\n")
        userDetailsString.append("Account Phone : ")
        userDetailsString.append(orderItemMain.userPhoneAccount)
        userDetailsString.append(" (PRIVATE)\n")

        view.orderUserDetailsTextView.text = userDetailsString.toString()
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
            .url("https://arpan-fcm.herokuapp.com/send-order-status-changed-notification")
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
        val request: Request = Request.Builder()
            .url("https://arpan-fcm.herokuapp.com/send-notification-to-da-about-a-new-order-that-he-recieved")
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
}