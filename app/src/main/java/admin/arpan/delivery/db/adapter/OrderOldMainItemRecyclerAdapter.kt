package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.OrderOldItems
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.cart_product_item_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderOldMainItemRecyclerAdapter(
        private val context : Context,
        private val productItems : ArrayList<OrderOldItems>
) : RecyclerView.Adapter
    <OrderOldMainItemRecyclerAdapter.RecyclerViewHolder>() {

    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var cartItemRecyclerAdapter: OrderOldSubItemRecyclerAdapter

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productsTextView = itemView.productsTextView as TextView
        val productsRecyclerView = itemView.productsRecyclerView as RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.cart_product_item_view, parent,
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        cartItemRecyclerAdapter = OrderOldSubItemRecyclerAdapter(context, productItems[position].orders, position)
        when (productItems[position].date) {
            getDate(System.currentTimeMillis(), "dd-MM-yyyy") -> {
                holder.productsTextView.text = "Today"
            }
            getDate(System.currentTimeMillis()-(24*3600*1000),
                    "dd-MM-yyyy") -> {
                holder.productsTextView.text = "Yesterday"
            }
            else -> {
                holder.productsTextView.text = productItems[position].date
            }
        }
        holder.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.productsRecyclerView.adapter = cartItemRecyclerAdapter
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.getTime())
    }
}