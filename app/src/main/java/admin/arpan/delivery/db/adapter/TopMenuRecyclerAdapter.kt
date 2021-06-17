package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.ui.auth.MainActivity
import admin.arpan.delivery.R
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.home.AddOffers
import admin.arpan.delivery.ui.order.OrdresActivity
import admin.arpan.delivery.ui.settings.SettingActivity
import admin.arpan.delivery.ui.shops.ShopsActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_top_item_view.view.*

class TopMenuRecyclerAdapter(val context : Context,
                      val images : List<Int>,
                      val titles : List<String>)
    : RecyclerView.Adapter<TopMenuRecyclerAdapter.RecyclerViewHolder>(){

    class RecyclerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val textView = itemView.title_text_view
        val imageView = itemView.image_view
        val cardView = itemView.cardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.custom_top_item_view,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
        holder.textView.text = titles[position]
        holder.cardView.setOnClickListener {
            when(position){
                0->{context.startActivity(Intent(context, ShopsActivity::class.java)) }
                1->{context.startActivity(Intent(context, AddOffers::class.java))}
                2->{context.startActivity(Intent(context, OrdresActivity::class.java))}
                3->{context.startActivity(Intent(context, DaActivity::class.java))}
                4->{context.startActivity(Intent(context, SettingActivity::class.java))}
                5->{context.startActivity(Intent(context, SettingActivity::class.java))}
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }


}