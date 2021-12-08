package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.da.UpdateDaFragment
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
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
    val daAgents: List<DaAgent>,
    val ordersAll: ArrayList<OrderItemMain>?,
    val homeMainNewInterface: HomeMainNewInterface
)
    : RecyclerView.Adapter<DaItemRecyclerAdapter.RecyclerViewHolder>(){

    val firebaseDatabase = FirebaseDatabase.getInstance().reference

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
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
        val daAgent = daAgents[position]
        holder.title_text_view.text = daAgent.da_name
        holder.daIdTextView.text = "ID#"+daAgent.da_uid
        holder.phone_text_view.text = daAgent.da_mobile
        holder.switchDaActivity.isChecked = daAgent.da_status_active
        holder.switchDaActivity.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(!daAgents[position].da_status_active){
                    daAgents[position].da_status_active = true
                    holder.switchDaActivity.isEnabled = false
                    FirebaseFirestore.getInstance()
                        .collection("da_agents_main_list_collection")
                        .document(daAgents[position].key)
                        .update("da_status_active",true)
                        .addOnCompleteListener {
                            holder.switchDaActivity.isEnabled = true
                        }
                }
            }else{
                if(daAgents[position].da_status_active) {
                    daAgents[position].da_status_active = false
                    holder.switchDaActivity.isEnabled = false
                    val hashMap = HashMap<String,Any>()
                    hashMap[daAgents[position].key] = daAgents[position]
                    FirebaseFirestore.getInstance()
                        .collection("da_agents_main_list_collection")
                        .document(daAgents[position].key)
                        .update("da_status_active",false)
                        .addOnCompleteListener {
                            holder.switchDaActivity.isEnabled = true
                            FirebaseDatabase.getInstance().reference
                                .child("da_agents_realtime_details")
                                .child(daAgents[position].key)
                                .child("status")
                                .setValue("Inactive")
                        }
                }
            }
        }

        if(daAgent.da_image.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance()
                .getReference("da_storage_image_location")
                .child(daAgent.da_image)

            Glide.with(context)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.test_shop_image)
                .into(holder.imageView)
        }

        firebaseDatabase.child("da_agents_realtime_details")
            .child(daAgent.key)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child("status").value.toString() == "Active"){
                        holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_active))
                    }else{
                        holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_busy))
                    }
                    if(snapshot.child("statusTextTitle").value != null && snapshot.child("statusTextTitle").value.toString().isNotEmpty()){
                        holder.statusTextView.visibility = View.VISIBLE
                        holder.statusTextView.text = snapshot.child("statusTextTitle").value.toString()
                    }else{
                        holder.statusTextView.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })

        holder.cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("key",  daAgent.key.toString())
            bundle.putString("da_uid",  daAgent.da_uid.toString())
            bundle.putString("da_name",  daAgent.da_name.toString())
            bundle.putString("da_mobile",  daAgent.da_mobile.toString())
            bundle.putString("da_bkash",  daAgent.da_bkash.toString())
            bundle.putString("da_password",  daAgent.da_password.toString())
            bundle.putString("da_blood_group",  daAgent.da_blood_group.toString())
            bundle.putString("da_category",  daAgent.da_category.toString())
            bundle.putString("da_image",  daAgent.da_image.toString())
            bundle.putString("da_address",  daAgent.da_address.toString())
            bundle.putString("da_status_active",  daAgent.da_status_active.toString())
            homeMainNewInterface.navigateToFragment(R.id.daStatsFragment, bundle)
        }

        holder.cardView.setOnLongClickListener {
            val dialogMain = AlertDialog.Builder(context)
                .setPositiveButton("Edit", DialogInterface.OnClickListener { dialog_main, which ->
                    val bundle = Bundle()
                    bundle.putString("key",  daAgent.key.toString())
                    bundle.putString("da_uid",  daAgent.da_uid.toString())
                    bundle.putString("da_name",  daAgent.da_name.toString())
                    bundle.putString("da_mobile",  daAgent.da_mobile.toString())
                    bundle.putString("da_bkash",  daAgent.da_bkash.toString())
                    bundle.putString("da_password",  daAgent.da_password.toString())
                    bundle.putString("da_blood_group",  daAgent.da_blood_group.toString())
                    bundle.putString("da_category",  daAgent.da_category.toString())
                    bundle.putString("da_image",  daAgent.da_image.toString())
                    bundle.putString("da_address",  daAgent.da_address.toString())
                    bundle.putString("da_status_active",  daAgent.da_status_active.toString())
                    homeMainNewInterface.navigateToFragment(R.id.updateDaFragment, bundle)
                })
                .setNegativeButton("Delete", DialogInterface.OnClickListener { dialog_main, which ->
                    val mDialog = AlertDialog.Builder(context)
                        .setTitle("Are you sure to delete this agent?")
                        .setPositiveButton(
                            context.getString(R.string.yes_ok)
                        ) { diaInt, _ ->
                            FirebaseFirestore.getInstance()
                                .collection("da_agents_main_list_collection")
                                .document(daAgents[position].key)
                                .delete().addOnCompleteListener {
                                    if(it.isSuccessful){
                                        firebaseDatabase.child("da_agents_realtime_details")
                                            .child(daAgent.key).removeValue()
//                                (context as DaActivity).daList.removeAt(position)
//                                (context as DaActivity).daItemRecyclerAdapter.notifyItemRemoved(position)
//                                (context as DaActivity).daItemRecyclerAdapter.notifyItemRangeChanged(position, daAgents.size)
                                        context.showToast("Success Deleted", FancyToast.SUCCESS)
                                    }else{
                                        it.exception!!.printStackTrace()
                                        context.showToast("Failed", FancyToast.SUCCESS)
                                    }
                                }
                            diaInt.dismiss()
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

        val calculationResult = ordersAll?.let { CalculationLogics()
            .calculateArpansStatsForArpan(it.filter { item ->  item.daID == daAgent.key} as ArrayList<OrderItemMain>) }
        if(daAgent.da_category=="পারমানেন্ট"){
            holder.myIncomeTextView.text = calculationResult!!.agentsIncomePermanent.toString()
            holder.arpanBokeyaTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
//            holder.arpanBokeyaTextView.text = (calculationResult!!.totalOrders-calculationResult.completed).toString()
//            holder.myIncomeTextView.text = calculationResult.completed.toString()
        }else{
            holder.myIncomeTextView.text = calculationResult!!.agentsIncome.toString()
            holder.arpanBokeyaTextView.text = calculationResult.agentsDueToArpan.toString()
//            holder.arpanBokeyaTextView.text = (calculationResult!!.totalOrders-calculationResult.completed).toString()
//            holder.myIncomeTextView.text = calculationResult.completed.toString()
        }
        holder.totalOrderThisMonthTextView.text = calculationResult.totalOrders.toString()
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