package admin.arpan.delivery.ui.order

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.callPermissionCheck
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.fragment_order_history.view.*
import kotlinx.android.synthetic.main.fragment_order_history.view.txtAllPrice
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrderHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderHistoryFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val mainCartCustomObjectHashMap = HashMap<String, ArrayList<CartProductEntity>>()
    private val mainShopItemHashMap = ArrayList<MainShopCartItem>()
    private lateinit var productRecyclerViewAdapter : OrderProductItemRecyclerAdapter
    private lateinit var progressDialog : Dialog
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var currentCalc = 0
    private var priceTotal = 0
    private var deliveryCharge = 0
    private var promoCodeActive = false
    private var promoCode = PromoCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OrderHistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OrderHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initVars(view)
        val orderId = arguments?.getString("orderID").toString()
        val customerId = arguments?.getString("customerId").toString()
        if(FirebaseAuth.getInstance().currentUser == null){
            view.shopsProgress.visibility = View.GONE
            view.noProductsText.visibility = View.VISIBLE
            view.mainLayout.visibility = View.GONE
        }else{
            FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId)
                    .get().addOnCompleteListener { it1 ->
                    if(it1.isSuccessful){
                            if(it1.result!!.exists()){
                                val orderItemMain = it1.result!!.toObject(OrderItemMain::class.java) as OrderItemMain
                                if(orderItemMain.pickDropOrder){
                                    view.nestedScrollView.visibility = View.GONE
                                    view.pickDropScrollView.visibility = View.VISIBLE
                                    view.text_name_container.visibility = View.GONE
                                    view.text_number_container.visibility = View.GONE
                                    view.txt_number.visibility = View.GONE
                                    orderItemMain.userAddress = "From "+orderItemMain.pickDropOrderItem.senderLocation+"To "+orderItemMain.pickDropOrderItem.recieverLocation
                                }else{
                                    view.text_number_container.visibility = View.VISIBLE
                                    view.text_name_container.visibility = View.VISIBLE
                                    view.txt_number.visibility = View.VISIBLE
                                    view.nestedScrollView.visibility = View.VISIBLE
                                    view.pickDropScrollView.visibility = View.GONE
                                    workWithTheArrayList(orderItemMain.products, view)
                                    if(orderItemMain.userAddress.isEmpty()){
                                        view.text_address_container.visibility = View.GONE
                                    }else{
                                        view.text_address_container.visibility = View.VISIBLE
                                        view.txt_address.setText(orderItemMain.userAddress)
                                    }
                                }
                                view.edt_name.setText(orderItemMain.pickDropOrderItem.senderName)
                                view.edt_mobile.setText(orderItemMain.pickDropOrderItem.senderPhone)
                                view.edt_address.setText(orderItemMain.pickDropOrderItem.senderAddress)
                                view.edt_aboutParcel.setText(orderItemMain.pickDropOrderItem.parcelDetails)
                                view.edt_name_reciver.setText(orderItemMain.pickDropOrderItem.recieverName)
                                view.edt_mobile_reciver.setText(orderItemMain.pickDropOrderItem.recieverPhone)
                                view.edt_address_reciver.setText(orderItemMain.pickDropOrderItem.recieverAddress)
                                priceTotal = orderItemMain.totalPrice
                                deliveryCharge = orderItemMain.deliveryCharge
                                promoCodeActive = orderItemMain.promoCodeApplied
                                promoCode = orderItemMain.promoCode
                                setPriceTotalOnView(view)
                                view.orderIDText.text = orderItemMain.orderId
                                view.orderStatusText2.text = orderItemMain.orderStatus
                                if(orderItemMain.orderStatus == "PENDING"){
                                    progressDialog.show()
                                    view.button.text = "ASSIGN"
                                    view.button.setOnClickListener {
                                        val alertDialogForDa = AlertDialog.Builder(context).create()
                                        val alertDialogForDaView = LayoutInflater.from(context).inflate(R.layout.assign_da_list_view, null)
                                        val arrayListDaStatus = (activity as OrdresActivity).daStatusList
                                        val arrayListDaStatusString = ArrayList<String>()
                                        for(daStatus in arrayListDaStatus){
                                            arrayListDaStatusString.add(daStatus.name)
                                        }
                                        alertDialogForDaView.listView.adapter = ArrayAdapter(requireContext(),R.layout.custom_spinner_item_view, arrayListDaStatusString)
                                        alertDialogForDa.setView(alertDialogForDaView)
                                        alertDialogForDaView.listView.setOnItemClickListener { parent, view, position, id ->
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
                                                        daDetails["orderStatus"] = "APPROVED"
                                                        FirebaseFirestore.getInstance().collection("users")
                                                            .document(customerId)
                                                            .collection("users_order_collection")
                                                            .document(orderId)
                                                            .update(daDetails)
                                                            .addOnCompleteListener {_->
                                                                (activity as OrdresActivity).ordersMainOldItemsArrayList[(activity as OrdresActivity).mainItemPositionsRecyclerAdapter].orders.removeAt((activity as OrdresActivity).selectedRecyclerAdapterItem)
                                                                if((activity as OrdresActivity).ordersMainOldItemsArrayList[(activity as OrdresActivity).mainItemPositionsRecyclerAdapter].orders.size==0){
                                                                    (activity as OrdresActivity).ordersMainOldItemsArrayList.removeAt((activity as OrdresActivity).mainItemPositionsRecyclerAdapter)
                                                                    (activity as OrdresActivity).orderAdapterMain.notifyItemRemoved((activity as OrdresActivity).mainItemPositionsRecyclerAdapter)
                                                                    (activity as OrdresActivity).orderAdapterMain.notifyItemRangeChanged((activity as OrdresActivity).mainItemPositionsRecyclerAdapter, (activity as OrdresActivity).ordersMainOldItemsArrayList.size)
                                                                }else{
                                                                    (activity as OrdresActivity).orderAdapterMain.notifyItemChanged((activity as OrdresActivity).mainItemPositionsRecyclerAdapter)
                                                                }
                                                                sendNotification(
                                                                        orderItemMain.userId,
                                                                "আপনার অর্ডার ${orderItemMain.orderId} টি অ্যাপ্রুভ করা হয়েছে ।",
                                                                "আপনার অর্ডারটি অ্যাপ্রুভ করা হয়েছে, দ্রুতই অর্ডার টি আপনার কাছে পৌছে যাবে ।",
                                                                it1.result!!.id
                                                                )
                                                                sendNotificationToDa(
                                                                    orderItemMain.userId,
                                                                    it.result!!.id,
                                                                    "আপনি একটি অর্ডার ${orderItemMain.orderId} পেয়েছেন ।",
                                                                    "আপনি একটি অর্ডার পেয়েছেন দ্রুত অর্ডারটি রিসিভ করুন ।",
                                                                    it1.result!!.id
                                                                )
                                                                alertDialogForDa.dismiss()
                                                                dismiss()
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
                                }else{
                                    view.button.text = orderItemMain.orderStatus
                                }
                                view.txt_name.setText(orderItemMain.userName)
                                view.txt_number.setText(orderItemMain.userNumber)
                                view.call_now.setOnClickListener {
                                    if (callPermissionCheck(requireContext(), requireActivity())) {
                                        val callIntent = Intent(
                                            Intent.ACTION_CALL,
                                            Uri.parse("tel:" + orderItemMain.userNumber)
                                        )
                                        startActivity(callIntent)
                                    }
                                }
                                if(orderItemMain.userNote.isEmpty()){
                                    view.text_note_container.visibility = View.GONE
                                }else{
                                    view.text_note_container.visibility = View.VISIBLE
                                    view.txt_note.setText(orderItemMain.userNote)
                                }
                                if(orderItemMain.pickDropOrderItem.parcelImage.isEmpty()){
                                    view.cardViewPickDropImaage.visibility = View.GONE
                                }else{
                                    view.cardViewPickDropImaage.visibility = View.VISIBLE
                                    val storageReference = FirebaseStorage.getInstance().reference.child("ORDER_IMAGES")
                                            .child(orderItemMain.key).child(orderItemMain.pickDropOrderItem.parcelImage)

                                    Glide.with(view.context)
                                            .load(storageReference)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .centerCrop()
                                            .override(300,300)
                                            .placeholder(R.drawable.loading_image_glide).into(view.imageView)
                                }
                                view.paymentText.text = getString(R.string.payment_method) + orderItemMain.paymentMethod
                                view.shopsProgress.visibility = View.GONE
                                view.noProductsText.visibility = View.GONE
                                view.mainLayout.visibility = View.VISIBLE
                            }else{
                                view.shopsProgress.visibility = View.GONE
                                view.noProductsText.visibility = View.VISIBLE
                                view.mainLayout.visibility = View.GONE
                            }
                        }else{
                            view.shopsProgress.visibility = View.GONE
                            view.noProductsText.visibility = View.VISIBLE
                            view.mainLayout.visibility = View.GONE
                        }
                    }
        }

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


    private fun workWithTheArrayList(list: List<CartProductEntity>, view: View) {
        for(cartProductEntity in list){
            when {
                cartProductEntity.parcel_item -> {
                    mainCartCustomObjectHashMap["parcel_item"]?.add(cartProductEntity)
                }
                cartProductEntity.custom_order_item -> {
                    mainCartCustomObjectHashMap["custom_order_item"]?.add(cartProductEntity)
                }
                cartProductEntity.medicine_item -> {
                    mainCartCustomObjectHashMap["medicine_item"]?.add(cartProductEntity)
                }
                else -> {
                    mainCartCustomObjectHashMap["product_item"]?.add(cartProductEntity)
                }
            }
        }
        if(mainCartCustomObjectHashMap["product_item"]!!.isNotEmpty()){
            view.productsTextView.visibility = View.VISIBLE
            view.productsRecyclerView.visibility = View.VISIBLE
            initiateRestLogicForArrayList(view)
        }else{
            view.productsTextView.visibility = View.GONE
            view.productsRecyclerView.visibility = View.GONE
        }
        if(mainCartCustomObjectHashMap["parcel_item"]!!.isNotEmpty()){
            view.parcelOrderTextView.visibility = View.VISIBLE
            view.parcelOrderTextView2.visibility = View.VISIBLE
            view.parcelRecyclerView.visibility = View.VISIBLE
            initiateRestLogicForParcel(view)
        }else{
            view.parcelOrderTextView.visibility = View.GONE
            view.parcelOrderTextView2.visibility = View.GONE
            view.parcelRecyclerView.visibility = View.GONE
        }
        if(mainCartCustomObjectHashMap["custom_order_item"]!!.isNotEmpty()){
            view.customOrderTextView.visibility = View.VISIBLE
            view.customOrderTextView2.visibility = View.VISIBLE
            view.customOrderRecyclerView.visibility = View.VISIBLE
            initiateRestLogicForCustomOrder(view)
        }else{
            view.customOrderTextView.visibility = View.GONE
            view.customOrderTextView2.visibility = View.GONE
            view.customOrderRecyclerView.visibility = View.GONE
        }
        if(mainCartCustomObjectHashMap["medicine_item"]!!.isNotEmpty()){
            view.medicineOrderTextView.visibility = View.VISIBLE
            view.medicineOrderTextView2.visibility = View.VISIBLE
            view.medicineRecyclerView.visibility = View.VISIBLE
            initiateRestLogicForMedicine(view)
        }else{
            view.medicineOrderTextView.visibility = View.GONE
            view.medicineOrderTextView2.visibility = View.GONE
            view.medicineRecyclerView.visibility = View.GONE
        }
    }

    private fun initiateRestLogicForMedicine(view: View) {
        val cartItemRecyclerAdapter = mainCartCustomObjectHashMap["medicine_item"]?.let { OrderItemRecyclerAdapter(view.context, it) }
        view.medicineRecyclerView.layoutManager = LinearLayoutManager(view.context)
        view.medicineRecyclerView.adapter = cartItemRecyclerAdapter
    }

    private fun initiateRestLogicForCustomOrder(view: View) {
        val cartItemRecyclerAdapter = mainCartCustomObjectHashMap["custom_order_item"]?.let { OrderItemRecyclerAdapter(view.context, it) }
        view.customOrderRecyclerView.layoutManager = LinearLayoutManager(view.context)
        view.customOrderRecyclerView.adapter = cartItemRecyclerAdapter
    }

    private fun initiateRestLogicForParcel(view: View) {
        val cartItemRecyclerAdapter = mainCartCustomObjectHashMap["parcel_item"]?.let { OrderItemRecyclerAdapter(view.context, it) }
        view.parcelRecyclerView.layoutManager = LinearLayoutManager(view.context)
        view.parcelRecyclerView.adapter = cartItemRecyclerAdapter
    }

    private fun initiateRestLogicForArrayList(view: View) {
        mainShopItemHashMap.clear()
        for(cartItemEntity in mainCartCustomObjectHashMap["product_item"]!!){
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
                    if(currentCalc+1 >= mainShopItemHashMap.size){
                        // The data is downloaded all of those
                        view.productsRecyclerView.layoutManager = LinearLayoutManager(view.context)
                        view.productsRecyclerView.adapter = productRecyclerViewAdapter
                        progressDialog.dismiss()
                    }else{
                        currentCalc ++
                        fillUpShopDetailsValueInMainShopItemList(view)
                    }
                }
    }

    private fun setPriceTotalOnView(view: View) {
        view.txtAllPrice.text = getString(R.string.total_total_text)+"${priceTotal}+${deliveryCharge} " +
                "= ${priceTotal+deliveryCharge} "+getString(R.string.taka_text)
        if(promoCodeActive){
            view.promoCodeAppliedLinear.visibility = View.VISIBLE
            view.promoCodeAppliedText.text  = getString(R.string.you_got_part_1)+" "+promoCode.discountPrice+" "+getString(R.string.you_got_part_2)
            if(priceTotal <= promoCode.discountPrice){
                view.txtAllPrice.text = getString(R.string.total_total_text)+"${0}+${deliveryCharge} " +
                        "= ${0+deliveryCharge} "+getString(R.string.taka_text)
            }else{
                view.txtAllPrice.text = getString(R.string.total_total_text)+
                        "${priceTotal - promoCode.discountPrice}+${deliveryCharge} " +
                        "= ${priceTotal - promoCode.discountPrice+deliveryCharge} "+
                        getString(R.string.taka_text)
            }
        }else{
            view.promoCodeAppliedLinear.visibility = View.GONE
        }
    }

    private fun initVars(view: View) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        progressDialog = view.context.createProgressDialog()
        productRecyclerViewAdapter = OrderProductItemRecyclerAdapter(view.context, mainShopItemHashMap)
        mainCartCustomObjectHashMap["product_item"] = ArrayList()
        mainCartCustomObjectHashMap["parcel_item"] = ArrayList()
        mainCartCustomObjectHashMap["custom_order_item"] = ArrayList()
        mainCartCustomObjectHashMap["medicine_item"] = ArrayList()
    }

    override fun onResume() {
        super.onResume()
    }
}