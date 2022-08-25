package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.models.Location
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.viewModels.LocationViewModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_add_location.view.*
import kotlinx.android.synthetic.main.location_item_recycler_view.view.*

class LocationItemRecyclerAdapter(
  val context: Context,
  val locations: ArrayList<Location>,
  val firebaseDatabaseLocation: String,
  val locationViewModel: LocationViewModel
) : RecyclerView.Adapter<LocationItemRecyclerAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
    holder.deliveryCharge.text = "ডেলিভারি চার্জঃ " + locations[position].deliveryCharge
    holder.daCharge.text = "ডিএ চার্জঃ " + locations[position].daCharge
    holder.locationLinearMain.setOnClickListener {
      val alertDialog = AlertDialog.Builder(context).create()
      val locationAlertDialogViewMain = LayoutInflater.from(context)
        .inflate(R.layout.dialog_add_location, null)
      if (firebaseDatabaseLocation == "delivery_charges") {
        locationAlertDialogViewMain.edt_client_delivery_charge_container.visibility = View.VISIBLE
        locationAlertDialogViewMain.edt_client_delivery_charge.setText(locations[position].deliveryChargeClient.toString())

        locationAlertDialogViewMain.edt_location_name.setText(locations[position].locationName)
        locationAlertDialogViewMain.edt_delivery_charge.setText(locations[position].deliveryCharge.toString())
        locationAlertDialogViewMain.edt_da_charge.setText(locations[position].daCharge.toString())
        locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
          val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
          val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
          val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
          val clientDeliveryCharge =
            locationAlertDialogViewMain.edt_client_delivery_charge.text.toString()
          if (
            locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
            && daCharge.isNotEmpty() && clientDeliveryCharge.isNotEmpty()
          ) {
            locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
            locationAlertDialogViewMain.edt_location_name.isEnabled = false
            locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
            locationAlertDialogViewMain.edt_da_charge.isEnabled = false
            locationAlertDialogViewMain.edt_client_delivery_charge.isEnabled = false
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            val key = locations[position].id!!
            val locationItem = HashMap<String, Any>()
            locationItem["locationName"] = locationName
            locationItem["deliveryCharge"] = deliveryCharge
            locationItem["daCharge"] = daCharge
            locationItem["deliveryChargeClient"] = clientDeliveryCharge
            LiveDataUtil.observeOnce(locationViewModel.updateItem(key, locationItem)) {
              alertDialog.dismiss()
              if (it.id != null) {
                locations[position] = Location(
                  id = it.id!!,
                  locationName = locationName,
                  deliveryCharge = deliveryCharge.toInt(),
                  deliveryChargeClient = clientDeliveryCharge.toInt(),
                  daCharge = daCharge.toInt()
                )
                notifyItemChanged(position)
              }
            }
          }
        }
        alertDialog.setView(locationAlertDialogViewMain)
        alertDialog.show()
      } else {
        locationAlertDialogViewMain.edt_location_name.setText(locations[position].locationName)
        locationAlertDialogViewMain.edt_delivery_charge.setText(locations[position].deliveryCharge.toString())
        locationAlertDialogViewMain.edt_da_charge.setText(locations[position].daCharge.toString())
        locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
          val locationName = locationAlertDialogViewMain.edt_location_name.text.toString()
          val deliveryCharge = locationAlertDialogViewMain.edt_delivery_charge.text.toString()
          val daCharge = locationAlertDialogViewMain.edt_da_charge.text.toString()
          if (
            locationName.isNotEmpty() && deliveryCharge.isNotEmpty()
            && daCharge.isNotEmpty()
          ) {
            locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
            locationAlertDialogViewMain.edt_location_name.isEnabled = false
            locationAlertDialogViewMain.edt_delivery_charge.isEnabled = false
            locationAlertDialogViewMain.edt_da_charge.isEnabled = false
            locationAlertDialogViewMain.edt_client_delivery_charge.isEnabled = false
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            val key = locations[position].id!!
            val locationItem = HashMap<String, Any>()
            locationItem["locationName"] = locationName
            locationItem["deliveryCharge"] = deliveryCharge
            locationItem["daCharge"] = daCharge
            LiveDataUtil.observeOnce(locationViewModel.updateItem(key, locationItem)) {
              alertDialog.dismiss()
              if (it.id != null) {
                locations[position] = Location(
                  id = it.id!!,
                  locationName = locationName,
                  deliveryCharge = deliveryCharge.toInt(),
                  daCharge = daCharge.toInt()
                )
                notifyItemChanged(position)
              }
            }
          }
        }
        alertDialog.setView(locationAlertDialogViewMain)
        alertDialog.show()
      }
    }
    holder.locationLinearMain.setOnLongClickListener {
      val progressDialog = context.createProgressDialog()
      val mDialog = android.app.AlertDialog.Builder(context)
        .setTitle("Are you sure to delete this location?")
        .setPositiveButton(
          context.getString(R.string.yes_ok)
        ) { diaInt, _ ->
          progressDialog.show()
          LiveDataUtil.observeOnce(locationViewModel.deleteItem(locations[position].id!!)){
            progressDialog.dismiss()
            if(it.error != true){
              locations.removeAt(position)
              notifyItemRemoved(position)
              notifyItemRangeChanged(position, locations.size)
            }
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