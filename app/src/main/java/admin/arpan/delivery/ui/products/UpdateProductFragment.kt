package admin.arpan.delivery.ui.products

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.utils.Constants
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.File

class UpdateProductFragment : DialogFragment() {
    private var imagePath = Uri.parse("")
    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var shop_key = ""
    private var product_category_key = ""
    private var product_category_tag = ""
    private var product_order = ""
    private var imageName = "ProductImageName"
    private var key = ""
    private var itemToUpdate = ProductItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        shop_key = requireArguments().getString("shopKey").toString()
        product_category_key = requireArguments().getString("product_category_key").toString()
        product_category_tag = requireArguments().getString("product_category").toString()
        product_order = requireArguments().getString("order").toString()
        itemToUpdate.key = requireArguments().getString("key").toString()
        itemToUpdate.name = requireArguments().getString("name").toString()
        itemToUpdate.shopKey = requireArguments().getString("shopKey").toString()
        itemToUpdate.shopCategoryId = requireArguments().getString("shopCategoryId").toString()
        itemToUpdate.shopCategoryKey = requireArguments().getString("shopCategoryKey").toString()
        itemToUpdate.price = requireArguments().getString("price").toString()
        itemToUpdate.arpanCharge = requireArguments().getString("arpanCharge").toString().toInt()
        itemToUpdate.image1 = requireArguments().getString("image1").toString()
        itemToUpdate.image2 = requireArguments().getString("image2").toString()
        itemToUpdate.image3 = requireArguments().getString("image3").toString()
        itemToUpdate.offerPrice = requireArguments().getString("offerPrice").toString()
        itemToUpdate.offerStatus = requireArguments().getString("offerStatus").toString()
        itemToUpdate.inStock = requireArguments().getString("inStock").toString()
        itemToUpdate.description = requireArguments().getString("description").toString()
        itemToUpdate.shortDescription = requireArguments().getString("shortDescription").toString()
        itemToUpdate.offerDetails = requireArguments().getString("offerDetails").toString()
        itemToUpdate.productDetails = requireArguments().getString("productDetails").toString()
        itemToUpdate.order = requireArguments().getString("order").toString()

        upload.text = "Update"

        key = itemToUpdate.key

        Log.e("KEY", key)

        productTitle.setText(itemToUpdate.name)
        productDesc.setText(itemToUpdate.shortDescription)
        price.setText(itemToUpdate.price)
        offerPrice.setText(itemToUpdate.offerPrice)
        arpanProfitPrice.setText(itemToUpdate.arpanCharge.toString())

        if(itemToUpdate.image1.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance().getReference("shops")
                .child(shop_key)
                .child(itemToUpdate.image1)

            Glide.with(this)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image).into(imagePick)
        }
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
                    val hashMap = HashMap<String,Any>()
                    //hashMap["image1"] = imageName
                    hashMap["price"] = price.text.toString()
                    //hashMap["shopKey"] = shop_key
                    hashMap["name"] = productTitle.text.toString()
                    if(offerPrice.text.toString().isNotEmpty()){
                        hashMap["offerPrice"] = offerPrice.text.toString()
                    }else{
                        hashMap["offerPrice"] = price.text.toString()
                    }
                    if(arpanProfitPrice.text.isEmpty()){
                        hashMap["arpanCharge"] = 0
                    }else{
                        hashMap["arpanCharge"] = arpanProfitPrice.text.toString().toInt()
                    }
                    hashMap["shortDescription"] = productDesc.text.toString()
                    //hashMap["inStock"] = "active"
                    //hashMap["offerStatus"] = "inactive"
                    //hashMap["order"] = product_order
                    //hashMap["offerDetails"] = "স্পেশাল ওফার"
                    //hashMap["shopCategoryKey"] = product_category_tag
                    firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                        .document(shop_key)
                        .collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(key)
                        .update(hashMap).addOnSuccessListener {
                            (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].price = hashMap["price"].toString()
                            (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].name = hashMap["name"].toString()
                            (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].offerPrice = hashMap["offerPrice"].toString()
                            (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].shortDescription = hashMap["shortDescription"].toString()
                            (activity as ProductsActivity).productsItemAdapterMain.notifyItemChanged((activity as ProductsActivity).currentSelectedProductMainIndex)
                            Toast.makeText(
                                requireContext(), "Update Success",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
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
                val hashMap = HashMap<String,Any>()
                hashMap["image1"] = imageName
                hashMap["price"] = price.text.toString()
               // hashMap["shopKey"] = shop_key
                hashMap["name"] = productTitle.text.toString()
                if(offerPrice.text.toString().isNotEmpty()){
                    hashMap["offerPrice"] = offerPrice.text.toString()
                }else{
                    hashMap["offerPrice"] = price.text.toString()
                }
                hashMap["shortDescription"] = productDesc.text.toString()
                if(arpanProfitPrice.text.isEmpty()){
                    hashMap["arpanCharge"] = 0
                }else{
                    hashMap["arpanCharge"] = arpanProfitPrice.text.toString().toInt()
                }
                //hashMap["inStock"] = "active"
                //hashMap["offerStatus"] = "inactive"
                //hashMap["order"] = product_order
                //hashMap["offerDetails"] = "স্পেশাল ওফার"
                //hashMap["shopCategoryKey"] = product_category_tag
                firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                    .document(shop_key)
                    .collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                    .document(itemToUpdate.key)
                    .update(hashMap).addOnSuccessListener {
                        (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].image1 = hashMap["image1"].toString()
                        (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].price = hashMap["price"].toString()
                        (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].name = hashMap["name"].toString()
                        (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].offerPrice = hashMap["offerPrice"].toString()
                        (activity as ProductsActivity).productsMainArrayList[(activity as ProductsActivity).currentSelectedProductMainIndex].shortDescription = hashMap["shortDescription"].toString()
                        (activity as ProductsActivity).productsItemAdapterMain.notifyItemChanged((activity as ProductsActivity).currentSelectedProductMainIndex)
                        Toast.makeText(
                            requireContext(), "Update  Success",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }
            }
    }

    @Deprecated("Deprecated in Java")
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
            Toast.makeText( requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText( requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}