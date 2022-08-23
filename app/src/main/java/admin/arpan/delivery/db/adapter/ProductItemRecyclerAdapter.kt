package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.models.Product
import admin.arpan.delivery.ui.products.*
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.android.synthetic.main.product_item_view.view.*

class ProductItemRecyclerAdapter(
  private val context: Context,
  private val activity: Activity,
  private val productItems: ArrayList<Product>,
  private val shopName: String,
  private val categoryKey: String,
  private val shopKey: String,
  private val productRecyclerAdapterInterface: ProductRecyclerAdapterInterface
) : RecyclerView.Adapter
<ProductItemRecyclerAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.shopImageItem as ImageView
    val textView = itemView.titleTextView as TextView
    val price = itemView.priceTextView as TextView
    val statusSwitch = itemView.status_switch as SwitchMaterial
    val offerStatusSwitch = itemView.offer_status_switch as SwitchMaterial
    val cardView = itemView.mainCardView as CardView
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    val view = LayoutInflater.from(context).inflate(
      R.layout.product_item_view, parent,
      false
    )
    return RecyclerViewHolder(view)
  }

  override fun getItemCount(): Int {
    return productItems.size
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    holder.textView.text = productItems[position].name
    holder.price.text = "মুল্যঃ ${productItems[position].price} ৳"
    holder.statusSwitch.isChecked = productItems[position].inStock!!
    holder.offerStatusSwitch.isChecked = productItems[position].offerActive!!
    holder.statusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
      productRecyclerAdapterInterface.onSwitchProductStatusCheckedChanged(
        position,
        productItems[position], buttonView, isChecked
      )
    }
    holder.offerStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
      productRecyclerAdapterInterface.onSwitchProductStatusCheckedChanged(
        position,
        productItems[position], buttonView, isChecked
      )
    }
    if (productItems[position].icon != null) {
      Glide.with(context)
        .load(Constants.SERVER_FILES_BASE_URL + productItems[position].icon!!.path)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .override(300, 300)
        .placeholder(R.drawable.test_shop_image).into(holder.imageView)
    }
    holder.cardView.setOnClickListener {
      (context as ProductsActivity).currentSelectedProductMainIndex = position
      val updateProductFragment = UpdateProductFragment(productItems[position])
      updateProductFragment.show(context.supportFragmentManager, "")
    }

    holder.cardView.setOnLongClickListener {
      val progressDialog = context.createProgressDialog()
      val mDialog = AlertDialog.Builder(context)
        .setTitle("Are you sure to delete this product?")
        .setMessage("This will delete this product of the shop and all other stuff.....")
        .setPositiveButton(
          context.getString(R.string.yes_ok)
        ) { diaInt, _ ->
          diaInt.dismiss()
          productRecyclerAdapterInterface.deleteProduct(position, productItems[position])
        }
        .setNegativeButton(
          context.getString(R.string.no_its_ok)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        .create()
      mDialog.show()
      true
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return position
  }
}

interface ProductRecyclerAdapterInterface {

  fun onSwitchProductStatusCheckedChanged(
    position: Int,
    product: Product,
    buttonView: View,
    isChecked: Boolean
  )

  fun deleteProduct(position: Int, product: Product)
}