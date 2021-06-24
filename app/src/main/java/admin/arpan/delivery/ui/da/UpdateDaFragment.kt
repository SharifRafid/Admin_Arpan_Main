package admin.arpan.delivery.ui.da

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_add_da.view.*
import java.io.File
import java.lang.Exception

class UpdateDaFragment : DialogFragment() {

    private var imagePath = Uri.parse("")
    private lateinit var mainView: View
    private var daCount = 0
    var selectedDaAgent = DaAgent()
    var selectedAgentPostition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_da, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view
        selectedDaAgent = (activity as DaActivity).selectedDaAgent
        selectedAgentPostition = (activity as DaActivity).selectedAgentPostition
        view.productTitle.setText(selectedDaAgent.da_name)
        view.bloodGroupTitle.setText(selectedDaAgent.da_blood_group)
        view.productDesc.setText(selectedDaAgent.da_password)
        view.price.setText(selectedDaAgent.da_mobile)
        view.offerPrice.setText(selectedDaAgent.da_bkash)
        if(selectedDaAgent.da_image.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance()
                .getReference("da_storage_image_location")
                .child(selectedDaAgent.da_image)

            Glide.with(requireActivity())
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image)
                .into(view.imagePick)
        }

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
            val password = view.productDesc.text.toString()
            val mobile = view.price.text.toString()
            val bkashNumber = view.offerPrice.text.toString()
            if(userName.isNotEmpty() && password.isNotEmpty() && mobile.isNotEmpty()){
                if(imagePath.toString().isEmpty()){
                    progress.show()
                    val daAgent = HashMap<String, Any>()
                    daAgent["da_name"] = userName
                    daAgent["da_mobile"] = mobile
                    daAgent["da_bkash"] = bkashNumber
                    daAgent["da_password"] = password
                    daAgent["da_blood_group"] = bloodGroup
                    daAgent["da_category"] = if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                        "রেগুলার"
                    }else{
                        "পারমানেন্ট"
                    }
                    FirebaseFirestore.getInstance()
                        .collection("da_agents_main_list_collection")
                        .document(selectedDaAgent.key)
                        .update(daAgent).addOnCompleteListener {
                            FirebaseDatabase.getInstance().reference
                                .child("da_agents_realtime_details")
                                .child(selectedDaAgent.key)
                                .child("name")
                                .setValue(userName).addOnCompleteListener { _ ->
                                    (activity as DaActivity).daList[selectedAgentPostition].da_name =  userName
                                    (activity as DaActivity).daList[selectedAgentPostition].da_mobile =  mobile
                                    (activity as DaActivity).daList[selectedAgentPostition].da_bkash =  bkashNumber
                                    (activity as DaActivity).daList[selectedAgentPostition].da_password =  password
                                    (activity as DaActivity).daList[selectedAgentPostition].da_blood_group =  bloodGroup
                                    (activity as DaActivity).daList[selectedAgentPostition].da_category =
                                        if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                                            "রেগুলার"
                                        }else{
                                            "পারমানেন্ট"
                                        }
                                    (activity as DaActivity).daItemRecyclerAdapter.notifyItemChanged(selectedAgentPostition)
                                    progress.dismiss()
                                    dismiss()
                                }
                        }
                }else{
                    progress.show()
                    val key = if(selectedDaAgent.da_image.isEmpty()){
                        "DA${System.currentTimeMillis()}"
                    }else{
                        selectedDaAgent.da_image
                    }
                    FirebaseStorage.getInstance()
                            .reference
                            .child("da_storage_image_location")
                            .child(key)
                            .putFile(imagePath)
                            .addOnSuccessListener {
                                val daAgent = HashMap<String, Any>()
                                daAgent["da_name"] = userName
                                daAgent["da_mobile"] = mobile
                                daAgent["da_bkash"] = bkashNumber
                                daAgent["da_password"] = password
                                daAgent["da_blood_group"] = bloodGroup
                                daAgent["da_category"] = if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                                    "রেগুলার"
                                }else {
                                    "পারমানেন্ট"
                                }
                                FirebaseFirestore.getInstance()
                                    .collection("da_agents_main_list_collection")
                                    .document(selectedDaAgent.key)
                                    .update(daAgent).addOnCompleteListener {
                                        FirebaseDatabase.getInstance().reference
                                            .child("da_agents_realtime_details")
                                            .child(selectedDaAgent.key)
                                            .child("name")
                                            .setValue(userName).addOnCompleteListener { _ ->
                                                (activity as DaActivity).daList[selectedAgentPostition].da_name =  userName
                                                (activity as DaActivity).daList[selectedAgentPostition].da_mobile =  mobile
                                                (activity as DaActivity).daList[selectedAgentPostition].da_bkash =  bkashNumber
                                                (activity as DaActivity).daList[selectedAgentPostition].da_password =  password
                                                (activity as DaActivity).daList[selectedAgentPostition].da_blood_group =  bloodGroup
                                                (activity as DaActivity).daList[selectedAgentPostition].da_image =  key
                                                (activity as DaActivity).daList[selectedAgentPostition].da_category =
                                                    if(view.radioGroup1.checkedRadioButtonId==R.id.regularRadio){
                                                        "রেগুলার"
                                                    }else{
                                                        "পারমানেন্ট"
                                                    }
                                                (activity as DaActivity).daItemRecyclerAdapter.notifyItemChanged(selectedAgentPostition)
                                                progress.dismiss()
                                                dismiss()
                                            }
                                    }
                            }
                }
            }
        }
    }


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