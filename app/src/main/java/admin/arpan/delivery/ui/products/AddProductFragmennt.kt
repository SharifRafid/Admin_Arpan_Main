package admin.arpan.delivery.ui.products

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.utils.Constants
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.File
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddProductFragmennt : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var imagePath = Uri.parse("")
    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var shop_key = ""
    private var product_category_key = ""
    private var product_category_tag = ""
    private var product_order = ""
    private var imageName = "ProductImageName"
    private var key = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return inflater.inflate(R.layout.activity_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initVars()
        initOnClicks()
    }

    private fun initVars() {
        shop_key = requireArguments().getString("shop_key").toString()
        product_category_key = requireArguments().getString("product_category_key").toString()
        product_category_tag = requireArguments().getString("product_category").toString()
        product_order = requireArguments().getString("product_order").toString()

        key = "pdct"+System.currentTimeMillis()

    }

    private fun initOnClicks() {
        imagePick.setOnClickListener {
//            val getImageIntent = Intent(Intent.ACTION_GET_CONTENT)
//            getImageIntent.type = "image/*"
//            startActivityForResult(
//                Intent.createChooser(
//                    getImageIntent,
//                    "Select Picture"
//                ), PICK_IMAGE_CODE
//            )
            ImagePicker.with(this)
                .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                .compress(40)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        upload.setOnClickListener {
            if(price.text.isNotEmpty()&&
                productTitle.text.isNotEmpty()){
                cardViewButton.visibility = View.GONE
                progress_circular.visibility  = View.VISIBLE
                if(imagePath.toString().isNotEmpty()){
                    uploadFile()
                }else{
                    FirebaseDatabase.getInstance().reference
                        .child("PRODUCT_COUNT")
                        .child(shop_key)
                        .child(Constants.COUNT_ALL_PRODUCT_MAIN)
                        .get().addOnCompleteListener {
                            try{
                                product_order = if(it.isSuccessful){
                                    (it.result!!.value.toString().toInt()+1).toString()
                                }else{
                                    "1"
                                }
                            }catch (e : Exception){
                                product_order = "1"
                                e.printStackTrace()
                            }
                            val hashMap = ProductItem()
                            hashMap.image1 = ""
                            hashMap.price = price.text.toString()
                            hashMap.name = productTitle.text.toString()
                            hashMap.shopKey = shop_key
                            if(offerPrice.text.toString().isNotEmpty()){
                                hashMap.offerPrice = offerPrice.text.toString()
                            }else{
                                hashMap.offerPrice = price.text.toString()
                            }
                            hashMap.shortDescription = productDesc.text.toString()
                            hashMap.order = product_order
                            hashMap.inStock = "active"
                            hashMap.offerStatus = "inactive"
                            hashMap.offerDetails = "স্পেশাল ওফার"
                            hashMap.shopCategoryKey = product_category_tag
                            firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                                .document(shop_key)
                                .collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                                .add(hashMap).addOnSuccessListener {task2 ->
                                    hashMap.key = task2.id
                                    (activity as ProductsActivity).productsMainArrayList.add(hashMap)
                                    (activity as ProductsActivity).productsItemAdapterMain.notifyItemInserted((activity as ProductsActivity).productsMainArrayList.size-1)
                                    (activity as ProductsActivity).productsItemAdapterMain.notifyItemRangeChanged((activity as ProductsActivity).productsMainArrayList.size-1, (activity as ProductsActivity).productsMainArrayList.size)
                                    FirebaseDatabase.getInstance().reference
                                        .child("PRODUCT_COUNT")
                                        .child(shop_key)
                                        .child(Constants.COUNT_ALL_PRODUCT_MAIN)
                                        .setValue(product_order)
                                    Toast.makeText(
                                        requireContext(), "Added  Success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismiss()
                                }
                        }
                }
            }else{
                Toast.makeText(
                    requireContext(), "fill everything",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadFile() {
        FirebaseStorage.getInstance()
            .reference
            .child(Constants.FS_SHOPS_MAIN)
            .child(shop_key)
            .child(imageName)
            .putFile(imagePath)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().reference
                    .child("PRODUCT_COUNT")
                    .child(shop_key)
                    .child(Constants.COUNT_ALL_PRODUCT_MAIN)
                    .get().addOnCompleteListener {
                        try{
                            product_order = if(it.isSuccessful){
                                (it.result!!.value.toString().toInt()+1).toString()
                            }else{
                                "1"
                            }
                        }catch (e : Exception){
                            product_order = "1"
                            e.printStackTrace()
                        }
                        val hashMap = ProductItem()
                        hashMap.image1 = imageName
                        hashMap.price = price.text.toString()
                        hashMap.shopKey = shop_key
                        hashMap.name = productTitle.text.toString()
                        if(offerPrice.text.toString().isNotEmpty()){
                            hashMap.offerPrice = offerPrice.text.toString()
                        }else{
                            hashMap.offerPrice = price.text.toString()
                        }
                        hashMap.shortDescription = productDesc.text.toString()
                        hashMap.inStock = "active"
                        hashMap.offerStatus = "inactive"
                        hashMap.order = product_order
                        hashMap.offerDetails = "স্পেশাল ওফার"
                        hashMap.shopCategoryKey = product_category_tag
                        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                            .document(shop_key)
                            .collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                            .add(hashMap).addOnSuccessListener {task2 ->
                                hashMap.key = task2.id
                                (activity as ProductsActivity).productsMainArrayList.add(hashMap)
                                (activity as ProductsActivity).productsItemAdapterMain.notifyItemInserted((activity as ProductsActivity).productsMainArrayList.size-1)
                                (activity as ProductsActivity).productsItemAdapterMain.notifyItemRangeChanged((activity as ProductsActivity).productsMainArrayList.size-1, (activity as ProductsActivity).productsMainArrayList.size)
                                FirebaseDatabase.getInstance().reference
                                    .child("PRODUCT_COUNT")
                                    .child(shop_key)
                                    .child(Constants.COUNT_ALL_PRODUCT_MAIN)
                                    .setValue(product_order)
                                Toast.makeText(
                                    requireContext(), "Added  Success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                    }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK) {
//            val fullPhotoUri = data!!.data as Uri
//            UCrop.of(fullPhotoUri, Uri.fromFile(File(cacheDir, key)))
//                .withAspectRatio(1F, 1F)
//                .withMaxResultSize(250, 250)
//                .start(this)
//        }else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            imagePath = UCrop.getOutput(data!!) as Uri
//            imagePick.setImageURI(imagePath)
//            val file = File(imagePath.path!!)
//            titleTextView.text = "Image Size : ${file.length()/1024} KB"
//            imageName = key
//        } else if (resultCode == UCrop.RESULT_ERROR) {
//            Toast.makeText(this,UCrop.RESULT_ERROR,Toast.LENGTH_SHORT).show()
//        }

        if (resultCode == Activity.RESULT_OK) {
            imagePath = data!!.data!!
            imagePick.setImageURI(imagePath)
            imagePick.setPadding(0,0,0,0)
            val file = File(imagePath.path!!)
            titleTextView.text = "Image Size : ${file.length()/1024} KB"
            imageName = "prdctimg"+System.currentTimeMillis()
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}