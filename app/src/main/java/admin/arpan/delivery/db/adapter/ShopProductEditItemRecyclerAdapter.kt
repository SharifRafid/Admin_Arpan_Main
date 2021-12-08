package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.CartProductEntity
import admin.arpan.delivery.utils.getNumValue
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.shop_product_edit_item_view.view.*

class ShopProductEditItemRecyclerAdapter(
        private val context: Context,
        private val productItems: ArrayList<CartProductEntity>,
        private val shopProductAddOrderInterface: ShopProductAddOrderInterface,
        private val shopProductAddOrderRecyclerBaseInterface: ShopProductAddOrderRecyclerBaseInterface,
        private val rootAdapterPosition : Int
) : RecyclerView.Adapter
    <ShopProductEditItemRecyclerAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productTitle = itemView.productTitle as EditText
        val unitEditText = itemView.unitEditText as EditText
        val priceEditText = itemView.priceEditText as EditText
        val arpanEditText = itemView.arpanEditText as EditText
        val deleteProductItem = itemView.deleteProductItem as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.shop_product_edit_item_view, parent,
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val cartProductEntity = productItems[position]
        holder.productTitle.setText(cartProductEntity.product_item_name)
        holder.unitEditText.setText(cartProductEntity.product_item_amount.toString())
        holder.priceEditText.setText((cartProductEntity.product_item_price-cartProductEntity.product_arpan_profit).toString())
        holder.arpanEditText.setText(cartProductEntity.product_arpan_profit.toString())
        holder.productTitle.doOnTextChanged { text, start, before, count ->
            productItems[position].product_item_name = text.toString()
        }
        holder.unitEditText.doOnTextChanged { text, start, before, count ->
            productItems[position].product_item_amount = holder.unitEditText.getNumValue()
            shopProductAddOrderRecyclerBaseInterface.updateItemTotalPriceOnRecycle(rootAdapterPosition)
            shopProductAddOrderInterface.updateTotalProductsPricing()
        }
        holder.priceEditText.doOnTextChanged { text, start, before, count ->
            productItems[position].product_arpan_profit = holder.arpanEditText.getNumValue()
            productItems[position].product_item_price = holder.priceEditText.getNumValue() + holder.arpanEditText.getNumValue()
            shopProductAddOrderRecyclerBaseInterface.updateItemTotalPriceOnRecycle(rootAdapterPosition)
            shopProductAddOrderInterface.updateTotalProductsPricing()
        }
        holder.arpanEditText.doOnTextChanged { text, start, before, count ->
            productItems[position].product_arpan_profit = holder.arpanEditText.getNumValue()
            productItems[position].product_item_price = holder.priceEditText.getNumValue() + holder.arpanEditText.getNumValue()
            shopProductAddOrderRecyclerBaseInterface.updateItemTotalPriceOnRecycle(rootAdapterPosition)
            shopProductAddOrderInterface.updateTotalProductsPricing()
        }
        holder.deleteProductItem.setOnClickListener {
            shopProductAddOrderInterface.removeProductItem(rootAdapterPosition, position)
            shopProductAddOrderInterface.updateTotalProductsPricing()
        }
    }
}