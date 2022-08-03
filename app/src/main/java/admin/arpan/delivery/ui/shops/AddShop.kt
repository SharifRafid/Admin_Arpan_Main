package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.models.Category
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.CategoryViewModel
import admin.arpan.delivery.viewModels.ShopViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.net.toFile
import com.github.dhaval2404.imagepicker.ImagePicker
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_shop.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class AddShop : AppCompatActivity() {

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
  private val viewModel: ShopViewModel by viewModels()
  private val categoryViewModel: CategoryViewModel by viewModels()
  private val uploadViewModel: UploadViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_shop)

    initVars()
    initOnClicks()
  }

  private fun initVars() {
    dialog = createProgressDialog()

    shop_order = intent.getStringExtra("array_size").toString()

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
        Collections.sort(categoryItemsArray, kotlin.Comparator { o1, o2 ->
          (o1.order!!).compareTo(o2.order!!)
        })
        val adapter = ArrayAdapter(
          this@AddShop,
          R.layout.custom_spinner_view,
          category_names
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
        categoriesSpinner.adapter = adapter
      }
    }

    key = "shop${System.currentTimeMillis()}"
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
      if (imagePath.toString().isNotEmpty() &&
        bookTitle.text.isNotEmpty() &&
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
      val shop = Shop(
        clientShopSwitchMaterial.isChecked,
        false,
        arrayListOf(category_keys[categoriesSpinner.selectedItemPosition]),
        arrayListOf(),
        bookTitle.text.toString(),
        shop_order.toInt(),
        "",
        null,
        it,
        "",
        location.text.toString(),
        delivery_charge.text.toString().toInt(),
        da_charge.text.toString().toInt(),
        arrayListOf(),
        null
      )
      LiveDataUtil.observeOnce(viewModel.createShopItem(shop)) { shop ->
        dialog.dismiss()
        if (shop.id != null) {
          finish()
          showToast("Sucessfully Added", FancyToast.SUCCESS)
        } else {
          showToast("Failed to add", FancyToast.ERROR)
        }
      }
    }
  }

  private fun uploadFileWithoutImage() {
    val shop = Shop(
      clientShopSwitchMaterial.isChecked,
      false,
      arrayListOf(category_keys[categoriesSpinner.selectedItemPosition]),
      arrayListOf(),
      bookTitle.text.toString(),
      shop_order.toInt(),
      "",
      null,
      null,
      "",
      location.text.toString(),
      delivery_charge.text.toString().toInt(),
      da_charge.text.toString().toInt(),
      arrayListOf(),
      null
    )
    LiveDataUtil.observeOnce(viewModel.createShopItem(shop)) { shop ->
      dialog.dismiss()
      if (shop.id != null) {
        showToast("Sucessfully Added", FancyToast.SUCCESS)
        finish()
      } else {
        showToast("Failed to add", FancyToast.ERROR)
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

    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) {coverImage->
      val shop = Shop(
        clientShopSwitchMaterial.isChecked,
        false,
        arrayListOf(category_keys[categoriesSpinner.selectedItemPosition]),
        arrayListOf(),
        bookTitle.text.toString(),
        shop_order.toInt(),
        "",
        coverImage,
        null,
        "",
        location.text.toString(),
        delivery_charge.text.toString().toInt(),
        da_charge.text.toString().toInt(),
        arrayListOf(),
        null
      )
      LiveDataUtil.observeOnce(viewModel.createShopItem(shop)) { shop ->
        dialog.dismiss()
        if (shop.id != null) {
          showToast("Sucessfully Added", FancyToast.SUCCESS)
          finish()
        } else {
          showToast("Failed to add", FancyToast.ERROR)
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
      LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "shops")) {coverImage->
        val shop = Shop(
          clientShopSwitchMaterial.isChecked,
          false,
          arrayListOf(category_keys[categoriesSpinner.selectedItemPosition]),
          arrayListOf(),
          bookTitle.text.toString(),
          shop_order.toInt(),
          "",
          coverImage,
          it,
          "",
          location.text.toString(),
          delivery_charge.text.toString().toInt(),
          da_charge.text.toString().toInt(),
          arrayListOf(),
          null
        )
        LiveDataUtil.observeOnce(viewModel.createShopItem(shop)) { shop ->
          dialog.dismiss()
          if (shop.id != null) {
            showToast("Sucessfully Added", FancyToast.SUCCESS)
            finish()
          } else {
            showToast("Failed to add", FancyToast.ERROR)
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
          imageName = key
        } else {
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