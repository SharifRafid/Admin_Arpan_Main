package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.LocationItem
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.dialog_add_location.view.*
import kotlinx.android.synthetic.main.location_item_recycler_view.view.*

class LocationItemRecyclerAdapter(val context : Context,
                                  val locations : ArrayList<LocationItem>,
                                    val firebaseDatabaseLocation : String)
    : RecyclerView.Adapter<LocationItemRecyclerAdapter.RecyclerViewHolder>(){

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val locationLinearMain = itemView.locationLinearMain as LinearLayout
        val locationName = itemView.locationName as TextView
        val deliveryCharge = itemView.deliveryCharge as TextView
        val daCharge = itemView.daCharge as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.location_item_recycler_view,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.locationName.text = locations[position].locationName
        holder.deliveryCharge.text = "ডেলিভারি চার্জঃ "+locations[position].deliveryCharge
        holder.daCharge.text = "ডিএ চার্জঃ "+locations[position].daCharge
        holder.locationLinearMain.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            val locationAlertDialogViewMain = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_location, null)
            locationAlertDialogViewMain.edt_location_name.setText(locations[position].locationName)
            locationAlertDialogViewMain.edt_delivery_charge.setText(locations[position].deliveryCharge.toString())
            locationAlertDialogViewMain.edt_da_charge.setText(locations[position].daCharge.toString())
            locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
                val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
                val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
                val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
                if(
                    locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
                    && daCharge.isNotEmpty()
                ){
                    locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
                    locationAlertDialogViewMain.edt_location_name.isEnabled = false
                    locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
                    locationAlertDialogViewMain.edt_da_charge.isEnabled = false
                    alertDialog.setCancelable(false)
                    alertDialog.setCanceledOnTouchOutside(false)
                    val key = locations[position].key
                    val locationItem = HashMap<String,String>()
                    locationItem["name"] = locationName
                    locationItem["deliveryCharge"] = deliveryCharge
                    locationItem["daCharge"] = daCharge
                    FirebaseDatabase.getInstance()
                        .reference
                        .child("data")
                        .child(firebaseDatabaseLocation)
                        .child(key)
                        .setValue(locationItem)
                        .addOnCompleteListener {
                            locations[position] = LocationItem(
                                key,
                                locationName,
                                deliveryCharge.toInt(),
                                daCharge.toInt()
                            )
                            notifyItemChanged(position)
                            alertDialog.dismiss()
                        }
                }
            }
            alertDialog.setView(locationAlertDialogViewMain)
            alertDialog.show()
        }
        holder.locationLinearMain.setOnLongClickListener {
            val progressDialog = context.createProgressDialog()
            val mDialog = android.app.AlertDialog.Builder(context)
                .setTitle("Are you sure to delete this location?")
                .setPositiveButton(
                    context.getString(R.string.yes_ok)
                ) { diaInt, _ ->
                    progressDialog.show()
                    FirebaseDatabase.getInstance()
                        .reference
                        .child("data")
                        .child(firebaseDatabaseLocation)
                        .child(locations[position].key)
                        .removeValue()
                        .addOnCompleteListener {
                            progressDialog.dismiss()
                            locations.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, locations.size)
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