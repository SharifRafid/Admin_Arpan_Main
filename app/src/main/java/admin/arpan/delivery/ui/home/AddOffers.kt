package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
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
import androidx.cardview.widget.CardView
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
import kotlinx.android.synthetic.main.activity_add_offers.*
import kotlinx.android.synthetic.main.fragment_add_da.view.*
import kotlinx.android.synthetic.main.offers_item_view.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddOffers : AppCompatActivity() {

    val PICK_IMAGE_CODE = 121
    private lateinit var imagePath : Uri
    private lateinit var databaseReference: FirebaseFirestore
    private lateinit var dialog : Dialog
    private var imageName = "OfferImageName"
    lateinit var offerItemRecyclerAdapter: OfferItemRecyclerAdapter

    val arrayList = ArrayList<String>()
    val keyList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_offers)


        initVars()
        initOnClicks()

        databaseReference.collection(Constants.FC_OFFERS_OI)
            .document(Constants.FD_OFFERS_OID)
            .get().addOnCompleteListener {
                Log.e("DATA","COMPLETE")
                if(it.isSuccessful){
                    if(it.result!!.data!!.entries.isNotEmpty()){
                        Log.e("DATA",it.result!!.data!!.entries.size.toString())
                        arrayList.clear()
                        keyList.clear()
                        val map = it.result!!.data!! as Map<String, Map<String, String>>
                        for(docField in map.entries){
                            keyList.add(docField.key)
                            arrayList.add(docField.value[Constants.FIELD_FD_OFFERS_OID_LOCATION].toString())
                        }
                        val linearLayoutManager = LinearLayoutManager(this@AddOffers)
                        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                        mainRecyclerView.layoutManager = linearLayoutManager
                        offerItemRecyclerAdapter = OfferItemRecyclerAdapter(this@AddOffers,
                            arrayList,keyList)
                        mainRecyclerView.adapter = offerItemRecyclerAdapter
                    }
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun initVars() {
        dialog = createProgressDialog()

        databaseReference = FirebaseFirestore.getInstance()
    }

    private fun initOnClicks() {
        upload2.setOnClickListener {
            ImagePicker.with(this)
                .crop(200f, 70f)	    			//Crop image(Optional), Check Customization for more option
                .compress(256)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        title_text_view.setOnClickListener {
            onBackPressed()
        }
    }

    private fun uploadFile() {
        imageName = "OFR"+System.currentTimeMillis()
        FirebaseStorage.getInstance()
            .getReference(Constants.FS_OFFERS_OI)
            .child(imageName)
            .putFile(imagePath)
            .addOnSuccessListener {
                val hashMap = HashMap<String,HashMap<String,String>>()
                val hashMap2 = HashMap<String,String>()
                hashMap2[Constants.FIELD_FD_OFFERS_OID_LOCATION] = imageName
                hashMap2[Constants.FIELD_FD_OFFERS_OID_DESCRIPTION] = imageName
                hashMap2[Constants.FIELD_FD_OFFERS_OID_ORDER] = ""+arrayList.size
                hashMap[imageName] = hashMap2
                databaseReference.collection(Constants.FC_OFFERS_OI)
                    .document(Constants.FD_OFFERS_OID)
                    .update(hashMap as Map<String, Any>).addOnCompleteListener {
                        dialog.dismiss()
                        arrayList.add(imageName)
                        keyList.add(imageName)
                        offerItemRecyclerAdapter.notifyItemInserted(arrayList.size-1)
                        Toast.makeText(
                            this,
                            "Success", Toast.LENGTH_SHORT
                        ).show()
                    }

            }
            .addOnProgressListener {
                val whole = it.totalByteCount / 1024 .toFloat()
                val passed = it.bytesTransferred / 1024 .toFloat()
                Log.e("UPLOAD PROGRESS", "Uploaded $passed of $whole kb")
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fullPhotoUri = data!!.data
            imagePath = fullPhotoUri!!
            imageName = getFileName(imagePath)!!
            dialog.show()
            if(imagePath.toString().isNotEmpty()){
                uploadFile()
            }else{
                Toast.makeText(this,"fill everything",
                    Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
        } else {
        }
    }
}



class OfferItemRecyclerAdapter(
    private val context : Activity,
    private val images : ArrayList<String>,
    private val keyList : ArrayList<String>) : RecyclerView.Adapter
<OfferItemRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.imageView as ImageView
        val cardView = itemView.cardView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.offers_item_view, parent,
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        val storageReference = FirebaseStorage.getInstance().getReference(Constants.FS_OFFERS_OI)
            .child(images[position])

        holder.cardView.setOnLongClickListener {
            val dia = AlertDialog.Builder(context)
            dia.setTitle("আপনি কি নিশ্চিত ?")
                .setMessage("আপনি অফার ডিলেট করতে যাচ্ছেন")
                .setCancelable(true)
                .setPositiveButton(
                    "ডিলেট করুন!"
                ) { di, _ ->
                    storageReference.delete().addOnSuccessListener {
                        val hashMap = HashMap<String,Any>()
                        hashMap[keyList[position]] = FieldValue.delete()
                        FirebaseFirestore.getInstance().collection(Constants.FC_OFFERS_OI)
                            .document(Constants.FD_OFFERS_OID)
                            .update(hashMap).addOnCompleteListener {
                                if(it.isSuccessful){
                                    images.removeAt(position)
                                    keyList.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, images.size)
                                    Toast.makeText(context, "SUCCESSFULLY DELETED",
                                        Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(context, "FAILED",
                                        Toast.LENGTH_SHORT).show()
                                }
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
            .load(storageReference)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageView)
    }



}