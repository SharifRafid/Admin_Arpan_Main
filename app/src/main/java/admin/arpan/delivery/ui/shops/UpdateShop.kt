package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopCategoryItem
import admin.arpan.delivery.db.model.ShopItem
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_update_shop.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UpdateShop : AppCompatActivity() {

    val PICK_IMAGE_CODE = 121
    val PICK_COVER_IMAGE_CODE = 122
    private var imagePath = Uri.parse("")
    private var imagePathCover = Uri.parse("")
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var firebaseStorage : StorageReference
    private lateinit var dialog : Dialog
    private var imageName = "CategoryImageName"
    private var imageCoverName = "CoverImageName"
    private var key = ""
    private var coverKey = ""
    private var imageNo = 0
    val category_keys = ArrayList<String>()
    val category_names = ArrayList<String>()
    var shop_order = "0"
    lateinit var shopItem : ShopItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_shop)

        initVars()
        initOnClicks()
    }

    private fun initVars() {
        dialog = createProgressDialog()

        shopItem =
            ShopItem(
                key = intent.getStringExtra("key").toString(),
                name = intent.getStringExtra("name").toString(),
                categories = intent.getStringExtra("categories").toString(),
                image = intent.getStringExtra("image").toString(),
                cover_image = intent.getStringExtra("cover_image").toString(),
                da_charge = intent.getStringExtra("da_charge").toString(),
                deliver_charge = intent.getStringExtra("deliver_charge").toString(),
                location = intent.getStringExtra("location").toString(),
                username = intent.getStringExtra("username").toString(),
                password = intent.getStringExtra("password").toString(),
                order = intent.getStringExtra("order").toString().toInt(),
                status = intent.getStringExtra("status").toString()
            )

        userName.setText(shopItem.username)
        passWord.setText(shopItem.password)
        bookTitle.setText(shopItem.name)
        da_charge.setText(shopItem.da_charge)
        delivery_charge.setText(shopItem.deliver_charge)
        location.setText(shopItem.location)

        if(shopItem.cover_image.isNotEmpty()){
            val string = FirebaseStorage.getInstance()
                .getReference(Constants.FS_SHOPS_MAIN)
                .child(shopItem.key)
                .child(shopItem.cover_image)


            Glide.with(this)
                .load(string)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image)
                .into(imagePickCover)
        }

        if(shopItem.image.isNotEmpty()){
            val string = FirebaseStorage.getInstance()
                .getReference(Constants.FS_SHOPS_MAIN)
                .child(shopItem.key)
                .child(shopItem.image)


            Glide.with(this)
                .load(string)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image)
                .into(imagePick)
        }

        firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
            .document(Constants.FD_SHOPS_MAIN_CATEGORY)
            .get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    category_keys.clear()
                    category_names.clear()
                    val categoryItemsArray = ArrayList<ShopCategoryItem>()
                    val map = task.result!!.data as Map<String, Map<String,String>>
                    for(category_field in map.entries){
                        categoryItemsArray.add(
                            ShopCategoryItem(
                                key = category_field.key,
                                name = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME].toString(),
                                category_key = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY].toString(),
                                order = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_ORDER].toString()
                                    .toInt(),
                            )
                        )
                        category_names.add(category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME].toString())
                        category_keys.add(category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY].toString())
                    }
                    Collections.sort(categoryItemsArray, kotlin.Comparator { o1, o2 ->
                        (o1.order).compareTo(o2.order) })
                    val adapter = ArrayAdapter(
                        this@UpdateShop,
                        R.layout.custom_spinner_view,
                        category_names
                    )
                    adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
                    categoriesSpinner.adapter = adapter
                    if(category_keys.contains(shopItem.categories)){
                        categoriesSpinner.setSelection(category_keys.indexOf(shopItem.categories))
                    }
                }else{
                    task.exception!!.printStackTrace()
                }
            }

        key = shopItem.key
        firebaseStorage = FirebaseStorage.getInstance()
            .getReference(Constants.FS_SHOPS_MAIN)
            .child(key)
    }

    private fun initOnClicks() {
        imagePick.setOnClickListener {
            imageNo = 0
            ImagePicker.with(this)
                .cropSquare()
                .compress(20)
                .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        imagePickCover.setOnClickListener {
            imageNo = 1
            ImagePicker.with(this)
                .crop(300f, 140f)
                .compress(100)
                .maxResultSize(720, 680)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        upload.setOnClickListener {
            dialog.show()
            if(bookTitle.text.isNotEmpty()&&
                da_charge.text.isNotEmpty()&&
                delivery_charge.text.isNotEmpty()){
                if(imagePathCover.toString().isNotEmpty()&&imagePath.toString().isNotEmpty()){
                    uploadFile()
                }else if(imagePathCover.toString().isNotEmpty()){
                    uploadFileWithCoverImage()
                }else if(imagePath.toString().isNotEmpty()){
                    uploadFileWithImage()
                } else{
                    uploadFileWithoutImage()
                }
            }else{
                dialog.dismiss()
                Toast.makeText(this,"fill everything",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFileWithImage() {
        val iconStringName = "icon"
        firebaseStorage.
        child(iconStringName)
            .putFile(imagePath)
            .addOnSuccessListener {
                val map = HashMap<String,String>()
                map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
                map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
                map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
                map[Constants.FIELD_FD_SM_ICON] = iconStringName
                map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
                map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
                map[Constants.FIELD_FD_SM_ORDER] = shop_order
                map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
                map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()

                firebaseFirestore
                    .collection(Constants.FC_SHOPS_MAIN)
                    .document(key)
                    .update(map as Map<String, Any>)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference
                            .child(Constants.COUNT_ALL_SHOPS_MAIN)
                            .setValue(shop_order)
                        Toast.makeText(this, "Sucessfully Added", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        finish()
                    }
            }
    }

    private fun uploadFileWithoutImage() {
        val map = HashMap<String,String>()
        map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
        map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
        map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
        map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
        map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
        map[Constants.FIELD_FD_SM_ORDER] = shop_order
        map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
        map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()

        firebaseFirestore
            .collection(Constants.FC_SHOPS_MAIN)
            .document(key)
            .update(map as Map<String, Any>)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().reference
                    .child(Constants.COUNT_ALL_SHOPS_MAIN)
                    .setValue(shop_order)
                Toast.makeText(this, "Sucessfully Added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }
    }

    private fun uploadFileWithCoverImage() {
        val coverStringName = "cover"
        firebaseStorage.child(coverStringName)
            .putFile(imagePathCover)
            .addOnSuccessListener {
                val map = HashMap<String,String>()
                map[Constants.FIELD_FD_SM_COVER] = coverStringName
                map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
                map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
                map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
                map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
                map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
                map[Constants.FIELD_FD_SM_ORDER] = shop_order
                map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
                map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()

                firebaseFirestore
                    .collection(Constants.FC_SHOPS_MAIN)
                    .document(key)
                    .update(map as Map<String, Any>)
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference
                            .child(Constants.COUNT_ALL_SHOPS_MAIN)
                            .setValue(shop_order)
                        Toast.makeText(this, "Sucessfully Added", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        finish()
                    }
            }
    }

    private fun uploadFile() {
        val iconStringName = "icon"
        val coverStringName = "cover"
        firebaseStorage.
        child(iconStringName)
            .putFile(imagePath)
            .addOnSuccessListener {
                firebaseStorage.child(coverStringName)
                    .putFile(imagePathCover)
                    .addOnSuccessListener {
                        val map = HashMap<String,String>()
                        map[Constants.FIELD_FD_SM_COVER] = coverStringName
                        map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
                        map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
                        map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
                        map[Constants.FIELD_FD_SM_ICON] = iconStringName
                        map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
                        map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
                        map[Constants.FIELD_FD_SM_ORDER] = shop_order
                        map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
                        map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()

                        firebaseFirestore
                            .collection(Constants.FC_SHOPS_MAIN)
                            .document(key)
                            .update(map as Map<String, Any>)
                            .addOnSuccessListener {
                                FirebaseDatabase.getInstance().reference
                                    .child(Constants.COUNT_ALL_SHOPS_MAIN)
                                    .setValue(shop_order)
                                Toast.makeText(this, "Sucessfully Added", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                finish()
                            }
                    }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                if(imageNo==0){
                    imagePath = data!!.data
                    imagePick.setImageURI(imagePath)
                    val file = File(imagePath.path!!)
                    titleTextView.text = "Image Size : ${file.length()/1024} KB"
                    imageName = "icon"
                }else{
                    imagePathCover = data!!.data
                    imagePickCover.setImageURI(imagePathCover)
                    imageCoverName = "cover"
                }
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}