package admin.arpan.delivery.ui.da

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.models.User
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.networking.responses.DaItemResponse
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.DAViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_da.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.lang.ClassCastException
import java.lang.Exception

@AndroidEntryPoint
class AddDaFragment : Fragment() {

  private var imagePath = Uri.parse("")
  private lateinit var mainView: View
  private var TAG = "AddDaFragment"
  lateinit var homeMainNewInterface: HomeMainNewInterface
  private val uploadViewModel: UploadViewModel by viewModels()

  private val daViewModel: DAViewModel by viewModels()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    try {
      homeMainNewInterface = context as HomeMainNewInterface
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_add_da, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    mainView = view
    view.imagePick.setOnClickListener {
//            val getImageIntent = Intent(Intent.ACTION_GET_CONTENT)
//            getImageIntent.type = "image/*"
//            startActivityForResult(
//                Intent.createChooser(
//                    getImageIntent,
//                    "Select Picture"
//                ), PICK_IMAGE_CODE
//            )
      ImagePicker.with(this)
        .cropSquare()            //Crop image(Optional), Check Customization for more option
        .compress(40)      //Final image size will be less than 1 MB(Optional)
        .maxResultSize(512, 512)  //Final image resolution will be less than 1080 x 1080(Optional)
        .start()
    }
    val progress = view.context.createProgressDialog()
    view.upload.setOnClickListener {
      val userName = view.productTitle.text.toString()
      val bloodGroup = view.bloodGroupTitle.text.toString()
      val mobile = view.price.text.toString()
      val bkashNumber = view.offerPrice.text.toString()
      val daIDString = view.daId.text.toString()
      if (userName.isNotEmpty() && mobile.isNotEmpty() && daIDString.isNotEmpty()) {
        if (imagePath.toString().isEmpty()) {
          val hashMap = User()
          hashMap.name = userName
          hashMap.daUID = daIDString
          hashMap.phone = mobile
          hashMap.bkash = bkashNumber
          hashMap.roles = arrayListOf("da")
          hashMap.bloodGroup = bloodGroup
          hashMap.daCategory = if (view.radioGroup1.checkedRadioButtonId == R.id.regularRadio) {
            Constants.DA_REG
          } else {
            Constants.DA_PERM
          }
          hashMap.daStatus = false
          progress.show()
          LiveDataUtil.observeOnce(daViewModel.createItem(hashMap)) {
            progress.dismiss()
            if (it.id != null) {
              hashMap.id = it.id
              homeMainNewInterface.callOnBackPressed()
              context?.showToast("Sucessfully Added", FancyToast.SUCCESS)
            } else {
              context?.showToast("Failed to add", FancyToast.ERROR)
            }
          }
        } else {
          progress.show()
          val imageName = "da${System.currentTimeMillis()}"
          // Pass it like this
          val file = imagePath.toFile()
          val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/png"), file)
          // MultipartBody.Part is used to send also the actual file name
          val body: MultipartBody.Part = MultipartBody.Part.createFormData(
            "fileName",
            imageName + ".png",
            requestFile
          )
          LiveDataUtil.observeOnce(uploadViewModel.uploadItem(body, "users")) { image ->
            if (!image.path.isNullOrEmpty()) {
              val hashMap = User()
              hashMap.name = userName
              hashMap.daUID = daIDString
              hashMap.phone = mobile
              hashMap.bkash = bkashNumber
              hashMap.roles = arrayListOf("da")
              hashMap.bloodGroup = bloodGroup
              hashMap.daCategory = if (view.radioGroup1.checkedRadioButtonId == R.id.regularRadio) {
                Constants.DA_REG
              } else {
                Constants.DA_PERM
              }
              hashMap.image = image
              hashMap.daStatus = false
              LiveDataUtil.observeOnce(daViewModel.createItem(hashMap)) {
                progress.dismiss()
                if (it.id != null) {
                  hashMap.id = it.id
                  homeMainNewInterface.callOnBackPressed()
                  context?.showToast("Sucessfully Added", FancyToast.SUCCESS)
                } else {
                  context?.showToast("Failed to add", FancyToast.ERROR)
                }
              }
            } else {
              progress.dismiss()
              context?.showToast("Failed to add", FancyToast.ERROR)
            }
          }
        }
      }
    }
  }


  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      imagePath = data!!.data!!
      mainView.imagePick.setImageURI(imagePath)
      mainView.imagePick.setPadding(0, 0, 0, 0)
    } else if (resultCode == ImagePicker.RESULT_ERROR) {
    } else {
    }
  }
}