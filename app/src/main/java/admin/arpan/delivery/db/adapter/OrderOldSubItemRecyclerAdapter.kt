package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.ui.order.OrderHistoryFragment
import admin.arpan.delivery.ui.order.OrdresActivity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_order_history.view.*
import kotlinx.android.synthetic.main.old_orders_list_view.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderOldSubItemRecyclerAdapter(
    private val context: Context,
    private val productItems: ArrayList<OrderItemMain>,
    private val mainItemPositions: Int
) : RecyclerView.Adapter
    <OrderOldSubItemRecyclerAdapter.RecyclerViewHolder>() {

    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var cartItemRecyclerAdapter: OrderItemRecyclerAdapter

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView = itemView.textView as TextView
        val timeTextView = itemView.time as TextView
        val statusTextView = itemView.status as TextView
        val cardView = itemView.cardView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.old_orders_list_view, parent,
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.orderIdTextView.text = productItems[position].orderId
        holder.timeTextView.text = getDate(productItems[position].orderPlacingTimeStamp,"hh:mm a")
        if(productItems[position].orderCompletedStatus == "CANCELLED") {
            holder.statusTextView.text = "CANCELLED"
            holder.statusTextView.setBackgroundColor(Color.parseColor("#EA594D"))
        }else{
            holder.statusTextView.text = productItems[position].orderStatus
            holder.statusTextView.setBackgroundColor(Color.parseColor("#43A047"))
        }
        holder.cardView.setOnClickListener {
            (context as OrdresActivity).selectedRecyclerAdapterItem = position
            (context as OrdresActivity).mainItemPositionsRecyclerAdapter = mainItemPositions
            val bundle = Bundle()
            bundle.putString("orderID",productItems[position].docID)
            bundle.putString("customerId",productItems[position].userId)
            val fg = OrderHistoryFragment()
            fg.arguments = bundle
            fg.show((context as OrdresActivity).supportFragmentManager, "")
        }
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