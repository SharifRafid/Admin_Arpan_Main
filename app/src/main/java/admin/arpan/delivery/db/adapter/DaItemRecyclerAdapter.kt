package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.da.UpdateDaFragment
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.getGsonParser
import admin.arpan.delivery.utils.networking.responses.DaItemResponse
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.da_list_items.view.*

class DaItemRecyclerAdapter(
  val context: Context,
  val daAgents: ArrayList<DaItemResponse>,
  val homeMainNewInterface: HomeMainNewInterface,
  val daItemRecyclerAdapterInterface: DaItemRecyclerAdapterInterface
) : RecyclerView.Adapter<DaItemRecyclerAdapter.RecyclerViewHolder>() {

  val firebaseDatabase = FirebaseDatabase.getInstance().reference

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title_text_view = itemView.title_text_view
    val totalOrderThisMonthTextView = itemView.totalOrderThisMonthTextView
    val myIncomeTextView = itemView.myIncomeTextView
    val arpanBokeyaTextView = itemView.arpanBokeyaTextView
    val phone_text_view = itemView.phone_text_view
    val daIdTextView = itemView.daIdTextView
    val statusTextView = itemView.statusTextView
    val imageView = itemView.image_view
    val da_activity = itemView.da_activity
    val switchDaActivity = itemView.switchDaActivity
    val cardView = itemView.cardView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    return RecyclerViewHolder(
      LayoutInflater.from(context)
        .inflate(
          R.layout.da_list_items,
          parent,
          false
        )
    )
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    val daAgent = daAgents[position].daItem!!
    val statistics = daAgents[position].statistics
    holder.title_text_view.text = daAgent.name
    holder.daIdTextView.text = "ID#" + daAgent.daUID
    holder.phone_text_view.text = daAgent.phone.toString()
    holder.switchDaActivity.isChecked = daAgent.daStatus == true
    holder.switchDaActivity.setOnCheckedChangeListener { buttonView, isChecked ->
      daItemRecyclerAdapterInterface.onSwitchDAStatusCheckedChanged(position, daAgent, buttonView, isChecked)
    }

    if (daAgent.image != null) {
      if (daAgent.image!!.path != null) {
        Glide.with(context)
          .load(Constants.SERVER_FILES_BASE_URL + daAgent.image!!.path!!)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .centerCrop()
          .override(300, 300)
          .placeholder(R.drawable.test_shop_image)
          .into(holder.imageView)
      }
    }

    if (daAgent.daStatus == true) {
      holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_active))
    } else {
      holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_busy))
    }

    if (!daAgent.daStatusTitle.isNullOrEmpty()) {
      holder.statusTextView.visibility = View.VISIBLE
      holder.statusTextView.text = daAgent.daStatusTitle
    } else {
      holder.statusTextView.visibility = View.GONE
    }

    holder.cardView.setOnClickListener {
      val bundle = Bundle()
      bundle.putString("data", getGsonParser()?.toJson(daAgent))
      homeMainNewInterface.navigateToFragment(R.id.daStatsFragment, bundle)
    }

    holder.cardView.setOnLongClickListener {
      val dialogMain = AlertDialog.Builder(context)
        .setPositiveButton("Edit", DialogInterface.OnClickListener { dialog_main, which ->
          val bundle = Bundle()
          bundle.putString("data", getGsonParser()?.toJson(daAgent))
          homeMainNewInterface.navigateToFragment(R.id.updateDaFragment, bundle)
        })
        .setNegativeButton("Delete", DialogInterface.OnClickListener { dialog_main, which ->
          val mDialog = AlertDialog.Builder(context)
            .setTitle("Are you sure to delete this agent?")
            .setPositiveButton(
              context.getString(R.string.yes_ok)
            ) { diaInt, _ ->
              diaInt.dismiss()
              daItemRecyclerAdapterInterface.deleteDA(position, daAgent)
            }
            .setNegativeButton(
              context.getString(R.string.no_its_ok)
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
          mDialog.show()
        })
        .create().show()
      true
    }

    if (daAgent.daCategory == Constants.DA_PERM) {
      holder.myIncomeTextView.text = statistics.agentsIncomePermanent.toString()
      holder.arpanBokeyaTextView.text = statistics.agentsDueToArpanPermanent.toString()
//            holder.arpanBokeyaTextView.text = (calculationResult!!.totalOrders-calculationResult.completed).toString()
//            holder.myIncomeTextView.text = calculationResult.completed.toString()
    } else {
      holder.myIncomeTextView.text = statistics.agentsIncome.toString()
      holder.arpanBokeyaTextView.text = statistics.agentsDueToArpan.toString()
//            holder.arpanBokeyaTextView.text = (calculationResult!!.totalOrders-calculationResult.completed).toString()
//            holder.myIncomeTextView.text = calculationResult.completed.toString()
    }
    holder.totalOrderThisMonthTextView.text = statistics.totalOrders.toString()
  }

  override fun getItemCount(): Int {
    return daAgents.size
  }

  override fun getItemViewType(position: Int): Int {
    return position
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

}

interface DaItemRecyclerAdapterInterface {
  fun onSwitchDAStatusCheckedChanged(
    position: Int,
    da: User,
    buttonView: View,
    isChecked: Boolean
  )

  fun deleteDA(position: Int, daAgent: User)

}