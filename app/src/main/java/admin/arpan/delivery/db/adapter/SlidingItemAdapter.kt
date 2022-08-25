package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.models.SlidingTextItem
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.viewModels.NoticeViewModel
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.addLocationConfirmButton
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonBgColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.buttonTextColor
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.edt_name
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.order
import kotlinx.android.synthetic.main.dialog_add_normal_banner.view.specialOfferTextView
import kotlinx.android.synthetic.main.dialog_add_time_based_banner.view.*
import kotlinx.android.synthetic.main.switch_textview_item_file.view.*

class SlidingItemAdapter(
  val context: Context,
  val locations: ArrayList<SlidingTextItem>,
  val noticeViewModel: NoticeViewModel,
  val timeBased: Boolean
) : RecyclerView.Adapter<SlidingItemAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
      if (isChecked) {
        if (!locations[position].enabled) {
          locations[position].enabled = true
          holder.switchMaterial.isEnabled = false
          val hashMap = HashMap<String, Any>()
          hashMap["enabled"] = true
          LiveDataUtil.observeOnce(noticeViewModel.updateItem(locations[position].id!!, hashMap)) {
            holder.switchMaterial.isEnabled = true
          }
        }
      } else {
        if (locations[position].enabled) {
          locations[position].enabled = false
          holder.switchMaterial.isEnabled = false
          val hashMap = HashMap<String, Any>()
          hashMap["enabled"] = false
          LiveDataUtil.observeOnce(noticeViewModel.updateItem(locations[position].id!!, hashMap)) {
            holder.switchMaterial.isEnabled = true
          }
        }
      }
    }

    holder.linearLayoutSwitch.setOnClickListener {
      val alertDialog = AlertDialog.Builder(context).create()
      val locationAlertDialogViewMain = LayoutInflater.from(context)
        .inflate(R.layout.dialog_add_time_based_banner, null)
      var textColor = "#ffffff"
      var bgColor = "#43A047"
      textColor = locations[position].textColorHex
      bgColor = locations[position].backgroundColorHex
      locationAlertDialogViewMain.specialOfferTextView.setTextColor(Color.parseColor(textColor))
      locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(Color.parseColor(bgColor))
      locationAlertDialogViewMain.specialOfferTextView.text = locations[position].textTitle
      locationAlertDialogViewMain.edt_name.setText(locations[position].textTitle)
      locationAlertDialogViewMain.edt_start_time.setText(locations[position].startTimeString)
      locationAlertDialogViewMain.edt_end_time.setText(locations[position].endTimeString)
      locationAlertDialogViewMain.order.setText(locations[position].order.toString())
      locationAlertDialogViewMain.buttonTextColor.setOnClickListener {
        MaterialColorPickerDialog
          .Builder(context)          // Pass Activity Instance
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
            textColor = colorHex
            locationAlertDialogViewMain.specialOfferTextView.setTextColor(color)
          }
          .show()
      }
      locationAlertDialogViewMain.buttonBgColor.setOnClickListener {
        MaterialColorPickerDialog
          .Builder(context)          // Pass Activity Instance
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
            bgColor = colorHex
            locationAlertDialogViewMain.specialOfferTextView.setBackgroundColor(color)
          }
          .show()
      }
      locationAlertDialogViewMain.edt_name.doOnTextChanged { text, start, before, count ->
        locationAlertDialogViewMain.specialOfferTextView.text = text
      }
      locationAlertDialogViewMain.addLocationConfirmButton.setOnClickListener {
        if (locationAlertDialogViewMain.edt_name.text.isNotEmpty()) {
          locationAlertDialogViewMain.buttonTextColor.isEnabled = false
          locationAlertDialogViewMain.buttonBgColor.isEnabled = false
          locationAlertDialogViewMain.edt_name.isEnabled = false
          locationAlertDialogViewMain.order.isEnabled = false
          locationAlertDialogViewMain.addLocationConfirmButton.isEnabled = false
          val d = HashMap<String, Any>()
          d["enabled"] = true
          d["textTitle"] = locationAlertDialogViewMain.edt_name.text.toString()
          d["startTimeString"] = locationAlertDialogViewMain.edt_start_time.text.toString()
          d["endTimeString"] = locationAlertDialogViewMain.edt_end_time.text.toString()
          d["textDescription"] = ""
          d["timeBased"] = timeBased
          d["backgroundColorHex"] = bgColor
          d["textColorHex"] = textColor
          d["order"] = if (locationAlertDialogViewMain.order.text.isEmpty()) {
            0
          } else {
            locationAlertDialogViewMain.order.text.toString().toLong()
          }
          LiveDataUtil.observeOnce(noticeViewModel.updateItem(locations[position].id!!, d)) {
            if (it.id != null) {
              locations[position] = it
              notifyItemChanged(position)
            }
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
          LiveDataUtil.observeOnce(noticeViewModel.deleteItem(locations[position].id!!)) {
            if (it.error != true) {
              locations.removeAt(position)
              notifyItemRemoved(position)
              notifyItemRangeChanged(position, locations.size)
              progressDialog.dismiss()
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