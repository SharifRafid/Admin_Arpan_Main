package admin.arpan.delivery.ui.order

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.OrderItemRecyclerAdapter
import admin.arpan.delivery.db.adapter.OrderProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.home.HomeActivity
import admin.arpan.delivery.ui.home.HomeViewModel
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.callPermissionCheck
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.assign_da_list_view.view.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.*
import kotlinx.android.synthetic.main.edit_order_item.view.*
import kotlinx.android.synthetic.main.fragment_order_history.view.*
import kotlinx.android.synthetic.main.fragment_order_history.view.imageView
import kotlinx.android.synthetic.main.fragment_order_history.view.orderIDText
import kotlinx.android.synthetic.main.fragment_order_history.view.txtAllPrice
import kotlinx.android.synthetic.main.product_image_big_view.view.*
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
    private var orderItemMain = OrderItemMain()
    private var orderId = ""
    private var customerId = ""
    private lateinit var homeViewModel: HomeViewModel
    private var listenerRegistration : ListenerRegistration? = null
    private var eventListener : EventListener<DocumentSnapshot>? = null

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
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        orderId = arguments?.getString("orderID").toString()
        customerId = arguments?.getString("customerId").toString()

        val eventListener: EventListener<DocumentSnapshot> =
            EventListener<DocumentSnapshot> { snapshot, e ->
                e?.printStackTrace()
                progressDialog.dismiss()
                if (snapshot != null && snapshot.exists()) {
                    workWithTheDocumentData(view, snapshot)
                }else{
                    view.shopsProgress.visibility = View.GONE
                    view.noProductsText.visibility = View.VISIBLE
                    view.mainLayout.visibility = View.GONE
                }
            }

        if(FirebaseAuth.getInstance().currentUser == null){
            view.shopsProgress.visibility = View.GONE
            view.noProductsText.visibility = View.VISIBLE
            view.mainLayout.visibility = View.GONE
        }else{
//            progressDialog.show()
//            FirebaseFirestore.getInstance().collection("users")
//                    .document(customerId)
//                    .collection("users_order_collection")
//                    .document(orderId)
//                    .get().addOnCompleteListener { it1 ->
//                    progressDialog.dismiss()
//                    if(it1.isSuccessful){
//                            if(it1.result!!.exists()){
//                                orderItemMain = it1.result!!.toObject(OrderItemMain::class.java) as OrderItemMain
//                                if(orderItemMain.pickDropOrder){
//                                    view.customOrderLinearLayout.visibility = View.GONE
//                                    view.nestedScrollView.visibility = View.GONE
//                                    view.pickDropScrollView.visibility = View.VISIBLE
//                                    view.text_name_container.visibility = View.GONE
//                                    view.text_number_container.visibility = View.GONE
//                                    view.edt_name.setText(orderItemMain.pickDropOrderItem.senderName)
//                                    view.edt_mobile.setText(orderItemMain.pickDropOrderItem.senderPhone)
//                                    view.edt_address.setText(orderItemMain.pickDropOrderItem.senderAddress)
//                                    view.edt_aboutParcel.setText(orderItemMain.pickDropOrderItem.parcelDetails)
//                                    view.edt_name_reciver.setText(orderItemMain.pickDropOrderItem.recieverName)
//                                    view.edt_mobile_reciver.setText(orderItemMain.pickDropOrderItem.recieverPhone)
//                                    view.edt_address_reciver.setText(orderItemMain.pickDropOrderItem.recieverAddress)
//                                    view.txt_number.visibility = View.GONE
//                                    orderItemMain.userAddress = "From "+orderItemMain.pickDropOrderItem.senderLocation+"To "+orderItemMain.pickDropOrderItem.recieverLocation
//                                }else{
//                                    view.text_number_container.visibility = View.VISIBLE
//                                    view.text_name_container.visibility = View.VISIBLE
//                                    view.txt_number.visibility = View.VISIBLE
//                                    if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
//                                        view.customOrderLinearLayout.visibility = View.VISIBLE
//                                        view.nestedScrollView.visibility = View.GONE
//                                        view.pickDropScrollView.visibility = View.GONE
//                                        if(orderItemMain.products[0].parcel_item){
//                                            view.titleCustomOrderLinear.text = "পার্সেল অর্ডার"
//                                            view.customOrderTitleTextView.text = "কুরিয়ার নেমঃ "+ orderItemMain.products[0].parcel_order_text
//                                            view.customOrderDetailsTextView.text = "ডিটেইলসঃ "+ orderItemMain.products[0].parcel_order_text_2
//                                            if(orderItemMain.products[0].parcel_order_image.isNotEmpty()){
//                                                val firebaseStorage = FirebaseStorage.getInstance()
//                                                    .reference.child("ORDER_IMAGES")
//                                                    .child(orderItemMain.key)
//                                                    .child(orderItemMain.products[0].parcel_order_image)
//
//                                                Glide.with(requireContext())
//                                                    .load(firebaseStorage)
//                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                    .centerInside()
//                                                    .placeholder(R.drawable.loading_image_glide).into(view.shopImageItem)
//
//                                                view.shopImageItem.setOnClickListener {
//                                                    val dialog = AlertDialog.Builder(context, R.style.Theme_AdminArpan).create()
//                                                    val view2 = LayoutInflater.from(context).inflate(R.layout.product_image_big_view,null)
//                                                    view2.floatingActionButton.setOnClickListener{
//                                                        dialog.dismiss()
//                                                    }
//                                                    Glide.with(requireContext())
//                                                        .load(firebaseStorage)
//                                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                        .centerInside()
//                                                        .into(view2.imageView)
//                                                    dialog.setView(view2)
//                                                    //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                                                    dialog.show()
//                                                }
//                                            }else{
//                                                view.shopImageItem.visibility = View.GONE
//                                            }
//                                        }else if(orderItemMain.products[0].medicine_item){
//                                            view.titleCustomOrderLinear.text = "মেডিসিন অর্ডার"
//                                            view.customOrderTitleTextView.text = "ফার্মেসিঃ "+ orderItemMain.products[0].medicine_order_text
//                                            view.customOrderDetailsTextView.text = "ঔষোধঃ "+ orderItemMain.products[0].medicine_order_text_2
//                                            if(orderItemMain.products[0].medicine_order_image.isNotEmpty()){
//                                                val firebaseStorage = FirebaseStorage.getInstance()
//                                                    .reference.child("ORDER_IMAGES")
//                                                    .child(orderItemMain.key)
//                                                    .child(orderItemMain.products[0].medicine_order_image)
//
//                                                Glide.with(requireContext())
//                                                    .load(firebaseStorage)
//                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                    .centerInside()
//                                                    .placeholder(R.drawable.loading_image_glide).into(view.shopImageItem)
//
//                                                view.shopImageItem.setOnClickListener {
//
//                                                    val dialog = AlertDialog.Builder(context, R.style.Theme_AdminArpan).create()
//                                                    val view2 = LayoutInflater.from(context).inflate(R.layout.product_image_big_view,null)
//                                                    view2.floatingActionButton.setOnClickListener{
//                                                        dialog.dismiss()
//                                                    }
//                                                    Glide.with(requireContext())
//                                                        .load(firebaseStorage)
//                                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                        .centerInside()
//                                                        .into(view2.imageView)
//                                                    dialog.setView(view2)
//                                                    //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                                                    dialog.show()
//                                                }
//                                            }else{
//                                                view.shopImageItem.visibility = View.GONE
//                                            }
//                                        }else if(orderItemMain.products[0].custom_order_item){
//                                            view.titleCustomOrderLinear.text = "কাস্টম অর্ডার"
//                                            if(orderItemMain.adminOrder){
//                                                view.customOrderTitleTextView.text = "অ্যাডমিন অর্ডার"
//                                            }else{
//                                                view.customOrderTitleTextView.text = "জেনারেল অর্ডার"
//                                            }
//                                            view.customOrderDetailsTextView.text = "ডিটেইলসঃ "+ orderItemMain.products[0].custom_order_text
//                                            if(orderItemMain.products[0].custom_order_image.isNotEmpty()){
//                                                val firebaseStorage = FirebaseStorage.getInstance()
//                                                    .reference.child("ORDER_IMAGES")
//                                                    .child(orderItemMain.key)
//                                                    .child(orderItemMain.products[0].custom_order_image)
//
//                                                Glide.with(requireContext())
//                                                    .load(firebaseStorage)
//                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                    .centerInside()
//                                                    .placeholder(R.drawable.loading_image_glide).into(view.shopImageItem)
//
//                                                view.shopImageItem.setOnClickListener {
//                                                    val dialog = AlertDialog.Builder(context, R.style.Theme_AdminArpan).create()
//                                                    val view2 = LayoutInflater.from(context).inflate(R.layout.product_image_big_view,null)
//                                                    view2.floatingActionButton.setOnClickListener{
//                                                        dialog.dismiss()
//                                                    }
//                                                    Glide.with(requireContext())
//                                                        .load(firebaseStorage)
//                                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                                        .centerInside()
//                                                        .into(view2.imageView)
//                                                    dialog.setView(view2)
//                                                    //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                                                    dialog.show()
//                                                }
//                                            }else{
//                                                view.shopImageItem.visibility = View.GONE
//                                            }
//                                        }
//                                    }else{
//                                        view.nestedScrollView.visibility = View.VISIBLE
//                                        view.pickDropScrollView.visibility = View.GONE
//                                        view.customOrderLinearLayout.visibility = View.GONE
//                                        workWithTheArrayList(orderItemMain.products, view)
//                                    }
//                                }
//                                if(orderItemMain.userAddress.isEmpty()){
//                                    view.text_address_container.visibility = View.GONE
//                                }else{
//                                    view.text_address_container.visibility = View.VISIBLE
//                                    view.txt_address.setText(orderItemMain.userAddress)
//                                }
//                                priceTotal = orderItemMain.totalPrice
//                                deliveryCharge = orderItemMain.deliveryCharge
//                                promoCodeActive = orderItemMain.promoCodeApplied
//                                promoCode = orderItemMain.promoCode
//                                setPriceTotalOnView(view)
//                                view.orderIDText.text = orderItemMain.orderId
//                                if(orderItemMain.orderCompletedStatus == "CANCELLED") {
//                                    view.orderStatusText2.text = "CANCELLED"
//                                }else{
//                                    view.orderStatusText2.text = orderItemMain.orderStatus
//                                }
//                                detectOrderItemStatus(view)
//                                view.txt_name.setText(orderItemMain.userName)
//                                view.txt_number.setText(orderItemMain.userNumber)
//                                view.call_now.setOnClickListener {
//                                    if (callPermissionCheck(requireContext(), requireActivity())) {
//                                        val callIntent = Intent(
//                                            Intent.ACTION_CALL,
//                                            Uri.parse("tel:" + orderItemMain.userNumber)
//                                        )
//                                        startActivity(callIntent)
//                                    }
//                                }
//                                if(orderItemMain.userNote.isEmpty()){
//                                    view.text_note_container.visibility = View.GONE
//                                }else{
//                                    view.text_note_container.visibility = View.VISIBLE
//                                    view.txt_note.setText(orderItemMain.userNote)
//                                }
//                                if(orderItemMain.pickDropOrderItem.parcelImage.isEmpty()){
//                                    view.cardViewPickDropImaage.visibility = View.GONE
//                                }else{
//                                    view.cardViewPickDropImaage.visibility = View.VISIBLE
//                                    val storageReference = FirebaseStorage.getInstance().reference.child("ORDER_IMAGES")
//                                            .child(orderItemMain.key).child(orderItemMain.pickDropOrderItem.parcelImage)
//
//                                    Glide.with(view.context)
//                                            .load(storageReference)
//                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                            .centerCrop()
//                                            .override(300,300)
//                                            .placeholder(R.drawable.loading_image_glide).into(view.imageView)
//                                }
//                                view.paymentText.text = getString(R.string.payment_method) + orderItemMain.paymentMethod
//                                view.shopsProgress.visibility = View.GONE
//                                view.noProductsText.visibility = View.GONE
//                                view.mainLayout.visibility = View.VISIBLE
//                            }else{
//                                view.shopsProgress.visibility = View.GONE
//                                view.noProductsText.visibility = View.VISIBLE
//                                view.mainLayout.visibility = View.GONE
//                            }
//                        }else{
//                            view.shopsProgress.visibility = View.GONE
//                            view.noProductsText.visibility = View.VISIBLE
//                            view.mainLayout.visibility = View.GONE
//                        }
//                    }
            if (listenerRegistration == null ) {
                progressDialog.show()
                listenerRegistration = FirebaseFirestore.getInstance().collection("users")
                    .document(customerId)
                    .collection("users_order_collection")
                    .document(orderId).addSnapshotListener(eventListener)
            }
        }

    }

    private fun workWithTheDocumentData(view: View, snapshot: DocumentSnapshot) {
        orderItemMain = snapshot.toObject(OrderItemMain::class.java) as OrderItemMain
        orderItemMain.docID = snapshot.id

        val alertDialogToEditOrderDetails = AlertDialog.Builder(requireContext()).create()
        val alertDialogToEditOrderDetailsView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_order_item, null)

        alertDialogToEditOrderDetailsView.nameEdittextEditOrder.setText(orderItemMain.userName)
        alertDialogToEditOrderDetailsView.phoneEdittextEditOrder.setText(orderItemMain.userNumber)
        alertDialogToEditOrderDetailsView.addressEdittextEditOrder.setText(orderItemMain.userAddress)
        alertDialogToEditOrderDetailsView.noteEdittextEditOrder.setText(orderItemMain.userNote)

        alertDialogToEditOrderDetailsView.deliveryChargeTotalEditOrder.setText(orderItemMain.deliveryCharge.toString())
        alertDialogToEditOrderDetailsView.daChargeTotalEditOrder.setText(orderItemMain.daCharge.toString())

        var changeTotalValue = false
        var customOrder = false
        var medicineOrder = false
        var parcelOrder = false
        var shopOrder = false

        if(orderItemMain.pickDropOrder){
            alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrder.visibility = View.VISIBLE
            changeTotalValue = true
            view.customOrderLinearLayout.visibility = View.GONE
            view.nestedScrollView.visibility = View.GONE
            view.pickDropScrollView.visibility = View.VISIBLE
            view.text_name_container.visibility = View.GONE
            view.text_number_container.visibility = View.GONE
            view.edt_name.setText(orderItemMain.pickDropOrderItem.senderName)
            view.edt_mobile.setText(orderItemMain.pickDropOrderItem.senderPhone)
            view.edt_address.setText(orderItemMain.pickDropOrderItem.senderAddress)
            view.edt_aboutParcel.setText(orderItemMain.pickDropOrderItem.parcelDetails)
            view.edt_name_reciver.setText(orderItemMain.pickDropOrderItem.recieverName)
            view.edt_mobile_reciver.setText(orderItemMain.pickDropOrderItem.recieverPhone)
            view.edt_address_reciver.setText(orderItemMain.pickDropOrderItem.recieverAddress)
            view.txt_number.visibility = View.GONE
            orderItemMain.userAddress = "From "+orderItemMain.pickDropOrderItem.senderLocation+"To "+orderItemMain.pickDropOrderItem.recieverLocation
        }else{
            view.text_number_container.visibility = View.VISIBLE
            view.text_name_container.visibility = View.VISIBLE
            view.txt_number.visibility = View.VISIBLE
            if(orderItemMain.products.size==1 && !orderItemMain.products[0].product_item){
                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.VISIBLE
                changeTotalValue = true
                view.customOrderLinearLayout.visibility = View.VISIBLE
                view.nestedScrollView.visibility = View.GONE
                view.pickDropScrollView.visibility = View.GONE
                if(orderItemMain.products[0].parcel_item){
                    parcelOrder = true
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
                    alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "কুরিয়ার নেম"
                    alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text)
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].parcel_order_text_2)
                    view.titleCustomOrderLinear.text = "পার্সেল অর্ডার"
                    view.customOrderTitleTextView.text = "কুরিয়ার নেমঃ "+ orderItemMain.products[0].parcel_order_text
                    view.customOrderDetailsTextView.text = "ডিটেইলসঃ "+ orderItemMain.products[0].parcel_order_text_2
                    if(orderItemMain.products[0].parcel_order_image.isNotEmpty()){
                        val firebaseStorage = FirebaseStorage.getInstance()
                            .reference.child("ORDER_IMAGES")
                            .child(orderItemMain.key)
                            .child(orderItemMain.products[0].parcel_order_image)

                        Glide.with(requireContext())
                            .load(firebaseStorage)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerInside()
                            .placeholder(R.drawable.loading_image_glide)
                            .into(view.shopImageItem)

                        view.shopImageItem.setOnClickListener {
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
                    }else{
                        view.shopImageItem.visibility = View.GONE
                    }
                }else if(orderItemMain.products[0].medicine_item){
                    medicineOrder = true
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ঔষোধ "
                    alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.hint = "ফার্মেসি "
                    alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text)
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].medicine_order_text_2)
                    view.titleCustomOrderLinear.text = "মেডিসিন অর্ডার"
                    view.customOrderTitleTextView.text = "ফার্মেসিঃ "+ orderItemMain.products[0].medicine_order_text
                    view.customOrderDetailsTextView.text = "ঔষোধঃ "+ orderItemMain.products[0].medicine_order_text_2
                    if(orderItemMain.products[0].medicine_order_image.isNotEmpty()){
                        val firebaseStorage = FirebaseStorage.getInstance()
                            .reference.child("ORDER_IMAGES")
                            .child(orderItemMain.key)
                            .child(orderItemMain.products[0].medicine_order_image)

                        Glide.with(requireContext())
                            .load(firebaseStorage)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerInside()
                            .placeholder(R.drawable.loading_image_glide).into(view.shopImageItem)

                        view.shopImageItem.setOnClickListener {

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
                    }else{
                        view.shopImageItem.visibility = View.GONE
                    }
                }else if(orderItemMain.products[0].custom_order_item){
                    customOrder = true
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.hint = "ডিটেইলস"
                    alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrder.setText(orderItemMain.products[0].custom_order_text)

                    alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
                    view.titleCustomOrderLinear.text = "কাস্টম অর্ডার"
                    if(orderItemMain.adminOrder){
                        view.customOrderTitleTextView.text = "অ্যাডমিন অর্ডার"
                    }else{
                        view.customOrderTitleTextView.text = "জেনারেল অর্ডার"
                    }
                    view.customOrderDetailsTextView.text = "ডিটেইলসঃ "+ orderItemMain.products[0].custom_order_text
                    if(orderItemMain.products[0].custom_order_image.isNotEmpty()){
                        val firebaseStorage = FirebaseStorage.getInstance()
                            .reference.child("ORDER_IMAGES")
                            .child(orderItemMain.key)
                            .child(orderItemMain.products[0].custom_order_image)

                        Glide.with(requireContext())
                            .load(firebaseStorage)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerInside()
                            .placeholder(R.drawable.loading_image_glide).into(view.shopImageItem)

                        view.shopImageItem.setOnClickListener {
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
                    }else{
                        view.shopImageItem.visibility = View.GONE
                    }
                }
            }else{
                shopOrder = true
                alertDialogToEditOrderDetailsView.totalChargeEdittextEditOrderContainer.visibility = View.GONE
                alertDialogToEditOrderDetailsView.titleCustomOrderEdittextEditOrderContainer.visibility = View.GONE
                alertDialogToEditOrderDetailsView.detailsCustomOrderEdittextEditOrderContainer.visibility = View.GONE
                view.nestedScrollView.visibility = View.VISIBLE
                view.pickDropScrollView.visibility = View.GONE
                view.customOrderLinearLayout.visibility = View.GONE
                workWithTheArrayList(orderItemMain.products, view)
            }
        }
        if(orderItemMain.userAddress.isEmpty()){
            view.text_address_container.visibility = View.GONE
        }else{
            view.text_address_container.visibility = View.VISIBLE
            view.txt_address.setText(orderItemMain.userAddress)
        }
        priceTotal = orderItemMain.totalPrice
        deliveryCharge = orderItemMain.deliveryCharge
        promoCodeActive = orderItemMain.promoCodeApplied
        promoCode = orderItemMain.promoCode
        setPriceTotalOnView(view)
        view.orderIDText.text = orderItemMain.orderId
        if(orderItemMain.orderCompletedStatus == "CANCELLED") {
            view.orderStatusText2.text = "CANCELLED"
        }else{
            view.orderStatusText2.text = orderItemMain.orderStatus
        }
        detectOrderItemStatus(view)
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
        view.orderStatusText2.setOnClickListener {
            alertDialogToEditOrderDetails.show()
        }

        view.shopsProgress.visibility = View.GONE
        view.noProductsText.visibility = View.GONE
        view.mainLayout.visibility = View.VISIBLE
    }

    private fun detectOrderItemStatus(view: View) {
        view.orderStatusText2.text = orderItemMain.orderStatus
        when(orderItemMain.orderStatus){
            "PENDING" -> {
                view.button.text = "VERIFY"
                view.buttonCancel.visibility = View.VISIBLE
                view.button.setOnClickListener {
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
                                .addOnCompleteListener{task2 ->
                                    if(task2.isSuccessful){
                                            orderItemMain.orderStatus = "VERIFIED"
//                                        (activity as OrdresActivity)
//                                            .ordersMainOldItemsArrayList[(activity as OrdresActivity)
//                                            .mainItemPositionsRecyclerAdapter]
//                                            .orders[(activity as OrdresActivity)
//                                            .selectedRecyclerAdapterItem].orderStatus = "VERIFIED"
//                                        (activity as OrdresActivity).orderAdapterMain
//                                            .notifyItemChanged((activity as OrdresActivity)
//                                                .mainItemPositionsRecyclerAdapter)
                                        sendNotification(
                                            orderItemMain.userId,
                                            "আপনার অর্ডার ${orderItemMain.orderId} টি কনফার্ম করা হয়েছে ।",
                                            "আপনার অর্ডারটি কনফার্ম করা হয়েছে, দ্রুতই অর্ডারটি আপনার কাছে পৌছে যাবে ।",
                                            orderId)
                                        //detectOrderItemStatus(view)
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
                view.buttonCancel.setOnClickListener {
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
                                            orderItemMain.orderStatus = "COMPLETED"
                                            orderItemMain.orderCompletedStatus = "CANCELLED"
//                                        (activity as HomeActivity)
//                                            .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter]
//                                            .orders[(activity as HomeActivity)
//                                            .selectedRecyclerAdapterItem].orderStatus = "COMPLETED"
//                                        (activity as HomeActivity)
//                                            .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter]
//                                            .orders[(activity as HomeActivity)
//                                            .selectedRecyclerAdapterItem].orderCompletedStatus = "CANCELLED"
//                                        (activity as HomeActivity).orderAdapterMain
//                                            .notifyItemChanged((activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter)
                                        sendNotification(
                                            orderItemMain.userId,
                                            "আপনার অর্ডার ${orderItemMain.orderId} টি ক্যান্সেল করা হয়েছে ।",
                                            "অর্পণের সাথে থাকার জন্য ধন্যবাদ ।",
                                            orderId)
                                        //detectOrderItemStatus(view)
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
            }
            "VERIFIED" -> {
                if(orderItemMain.daID.isNotEmpty()){
                    view.txtDaStatus.visibility = View.VISIBLE
                    view.txtDaStatus.text = "PENDING VERIFICATION FROM DA -> ${orderItemMain.daDetails.da_name}"
                    view.button.text = "ASSIGN ANOTHER DA"
                }else{
                    view.txtDaStatus.visibility = View.VISIBLE
                    view.button.text = "ASSIGN DA"
                }
                view.buttonCancel.visibility = View.VISIBLE
                view.buttonCancel.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Are you sure you want to cancel this order?")
                        .setMessage("By clicking yes you're cancelling this order")
                        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                            progressDialog.show()
                            val hashMap = HashMap<String,Any>()
                            hashMap["orderStatus"] = "COMPLETED"
                            hashMap["orderCompletedStatus"] = "CANCELLED"
                            hashMap["daID"] = ""
                            FirebaseFirestore.getInstance().collection("users")
                                .document(customerId)
                                .collection("users_order_collection")
                                .document(orderId)
                                .update(hashMap)
                                .addOnCompleteListener {task2 ->
                                    if(task2.isSuccessful){
                                        orderItemMain.orderStatus = "COMPLETED"
                                        orderItemMain.orderCompletedStatus = "CANCELLED"
//                                        (activity as HomeActivity)
//                                            .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter]
//                                            .orders[(activity as HomeActivity)
//                                            .selectedRecyclerAdapterItem].orderStatus = "COMPLETED"
//                                        (activity as HomeActivity)
//                                            .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter]
//                                            .orders[(activity as HomeActivity)
//                                            .selectedRecyclerAdapterItem].orderCompletedStatus = "CANCELLED"
//                                        (activity as HomeActivity).orderAdapterMain
//                                            .notifyItemChanged((activity as HomeActivity)
//                                            .mainItemPositionsRecyclerAdapter)
                                        sendNotification(
                                            orderItemMain.userId,
                                            "আপনার অর্ডার ${orderItemMain.orderId} টি ক্যান্সেল করা হয়েছে ।",
                                            "অর্পণের সাথে থাকার জন্য ধন্যবাদ ।",
                                            orderId)
                                        //detectOrderItemStatus(view)
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
                view.button.setOnClickListener {
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
                                            orderItemMain.daDetails = daAgent
                                            orderItemMain.daID = it.result!!.id
//                                            (activity as HomeActivity)
//                                                .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                                .mainItemPositionsRecyclerAdapter]
//                                                .orders[(activity as HomeActivity)
//                                                .selectedRecyclerAdapterItem].daDetails = daAgent
//                                            (activity as HomeActivity)
//                                                .ordersMainOldItemsArrayList[(activity as HomeActivity)
//                                                .mainItemPositionsRecyclerAdapter]
//                                                .orders[(activity as HomeActivity)
//                                                .selectedRecyclerAdapterItem].daID = it.result!!.id
//                                            (activity as HomeActivity).orderAdapterMain
//                                                .notifyItemChanged((activity as HomeActivity)
//                                                    .mainItemPositionsRecyclerAdapter)
                                            sendNotificationToDa(
                                                orderItemMain.userId,
                                                it.result!!.id,
                                                "আপনি একটি অর্ডার ${orderItemMain.orderId} পেয়েছেন ।",
                                                "আপনি একটি অর্ডার পেয়েছেন দ্রুত অর্ডারটি রিসিভ করুন ।",
                                                orderId
                                            )
                                            alertDialogForDa.dismiss()
                                            //detectOrderItemStatus(view)
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
            }
            else -> {
                if(orderItemMain.daID.isNotEmpty()){
                    view.txtDaStatus.visibility = View.VISIBLE
                    view.txtDaStatus.text = "ASSIGNED TO -> ${orderItemMain.daDetails.da_name}"
                }else{
                    view.txtDaStatus.visibility = View.GONE
                }
                view.buttonCancel.visibility = View.GONE
                if(orderItemMain.orderCompletedStatus == "CANCELLED") {
                    view.orderStatusText2.text = "CANCELLED"
                    view.button.text = "CANCELLED"
                }else{
                    view.button.text = orderItemMain.orderStatus
                    view.orderStatusText2.text = orderItemMain.orderStatus
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
        val request: Request = Request.Builder()
            .url("https://admin.arpan.delivery/api/notification/send-notification-to-da-about-a-new-order-that-he-recieved")
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
            view.promoCodeAppliedText.text  = "THE USER HAS APPLIED ${orderItemMain.promoCode.promoCodeName} PROMO CODE"
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
}