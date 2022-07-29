package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.CalculationLogics
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.OrderOldItems
import admin.arpan.delivery.viewModels.HomeViewModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.cart_product_item_view.view.*
import kotlinx.android.synthetic.main.cart_product_item_view.view.completedOrdersTextView
import kotlinx.android.synthetic.main.cart_product_item_view.view.ordersTotalTextView
import kotlinx.android.synthetic.main.cart_product_item_view.view.totalIncomeTextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderOldMainItemRecyclerAdapter(
    private val context: Context,
    private val productItems: ArrayList<OrderOldItems>,
    private val orderOldSubItemRecyclerAdapterInterfaceListener: OrderOldSubItemRecyclerAdapterInterface,
    private val showStats: Boolean,
    private val showDaStatsMode: Boolean,
    private val da_category: String,
    private val viewModel: HomeViewModel?
) : RecyclerView.Adapter
    <OrderOldMainItemRecyclerAdapter.RecyclerViewHolder>() {

    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var cartItemRecyclerAdapter: OrderOldSubItemRecyclerAdapter

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ordersTotalTextView = itemView.ordersTotalTextView as TextView
        val totalIncomeTextView = itemView.totalIncomeTextView as TextView
        val totalIncomeTextViewBottom = itemView.totalIncomeTextViewBottom as TextView
        val cancelledOrdersTextView = itemView.cancelledOrdersTextView as TextView
        val completedOrdersTextView = itemView.completedOrdersTextView as TextView
        val completedOrdersTextViewBottom = itemView.completedOrdersTextViewBottom as TextView
        val cardViewCancelledOrders = itemView.cardViewCancelledOrders as MaterialCardView
        val productsTextView = itemView.productsTextView as TextView
        val orderStatusContainerLinearLayout = itemView.orderStatusContainerLinearLayout as LinearLayout
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
        cartItemRecyclerAdapter = OrderOldSubItemRecyclerAdapter(context, productItems[position].orders, position, orderOldSubItemRecyclerAdapterInterfaceListener, viewModel)
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

        if(showStats){
            if(showDaStatsMode){
                holder.orderStatusContainerLinearLayout.visibility = View.VISIBLE
                holder.cardViewCancelledOrders.visibility = View.GONE
                holder.totalIncomeTextViewBottom.text = "Total Balance (Daily)"
                holder.completedOrdersTextViewBottom.text = "Due to Arpan (Daily)"
                val calculationResult = CalculationLogics().calculateArpansStatsForArpan(productItems[position].orders)
                if(da_category=="পারমানেন্ট"){
                    holder.totalIncomeTextView.text = calculationResult.agentsIncomePermanent.toString()
                    holder.ordersTotalTextView.text = calculationResult.totalOrders.toString()
                    holder.completedOrdersTextView.text = calculationResult.agentsDueToArpanPermanent.toString()
                }else{
                    holder.totalIncomeTextView.text = calculationResult.agentsIncome.toString()
                    holder.ordersTotalTextView.text = calculationResult.totalOrders.toString()
                    holder.completedOrdersTextView.text = calculationResult.agentsDueToArpan.toString()
                }
            }else{
                holder.orderStatusContainerLinearLayout.visibility = View.VISIBLE
                val calculationResult = CalculationLogics().calculateArpansStatsForArpan(productItems[position].orders)
                holder.ordersTotalTextView.text = calculationResult.totalOrders.toString()
                holder.totalIncomeTextView.text = calculationResult.arpansIncome.toString()
                holder.completedOrdersTextView.text = calculationResult.completed.toString()
                holder.cancelledOrdersTextView.text = calculationResult.cancelled.toString()
            }
        }else{
            holder.orderStatusContainerLinearLayout.visibility = View.GONE
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