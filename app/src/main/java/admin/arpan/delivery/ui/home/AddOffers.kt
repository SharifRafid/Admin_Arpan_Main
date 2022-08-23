package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.models.Banner
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.BannerViewModel
import admin.arpan.delivery.viewModels.HomeViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_offers.*
import kotlinx.android.synthetic.main.fragment_add_da.view.*
import kotlinx.android.synthetic.main.offers_item_view.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class AddOffers : AppCompatActivity() {

  val PICK_IMAGE_CODE = 121
  private lateinit var imagePath: Uri
  private lateinit var databaseReference: FirebaseFirestore
  private lateinit var dialog: Dialog
  private var imageName = "OfferImageName"
  lateinit var offerItemRecyclerAdapter: OfferItemRecyclerAdapter
  private val bannerViewModel: BannerViewModel by viewModels()
  private val uploadViewModel: UploadViewModel by viewModels()

  val arrayList = ArrayList<String>()
  val keyList = ArrayList<String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_offers)


    initVars()
    initOnClicks()

    LiveDataUtil.observeOnce(bannerViewModel.getAllItems()) {
      arrayList.clear()
      keyList.clear()
      val map = it.results
      for (docField in map) {
        keyList.add(docField.id!!)
        arrayList.add(docField.icon!!.path!!)
      }
      val linearLayoutManager = LinearLayoutManager(this@AddOffers)
      linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
      mainRecyclerView.layoutManager = linearLayoutManager
      offerItemRecyclerAdapter = OfferItemRecyclerAdapter(
        this@AddOffers,
        arrayList, keyList,
        bannerViewModel
      )
      mainRecyclerView.adapter = offerItemRecyclerAdapter
    }
  }

  private fun initVars() {
    dialog = createProgressDialog()
    databaseReference = FirebaseFirestore.getInstance()
  }

  private fun initOnClicks() {
    upload2.setOnClickListener {
      ImagePicker.with(this)
        .crop(200f, 70f)            //Crop image(Optional), Check Customization for more option
        .compress(256)      //Final image size will be less than 1 MB(Optional)
        .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
        .start()
    }
    title_text_view.setOnClickListener {
      onBackPressed()
    }
  }

  private fun uploadFile() {
    dialog.show()
    imageName = "OFR" + System.currentTimeMillis()
    // Pass it like this
    val file = imagePath.toFile()
    val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
    // MultipartBody.Part is used to send also the actual file name
    val body: MultipartBody.Part = MultipartBody.Part.createFormData(
      "fileName",
      imageName + ".png",
      requestFile
    )
    LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "banners")) { image ->
      if (!image.path.isNullOrEmpty()) {
        val hashMap = Banner()
        hashMap.order = 1
        hashMap.icon = image
        LiveDataUtil.observeOnce(bannerViewModel.createItem(hashMap)) {
          dialog.dismiss()
          if (it.id != null) {
            arrayList.add(image.path!!)
            keyList.add(it.id!!)
            offerItemRecyclerAdapter.notifyItemInserted(arrayList.size - 1)
            Toast.makeText(
              this,
              "Success", Toast.LENGTH_SHORT
            ).show()
          } else {
            Toast.makeText(
              this,
              "Failed", Toast.LENGTH_SHORT
            ).show()
          }
        }
      } else {
        dialog.dismiss()
        showToast("Failed to add", FancyToast.ERROR)
      }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      val fullPhotoUri = data!!.data
      imagePath = fullPhotoUri!!
      imageName = "OFR" + System.currentTimeMillis()
      dialog.show()
      if (imagePath.toString().isNotEmpty()) {
        uploadFile()
      } else {
        Toast.makeText(
          this, "fill everything",
          Toast.LENGTH_SHORT
        ).show()
      }
    } else if (resultCode == ImagePicker.RESULT_ERROR) {
    } else {
    }
  }
}


class OfferItemRecyclerAdapter(
  private val context: Activity,
  private val images: ArrayList<String>,
  private val keyList: ArrayList<String>,
  private val bannerViewModel: BannerViewModel
) : RecyclerView.Adapter
<OfferItemRecyclerAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.imageView as ImageView
    val cardView = itemView.cardView as CardView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    val view = LayoutInflater.from(context).inflate(
      R.layout.offers_item_view, parent,
      false
    )
    return RecyclerViewHolder(view)
  }

  override fun getItemCount(): Int {
    return images.size
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    holder.cardView.setOnLongClickListener {
      val dia = AlertDialog.Builder(context)
      dia.setTitle("আপনি কি নিশ্চিত ?")
        .setMessage("আপনি অফার ডিলেট করতে যাচ্ছেন")
        .setCancelable(true)
        .setPositiveButton(
          "ডিলেট করুন!"
        ) { di, _ ->
          LiveDataUtil.observeOnce(bannerViewModel.deleteItem(keyList[position])) {
            if (it.error == true) {
              Toast.makeText(
                context, "FAILED",
                Toast.LENGTH_SHORT
              ).show()
            } else {
              images.removeAt(position)
              keyList.removeAt(position)
              notifyItemRemoved(position)
              notifyItemRangeChanged(position, images.size)
              Toast.makeText(
                context, "SUCCESSFULLY DELETED",
                Toast.LENGTH_SHORT
              ).show()
            }
          }
          di.dismiss()
        }
        .setNegativeButton(
          "না। থাক।"
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        .create().show()
      true
    }

    Glide.with(context)
      .load(Constants.SERVER_FILES_BASE_URL + images[position])
      .fitCenter()
      .diskCacheStrategy(DiskCacheStrategy.ALL)
      .into(holder.imageView)
  }


}