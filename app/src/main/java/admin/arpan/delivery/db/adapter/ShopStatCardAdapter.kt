package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopStatItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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