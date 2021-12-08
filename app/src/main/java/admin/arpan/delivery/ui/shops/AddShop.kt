package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopCategoryItem
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
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_add_shop.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddShop : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_shop)

        initVars()
        initOnClicks()
    }

    private fun initVars() {
        dialog = createProgressDialog()

        shop_order = intent.getStringExtra("array_size").toString()

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
                        this@AddShop,
                            R.layout.custom_spinner_view,
                        category_names
                    )

                    adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)

                    categoriesSpinner.adapter = adapter
                }else{
                    task.exception!!.printStackTrace()
                }
            }

        key = "shop${System.currentTimeMillis()}"
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
            if(imagePath.toString().isNotEmpty()&&
                bookTitle.text.isNotEmpty()&&
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
        val iconStringName = "icon${System.currentTimeMillis()}"
        firebaseStorage.
        child(iconStringName)
            .putFile(imagePath)
            .addOnSuccessListener {
                val map = HashMap<String,String>()
                map[Constants.FIELD_FD_SM_COVER] = ""
                map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
                map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
                map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
                map[Constants.FIELD_FD_SM_ICON] = iconStringName
                map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
                map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
                map[Constants.FIELD_FD_SM_ORDER] = shop_order
                map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
                map[Constants.FIELD_FD_SM_STATUS] = "open"
                map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()
                map[Constants.FIELD_FD_SM_IS_CLIENT] = if(clientShopSwitchMaterial.isChecked){
                    "yes"
                }else{
                    "no"
                }

                firebaseFirestore
                    .collection(Constants.FC_SHOPS_MAIN)
                    .document(key)
                    .set(map)
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
        map[Constants.FIELD_FD_SM_COVER] = ""
        map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
        map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
        map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
        map[Constants.FIELD_FD_SM_ICON] = ""
        map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
        map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
        map[Constants.FIELD_FD_SM_ORDER] = shop_order
        map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
        map[Constants.FIELD_FD_SM_STATUS] = "open"
        map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()
        map[Constants.FIELD_FD_SM_IS_CLIENT] = if(clientShopSwitchMaterial.isChecked){
            "yes"
        }else{
            "no"
        }
        firebaseFirestore
            .collection(Constants.FC_SHOPS_MAIN)
            .document(key)
            .set(map)
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
        val coverStringName = "cover${System.currentTimeMillis()}"
        firebaseStorage.child(coverStringName)
            .putFile(imagePathCover)
            .addOnSuccessListener {
                val map = HashMap<String,String>()
                map[Constants.FIELD_FD_SM_COVER] = coverStringName
                map[Constants.FIELD_FD_SM_CATEGORY] = category_keys[categoriesSpinner.selectedItemPosition]
                map[Constants.FIELD_FD_SM_DA_CHARGE] = da_charge.text.toString()
                map[Constants.FIELD_FD_SM_DELIVERY] = delivery_charge.text.toString()
                map[Constants.FIELD_FD_SM_ICON] = ""
                map[Constants.FIELD_FD_SM_LOCATION] = location.text.toString()
                map[Constants.FIELD_FD_SM_NAME] = bookTitle.text.toString()
                map[Constants.FIELD_FD_SM_ORDER] = shop_order
                map[Constants.FIELD_FD_SM_PASSWORD] = passWord.text.toString()
                map[Constants.FIELD_FD_SM_STATUS] = "open"
                map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()
                map[Constants.FIELD_FD_SM_IS_CLIENT] = if(clientShopSwitchMaterial.isChecked){
                    "yes"
                }else{
                    "no"
                }
                firebaseFirestore
                    .collection(Constants.FC_SHOPS_MAIN)
                    .document(key)
                    .set(map)
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
        val iconStringName = "icon${System.currentTimeMillis()}"
        val coverStringName = "cover${System.currentTimeMillis()}"
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
                        map[Constants.FIELD_FD_SM_STATUS] = "open"
                        map[Constants.FIELD_FD_SM_USERNAME] = userName.text.toString()
                        map[Constants.FIELD_FD_SM_IS_CLIENT] = if(clientShopSwitchMaterial.isChecked){
                            "yes"
                        }else{
                            "no"
                        }
                        firebaseFirestore
                            .collection(Constants.FC_SHOPS_MAIN)
                            .document(key)
                            .set(map)
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
                    imageName = key
                }else{
                    imagePathCover = data!!.data
                    imagePickCover.setImageURI(imagePathCover)
                    imageCoverName = "cover$key"
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