package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.ui.da.AddDaFragment
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.da.UpdateDaFragment
import admin.arpan.delivery.utils.AppGlide
import admin.arpan.delivery.utils.showToast
import android.content.Context
import android.icu.number.NumberRangeFormatter.with
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

class DaItemRecyclerAdapter(val context : Context,
                            val daAgents : List<DaAgent>)
    : RecyclerView.Adapter<DaItemRecyclerAdapter.RecyclerViewHolder>(){

    val firebaseDatabase = FirebaseDatabase.getInstance().reference

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title_text_view = itemView.title_text_view
        val phone_text_view = itemView.phone_text_view
        val imageView = itemView.image_view
        val da_activity = itemView.da_activity
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
        holder.phone_text_view.text = daAgent.da_mobile

        if(daAgent.da_image.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance()
                .getReference("da_storage_image_location")
                .child(daAgent.da_image)

            Glide.with(context)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image)
                .into(holder.imageView)
        }

        firebaseDatabase.child("da_agents_realtime_details")
            .child(daAgent.key)
            .child("status")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    when(snapshot.value.toString()){
                        "active" -> {
                            holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_active))
                        }
                        "inactive" -> {
                            holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_inactive))
                        }
                        "busy" -> {
                            holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_busy))
                        }
                        "gone" -> {
                            holder.da_activity.setImageDrawable(context.resources.getDrawable(R.drawable.da_gone))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }

            })

        holder.cardView.setOnClickListener {
            val fragmentDialog = UpdateDaFragment()
            (context as DaActivity).selectedDaAgent = daAgent
            (context as DaActivity).selectedAgentPostition = position
            fragmentDialog.show(context.supportFragmentManager, "")
        }

        holder.cardView.setOnLongClickListener {
            FirebaseFirestore.getInstance()
                .collection("da_agents_main_list_collection")
                .document(daAgents[position].key)
                .delete().addOnCompleteListener {
                    if(it.isSuccessful){
                        firebaseDatabase.child("da_agents_realtime_details")
                            .child(daAgent.key).removeValue()
                        (context as DaActivity).daList.removeAt(position)
                        (context as DaActivity).daItemRecyclerAdapter.notifyItemRemoved(position)
                        (context as DaActivity).daItemRecyclerAdapter.notifyItemRangeChanged(position, daAgents.size)
                        context.showToast("Success Deleted", FancyToast.SUCCESS)
                    }else{
                        it.exception!!.printStackTrace()
                        context.showToast("Failed", FancyToast.SUCCESS)
                    }
                }
            true
        }
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