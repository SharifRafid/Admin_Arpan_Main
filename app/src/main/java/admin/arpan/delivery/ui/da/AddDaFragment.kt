package admin.arpan.delivery.ui.da

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add_da.view.*
import java.io.File
import java.lang.ClassCastException
import java.lang.Exception

class AddDaFragment : Fragment() {

    private var imagePath = Uri.parse("")
    private lateinit var mainView: View
    private var TAG = "AddDaFragment"
    lateinit var homeMainNewInterface: HomeMainNewInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
                .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                .compress(40)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        val progress = view.context.createProgressDialog()
        view.upload.setOnClickListener {
            val userName = view.productTitle.text.toString()
            val bloodGroup = view.bloodGroupTitle.text.toString()
            val password = ""
            val mobile = view.price.text.toString()
            val bkashNumber = view.offerPrice.text.toString()
            val daIDString = view.daId.text.toString()
            if(userName.isNotEmpty() && mobile.isNotEmpty()&& daIDString.isNotEmpty()){
                if(imagePath.toString().isEmpty()){
                    progress.show()
                    val daAgent = DaAgent()
                    daAgent.da_uid = daIDString
                    daAgent.da_name = userName
                    daAgent.da_mobile = mobile
                    daAgent.da_bkash = bkashNumber
                    daAgent.da_password = password
                    daAgent.da_blood_group = bloodGroup
                    daAgent.da_category = if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                        "রেগুলার"
                    }else{
                        "পারমানেন্ট"
                    }
                    daAgent.da_image = ""
                    daAgent.da_address = ""
                    FirebaseFirestore.getInstance()
                        .collection("da_agents_main_list_collection")
                        .add(daAgent).addOnCompleteListener {
                            val mapDaDetails = HashMap<String,String>()
                            mapDaDetails["status"] = "inactive"
                            mapDaDetails["name"] = userName
                            FirebaseDatabase.getInstance().reference
                                .child("da_agents_realtime_details")
                                .child(it.result!!.id)
                                .setValue(mapDaDetails).addOnCompleteListener { _ ->
//                                    daAgent.key = it.result!!.id
//                                    (activity as DaActivity).daList.add(daAgent)
//                                    (activity as DaActivity).daItemRecyclerAdapter.notifyItemInserted((activity as DaActivity).daList.size-1)
                                    progress.dismiss()
                                    homeMainNewInterface.callOnBackPressed()
                                }
                        }
                }else{
                    progress.show()
                    val key = "DA${System.currentTimeMillis()}"
                    FirebaseStorage.getInstance()
                            .reference
                            .child("da_storage_image_location")
                            .child(key)
                            .putFile(imagePath)
                            .addOnSuccessListener {
                                val daAgent = DaAgent()
                                daAgent.da_uid = daIDString
                                daAgent.da_name = userName
                                daAgent.da_mobile = mobile
                                daAgent.da_bkash = bkashNumber
                                daAgent.da_password = password
                                daAgent.da_blood_group = bloodGroup
                                daAgent.da_category = if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                                    "রেগুলার"
                                }else{
                                    "পারমানেন্ট"
                                }
                                daAgent.da_image = key
                                daAgent.da_address = ""
                                FirebaseFirestore.getInstance()
                                        .collection("da_agents_main_list_collection")
                                        .add(daAgent).addOnCompleteListener {
                                        val mapDaDetails = HashMap<String,String>()
                                        mapDaDetails["status"] = "inactive"
                                        mapDaDetails["name"] = userName
                                            FirebaseDatabase.getInstance().reference
                                                    .child("da_agents_realtime_details")
                                                .child(it.result!!.id)
                                                    .setValue(mapDaDetails).addOnCompleteListener { _ ->
//                                                        daAgent.key = it.result!!.id
//                                                        (activity as DaActivity).daList.add(daAgent)
//                                                        (activity as DaActivity).daItemRecyclerAdapter.notifyItemInserted((activity as DaActivity).daList.size-1)
                                                        progress.dismiss()
                                                    homeMainNewInterface.callOnBackPressed()
                                                }
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
            mainView.imagePick.setPadding(0,0,0,0)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
        } else {
        }
    }
}