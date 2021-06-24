package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.db.model.SlidingTextItem
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.home.AddOffers
import admin.arpan.delivery.ui.order.OrdresActivity
import admin.arpan.delivery.ui.settings.SettingActivity
import admin.arpan.delivery.ui.shops.ShopsActivity
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.*
import kotlinx.android.synthetic.main.switch_textview_item_file.view.*
import java.lang.reflect.Field

class NormalBannersPopUpAdapter(val context : Context,
                                val locations : ArrayList<SlidingTextItem>)
    : RecyclerView.Adapter<NormalBannersPopUpAdapter.RecyclerViewHolder>(){

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val linearLayoutSwitch = itemView.linearLayoutSwitch as LinearLayout
        val titleTextView = itemView.titleTextView as TextView
        val switchMaterial = itemView.switchMaterial as SwitchMaterial
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.switch_textview_item_file,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.titleTextView.text = locations[position].textTitle
        holder.switchMaterial.isChecked = locations[position].enabled
        holder.switchMaterial.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(!locations[position].enabled){
                    locations[position].enabled = true
                    holder.switchMaterial.isEnabled = false
                    val hashMap = HashMap<String,Any>()
                    hashMap[locations[position].key] = locations[position]
                    FirebaseFirestore.getInstance()
                        .collection(Constants.FC_OFFERS_OI)
                        .document("normal_notifications_document")
                        .update(hashMap)
                        .addOnCompleteListener {
                            holder.switchMaterial.isEnabled = true
                        }
                }
            }else{
                if(locations[position].enabled) {
                    locations[position].enabled = false
                    holder.switchMaterial.isEnabled = false
                    val hashMap = HashMap<String,Any>()
                    hashMap[locations[position].key] = locations[position]
                    FirebaseFirestore.getInstance()
                        .collection(Constants.FC_OFFERS_OI)
                        .document("normal_notifications_document")
                        .update(hashMap)
                        .addOnCompleteListener {
                            holder.switchMaterial.isEnabled = true
                        }
                }
            }
        }

        holder.linearLayoutSwitch.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            val locationAlertDialogViewMain = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_normal_banner, null)
            var textColor = "#ffffff"
            var bgColor = "#43A047"
            textColor = locations[position].textColorHex
            bgColor = locations[position].backgroundColorHex
            locationAlertDialogViewMain.specialOfferTextView.setTextColor(Color.parseColor(textColor))
            locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(Color.parseColor(bgColor))
            locationAlertDialogViewMain.specialOfferTextView.text = locations[position].textTitle
            locationAlertDialogViewMain.edt_name.setText(locations[position].textTitle)
            locationAlertDialogViewMain.order.setText(locations[position].order.toString())
            locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
                ColorPickerDialog
                    .Builder(context)        				// Pass Activity Instance
                    .setTitle("Pick Text Color")           	// Default "Choose Color"
                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                    .setDefaultColor(textColor)     // Pass Default Color
                    .setColorListener { color, colorHex ->
                        textColor = colorHex
                        locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
                    }
                    .show()
            }
            locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
                ColorPickerDialog
                    .Builder(context)        				// Pass Activity Instance
                    .setTitle("Pick Background Color")           	// Default "Choose Color"
                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                    .setDefaultColor(bgColor)     // Pass Default Color
                    .setColorListener { color, colorHex ->
                        bgColor = colorHex
                        locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(color)
                    }
                    .show()
            }
            locationAlertDialogViewMain.edt_name.doOnTextChanged { text, start, before, count ->
                locationAlertDialogViewMain.specialOfferTextView.text = text
            }
            locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                if(locationAlertDialogViewMain.edt_name.text.isNotEmpty()){
                    locationAlertDialogViewMain.buttonTextColor.isEnabled = false
                    locationAlertDialogViewMain.buttonBgColor.isEnabled = false
                    locationAlertDialogViewMain.edt_name.isEnabled = false
                    locationAlertDialogViewMain.order.isEnabled = false
                    locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                    val d = SlidingTextItem()
                    d.key = locations[position].key
                    d.enabled = true
                    d.textTitle = locationAlertDialogViewMain.edt_name.text.toString()
                    d.textDescription = ""
                    d.timeBased = false
                    d.backgroundColorHex = bgColor
                    d.textColorHex = textColor
                    d.order = if(locationAlertDialogViewMain.order.text.isEmpty()){
                        0
                    }else{
                        locationAlertDialogViewMain.order.text.toString().toLong()
                    }
                    val hashMap = HashMap<String,Any>()
                    hashMap[d.key] = d
                    FirebaseFirestore.getInstance()
                        .collection(Constants.FC_OFFERS_OI)
                        .document("normal_notifications_document")
                        .update(hashMap)
                        .addOnCompleteListener {
                            locations[position] = d
                            notifyItemChanged(position)
                            alertDialog.dismiss()
                        }
                }
            }
            alertDialog.setView(locationAlertDialogViewMain)
            alertDialog.show()
        }
        holder.linearLayoutSwitch.setOnLongClickListener {
            val progressDialog = context.createProgressDialog()
            val mDialog = android.app.AlertDialog.Builder(context)
                .setTitle("Are you sure to delete this location?")
                .setPositiveButton(
                    context.getString(R.string.yes_ok)
                ) { diaInt, _ ->
                    progressDialog.show()
                    val hashMap = HashMap<String,Any>()
                    hashMap[locations[position].key] = FieldValue.delete()
                    FirebaseFirestore.getInstance()
                        .collection(Constants.FC_OFFERS_OI)
                        .document("normal_notifications_document")
                        .update(hashMap)
                        .addOnCompleteListener {
                            locations.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, locations.size)
                            progressDialog.dismiss()
                        }
                    diaInt.dismiss()
                }
                .setNegativeButton(
                    context.getString(R.string.no_its_ok)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
            mDialog.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }


}