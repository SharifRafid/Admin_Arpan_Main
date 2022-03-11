package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.db.model.ShopStatItem
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
import android.widget.ImageView
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

class ShopStatCardAdapter(val context : Context, val shopStatItems : ArrayList<ShopStatItem>)
    : RecyclerView.Adapter<ShopStatCardAdapter.RecyclerViewHolder>(){

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val titleTextView = itemView.findViewById<TextView>(R.id.title_text_view)
        val dataTextView = itemView.findViewById<TextView>(R.id.data_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.shop_stat_recycler_item,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.titleTextView.text = shopStatItems[position].title
        holder.dataTextView.text = shopStatItems[position].data
    }

    override fun getItemCount(): Int {
        return shopStatItems.size
    }


}