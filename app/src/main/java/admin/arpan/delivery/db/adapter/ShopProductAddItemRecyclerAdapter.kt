package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import admin.arpan.delivery.db.model.MainShopCartItem
import admin.arpan.delivery.db.model.ProductCategoryItem
import admin.arpan.delivery.utils.Constants
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog_list_common.view.*
import kotlinx.android.synthetic.main.shop_product_add_item_view.view.*
import java.util.*
import kotlin.collections.ArrayList

class ShopProductAddItemRecyclerAdapter(
        private val context : Context,
        private val mainShopHashMap : ArrayList<MainShopCartItem>,
        private val shopProductAddOrderInterface: ShopProductAddOrderInterface
) : RecyclerView.Adapter
    <ShopProductAddItemRecyclerAdapter.RecyclerViewHolder>(), ShopProductAddOrderRecyclerBaseInterface {

    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var cartItemRecyclerAdapter: ShopProductEditItemRecyclerAdapter

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productsTextView = itemView.productsTextView as TextView
        val priceTextView = itemView.priceTextView as TextView
        val productAddImageView = itemView.productAddImageView as ImageView
        val productsRecyclerView = itemView.productsRecyclerView as RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.shop_product_add_item_view, parent,
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return mainShopHashMap.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.priceTextView.visibility = View.VISIBLE
        cartItemRecyclerAdapter = ShopProductEditItemRecyclerAdapter(context,
            mainShopHashMap[position].cart_products, shopProductAddOrderInterface,this, position)
        holder.productsTextView.text = mainShopHashMap[position].shop_details.name
        holder.productsTextView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Are you sure to delete this shop ?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    mainShopHashMap.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mainShopHashMap.size)
                    shopProductAddOrderInterface.updateTotalProductsPricing()
                })
                .create().show()
        }
        holder.productsRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.productsRecyclerView.adapter = cartItemRecyclerAdapter
        var chargeTotal = 0
        for(it in mainShopHashMap[position].cart_products){
                    chargeTotal += (it.product_item_price * it.product_item_amount)
                }
        holder.priceTextView.text = context.getString(R.string.total)+" "+chargeTotal+"৳"
        holder.productAddImageView.setOnClickListener {
            shopProductAddOrderInterface.addProductToShop(position, mainShopHashMap[position].shop_doc_id)
        }
    }

    override fun updateItemTotalPriceOnRecycle(rootAdapterPosition: Int) {
        notifyItemChanged(rootAdapterPosition)
    }
}

interface ShopProductAddOrderInterface {
    fun addProductToShop(position: Int, shopDocId: String)
    fun removeProductItem(rootAdapterPosition: Int, productItemPosition: Int)
    fun updateTotalProductsPricing()
}

interface ShopProductAddOrderRecyclerBaseInterface{
    fun updateItemTotalPriceOnRecycle(rootAdapterPosition: Int)
}