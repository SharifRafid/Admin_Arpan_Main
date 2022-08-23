package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopCategoryItem
import admin.arpan.delivery.db.model.ShopItem
import admin.arpan.delivery.db.model.SlidingTextItem
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Notice
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.*
import admin.arpan.delivery.viewModels.CategoryViewModel
import admin.arpan.delivery.viewModels.ShopViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubd.bdlocationchooser.BdLocationChooser
import com.dubd.bdlocationchooser.District
import com.dubd.bdlocationchooser.Division
import com.dubd.bdlocationchooser.Upazila
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_shop.*
import kotlinx.android.synthetic.main.activity_update_shop.*
import kotlinx.android.synthetic.main.activity_update_shop.bookTitle
import kotlinx.android.synthetic.main.activity_update_shop.categoriesSpinner
import kotlinx.android.synthetic.main.activity_update_shop.clientShopSwitchMaterial
import kotlinx.android.synthetic.main.activity_update_shop.da_charge
import kotlinx.android.synthetic.main.activity_update_shop.delivery_charge
import kotlinx.android.synthetic.main.activity_update_shop.imagePick
import kotlinx.android.synthetic.main.activity_update_shop.imagePickCover
import kotlinx.android.synthetic.main.activity_update_shop.location
import kotlinx.android.synthetic.main.activity_update_shop.passWord
import kotlinx.android.synthetic.main.activity_update_shop.titleTextView
import kotlinx.android.synthetic.main.activity_update_shop.upload
import kotlinx.android.synthetic.main.activity_update_shop.userName
import kotlinx.android.synthetic.main.activity_update_shop.view.*
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class UpdateShop : AppCompatActivity() {

  val PICK_IMAGE_CODE = 121
  val PICK_COVER_IMAGE_CODE = 122
  private var imagePath = Uri.parse("")
  private var imagePathCover = Uri.parse("")
  private lateinit var dialog: Dialog
  private var imageName = "CategoryImageName"
  private var imageCoverName = "CoverImageName"
  private var key = ""
  private var coverKey = ""
  private var imageNo = 0
  val category_keys = ArrayList<String>()
  val category_names = ArrayList<String>()
  var shop_order = "0"
  lateinit var shopItem: Shop
  private var textColorNotice = "#FFFFFF"
  private var bgColorNotice = "#000000"
  private val viewModel: ShopViewModel by viewModels()
  private val categoryViewModel: CategoryViewModel by viewModels()
  private val uploadViewModel: UploadViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_update_shop)

    initVars()
    initOnClicks()
  }

  private fun initVars() {
    dialog = createProgressDialog()

    shopItem =
      getGsonParser()!!.fromJson(intent.getStringExtra("data").toString(), Shop::class.java)

    bookTitle.setText(shopItem.name)
    da_charge.setText(shopItem.daCharge.toString())
    delivery_charge.setText(shopItem.deliveryCharge.toString())
    location.setText(shopItem.location)
    shopOrderEdittext.setText(shopItem.order.toString())

    if (!shopItem.notices.isEmpty()) {
      addShopNoteTextView.text = shopItem.notices[0].title
      addShopNoteTextView.setTextColor(Color.parseColor(shopItem.notices[0].color.toString()))
      addShopNoteTextView.setBackgroundColor(Color.parseColor(shopItem.notices[0].bgColor.toString()))
    } else {
      addShopNoteTextView.text = "Add Shop Top Note"
    }
    addShopNoteTextView.setOnClickListener {
      PopUpEditText.create(this).setCompleteListener(object : CompleteListener {
        override fun onTextSubmitted(text: String) {
          if (text.trim().isEmpty()) {
            addShopNoteTextView.text = "Add Shop Top Note"
          } else {
            addShopNoteTextView.text = text
          }
        }
      }).show()
    }
    buttonTextColor.setOnClickListener {
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
          textColorNotice = colorHex
          addShopNoteTextView.setTextColor(color)
        }
        .show()
    }
    buttonBgColor.setOnClickListener {
      MaterialColorPickerDialog
        .Builder(this)          // Pass Activity Instance
        .setTitle("Pick Background Color")
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
          bgColorNotice = colorHex
          addShopNoteTextView.setBackgroundColor(color)
        }
        .show()
    }
    clientShopSwitchMaterial.isChecked = shopItem.isClient!!

    if (shopItem.coverPhoto != null) {
      Glide.with(this)
        .load(Constants.SERVER_FILES_BASE_URL + shopItem.coverPhoto!!.path)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .override(300, 300)
        .placeholder(R.drawable.test_shop_image)
        .into(imagePickCover)
    }

    if (shopItem.icon != null) {
      Glide.with(this)
        .load(Constants.SERVER_FILES_BASE_URL + shopItem.icon!!.path)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .override(300, 300)
        .placeholder(R.drawable.test_shop_image)
        .into(imagePick)
    }

    LiveDataUtil.observeOnce(categoryViewModel.getCategories("shop")) {
      if (it.error == true) {
        showToast("Failed to fetch categories", FancyToast.ERROR)
      } else {
        category_keys.clear()
        category_names.clear()
        val categoryItemsArray = it.results as ArrayList<Category>
        for (category_field in categoryItemsArray) {
          category_names.add(category_field.name.toString())
          category_keys.add(category_field.id.toString())
        }
        categoryItemsArray.sortWith { o1, o2 ->
          (o1.order!!).compareTo(o2.order!!)
        }
        val adapter = ArrayAdapter(
          this@UpdateShop,
          R.layout.custom_spinner_view,
          category_names
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
        categoriesSpinner.adapter = adapter
        if (shopItem.categories.isNotEmpty()) {
          if (category_keys.contains(shopItem.categories[0])) {
            categoriesSpinner.setSelection(category_keys.indexOf(shopItem.categories[0]))
          }
        }else{
          Log.e("SHOPID", shopItem.id.toString())
        }
      }
    }

    key = shopItem.id.toString()
  }

  private fun initOnClicks() {
    imagePick.setOnClickListener {
      imageNo = 0
      ImagePicker.with(this)
        .cropSquare()
        .compress(20)
        .maxResultSize(512, 512)  //Final image resolution will be less than 1080 x 1080(Optional)
        .start()
    }

    imagePickCover.setOnClickListener {
      imageNo = 1
      ImagePicker.with(this)
        .crop(300f, 140f)
        .compress(100)
        .maxResultSize(720, 680)  //Final image resolution will be less than 1080 x 1080(Optional)
        .start()
    }

    upload.setOnClickListener {
      dialog.show()
      if (bookTitle.text.isNotEmpty() &&
        da_charge.text.isNotEmpty() &&
        delivery_charge.text.isNotEmpty()
      ) {
        if (imagePathCover.toString().isNotEmpty() && imagePath.toString().isNotEmpty()) {
          uploadFile()
        } else if (imagePathCover.toString().isNotEmpty()) {
          uploadFileWithCoverImage()
        } else if (imagePath.toString().isNotEmpty()) {
          uploadFileWithImage()
        } else {
          uploadFileWithoutImage()
        }
      } else {
        dialog.dismiss()
        Toast.makeText(
          this, "fill everything",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun uploadFileWithImage() {
    val iconStringName = "icon${System.currentTimeMillis()}"
    // Pass it like this
    val file = imagePath.toFile()
    val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
    // MultipartBody.Part is used to send also the actual file name
    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
      "fileName",
      key + "_" + iconStringName + ".png",
      requestFile
    )

    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) {
      val map = HashMap<String, Any>()
      map["categories"] = arrayListOf(category_keys[categoriesSpinner.selectedItemPosition])
      map["daCharge"] = da_charge.text.toString()
      map["deliveryCharge"] = delivery_charge.text.toString()
      map["icon"] = it
      map["order"] = shopOrderEdittext.text.toString()
      map["location"] = location.text.toString()
      map["name"] = bookTitle.text.toString()
      if (addShopNoteTextView.text.isEmpty() || addShopNoteTextView.text == "Add Shop Top Note") {
        map["notices"] = ArrayList<Notice>()
      } else {
        map["notices"] = arrayListOf(
          Notice(
            title = addShopNoteTextView.text.toString(),
            color = textColorNotice,
            bgColor = bgColorNotice
          )
        )
      }
      map["isClient"] = clientShopSwitchMaterial.isChecked
      LiveDataUtil.observeOnce(viewModel.updateShopItem(key, map)) { shop ->
        dialog.dismiss()
        if (shop.id != null) {
          showToast("Updated Successfully", FancyToast.SUCCESS)
          finish()
        } else {
          showToast("Failed to update.", FancyToast.SUCCESS)
        }
      }
    }
  }

  private fun uploadFileWithoutImage() {
    val map = HashMap<String, Any>()
    map["categories"] = arrayListOf(category_keys[categoriesSpinner.selectedItemPosition])
    map["daCharge"] = da_charge.text.toString()
    map["deliveryCharge"] = delivery_charge.text.toString()
    map["order"] = shopOrderEdittext.text.toString()
    map["location"] = location.text.toString()
    map["name"] = bookTitle.text.toString()
    if (addShopNoteTextView.text.isEmpty() || addShopNoteTextView.text == "Add Shop Top Note") {
      map["notices"] = ArrayList<Notice>()
    } else {
      map["notices"] = arrayListOf(
        Notice(
          title = addShopNoteTextView.text.toString(),
          color = textColorNotice,
          bgColor = bgColorNotice
        )
      )
    }
    map["isClient"] = clientShopSwitchMaterial.isChecked
    LiveDataUtil.observeOnce(viewModel.updateShopItem(key, map)) {
      dialog.dismiss()
      if (it.id != null) {
        showToast("Updated Successfully", FancyToast.SUCCESS)
        finish()
      } else {
        showToast("Failed to update.", FancyToast.SUCCESS)
      }
    }
  }

  private fun uploadFileWithCoverImage() {
    val coverStringName = "cover${System.currentTimeMillis()}"
    // Pass it like this
    val file = imagePathCover.toFile()
    val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
    // MultipartBody.Part is used to send also the actual file name
    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
      "fileName",
      key + "_" + coverStringName + ".png",
      requestFile
    )

    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) { coverPhoto ->
      val map = HashMap<String, Any>()
      map["categories"] = arrayListOf(category_keys[categoriesSpinner.selectedItemPosition])
      map["daCharge"] = da_charge.text.toString()
      map["deliveryCharge"] = delivery_charge.text.toString()
      map["coverPhoto"] = coverPhoto
      map["order"] = shopOrderEdittext.text.toString()
      map["location"] = location.text.toString()
      map["name"] = bookTitle.text.toString()
      if (addShopNoteTextView.text.isEmpty() || addShopNoteTextView.text == "Add Shop Top Note") {
        map["notices"] = ArrayList<Notice>()
      } else {
        map["notices"] = arrayListOf(
          Notice(
            title = addShopNoteTextView.text.toString(),
            color = textColorNotice,
            bgColor = bgColorNotice
          )
        )
      }
      map["isClient"] = clientShopSwitchMaterial.isChecked
      LiveDataUtil.observeOnce(viewModel.updateShopItem(key, map)) {
        dialog.dismiss()
        if (it.id != null) {
          showToast("Updated Successfully", FancyToast.SUCCESS)
          finish()
        } else {
          showToast("Failed to update.", FancyToast.SUCCESS)
        }
      }
    }
  }

  private fun uploadFile() {
    val coverStringName = "cover${System.currentTimeMillis()}"

    val iconStringName = "icon${System.currentTimeMillis()}"
    // Pass it like this
    var file = imagePath.toFile()
    var requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
    // MultipartBody.Part is used to send also the actual file name
    var body: MultipartBody.Part = MultipartBody.Part.createFormData(
      "fileName",
      key + "_" + iconStringName + ".png",
      requestFile
    )
    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) {
      // Pass it like this
      file = imagePathCover.toFile()
      requestFile = RequestBody.create(MediaType.parse("image/png"), file)
      // MultipartBody.Part is used to send also the actual file name
      body = MultipartBody.Part.createFormData(
        "fileName",
        key + "_" + coverStringName + ".png",
        requestFile
      )
      LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) { coverPhoto ->
        val map = HashMap<String, Any>()
        map["categories"] = arrayListOf(category_keys[categoriesSpinner.selectedItemPosition])
        map["daCharge"] = da_charge.text.toString()
        map["deliveryCharge"] = delivery_charge.text.toString()
        map["coverPhoto"] = coverPhoto
        map["icon"] = it
        map["order"] = shopOrderEdittext.text.toString()
        map["location"] = location.text.toString()
        map["name"] = bookTitle.text.toString()
        if (addShopNoteTextView.text.isEmpty() || addShopNoteTextView.text == "Add Shop Top Note") {
          map["notices"] = ArrayList<Notice>()
        } else {
          map["notices"] = arrayListOf(
            Notice(
              title = addShopNoteTextView.text.toString(),
              color = textColorNotice,
              bgColor = bgColorNotice
            )
          )
        }
        map["isClient"] = clientShopSwitchMaterial.isChecked
        LiveDataUtil.observeOnce(viewModel.updateShopItem(key, map)) {
          dialog.dismiss()
          if (it.id != null) {
            showToast("Updated Successfully", FancyToast.SUCCESS)
            finish()
          } else {
            showToast("Failed to update.", FancyToast.SUCCESS)
          }
        }
      }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> {
        if (imageNo == 0) {
          imagePath = data!!.data
          imagePick.setImageURI(imagePath)
          val file = File(imagePath.path!!)
          titleTextView.text = "Image Size : ${file.length() / 1024} KB"
          imageName = "icon"
        } else {
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