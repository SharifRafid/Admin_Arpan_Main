package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.ui.feedback.UserFeedBackFragment
import admin.arpan.delivery.ui.home.HomeActivityMain
import android.content.Context
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
                4-> UserFeedBackFragment().show((context as HomeActivityMain).supportFragmentManager, "")
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }


}