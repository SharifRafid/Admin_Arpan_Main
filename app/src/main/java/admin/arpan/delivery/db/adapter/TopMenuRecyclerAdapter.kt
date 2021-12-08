package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.ui.auth.MainActivity
import admin.arpan.delivery.R
import admin.arpan.delivery.ui.da.DaActivity
import admin.arpan.delivery.ui.feedback.UserFeedBackFragment
import admin.arpan.delivery.ui.home.AddOffers
import admin.arpan.delivery.ui.home.HomeActivity
import admin.arpan.delivery.ui.home.HomeActivityMain
import admin.arpan.delivery.ui.order.AddCustomOrder
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
                0->{(context as HomeActivityMain)}
                1->{(context as HomeActivityMain)}
                2->{(context as HomeActivityMain)}
                4-> UserFeedBackFragment().show((context as HomeActivity).supportFragmentManager, "")
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }


}