package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.ui.products.ProductsActivity
import admin.arpan.delivery.ui.shops.UpdateShop
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.getGsonParser
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.shop_item_view.view.*
import com.google.firebase.dynamiclinks.ktx.component1
import com.google.firebase.dynamiclinks.ktx.component2
import com.shashank.sony.fancytoastlib.FancyToast

import android.content.ClipData

import android.content.ClipboardManager

import com.google.firebase.dynamiclinks.ShortDynamicLink


class ShopItemRecyclerAdapter(
  private val context: Context,
  private val shopItems: ArrayList<Shop>,
  private val dataLocation: String,
  private val shopRecyclerAdapterInterface: ShopRecyclerAdapterInterface
) : RecyclerView.Adapter<ShopItemRecyclerAdapter.RecyclerViewHolder>() {

  class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.shopImageItem as ImageView
    val switchShopStatus = itemView.switchShopStatus as SwitchMaterial
    val textView = itemView.titleTextView as TextView
    val cardView = itemView.mainCardView as CardView
    val editButton = itemView.editButton as ImageButton
    val shareButton = itemView.shareButton as ImageButton
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
    val view = LayoutInflater.from(context).inflate(
      R.layout.shop_item_view, parent,
      false
    )
    return RecyclerViewHolder(
      view
    )
  }

  override fun getItemCount(): Int {
    return shopItems.size
  }

  override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
    holder.textView.text = shopItems[position].name
    holder.switchShopStatus.isChecked = shopItems[position].open!!

    holder.switchShopStatus.setOnCheckedChangeListener { buttonView, isChecked ->
      shopRecyclerAdapterInterface.onSwitchShopStatusCheckedChanged(
        position,
        shopItems[position], buttonView, isChecked
      )
    }

    if (shopItems[position].icon != null) {
      if (shopItems[position].icon!!.path!!.contains(".png")) {
        Glide.with(context)
          .load(Constants.SERVER_FILES_BASE_URL + shopItems[position].icon!!.path!!)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .centerCrop()
          .override(300, 300)
          .placeholder(R.drawable.test_shop_image)
          .into(holder.imageView)
      }
    }

    holder.cardView.setOnClickListener {
      val intent = Intent(context, ProductsActivity::class.java)
      intent.putExtra("data", getGsonParser()!!.toJson(shopItems[position]))
      context.startActivity(intent)
    }

    holder.editButton.setOnClickListener {
      val intent = Intent(context, UpdateShop::class.java)
      intent.putExtra("data", getGsonParser()!!.toJson(shopItems[position]))
      context.startActivity(intent)
    }

    holder.cardView.setOnLongClickListener {
      val mDialog = AlertDialog.Builder(context)
        .setTitle("Are you sure to delete this shop?")
        .setMessage("This will delete all the products of the shop and all other stuff.....")
        .setPositiveButton(
          context.getString(R.string.yes_ok)
        ) { diaInt, _ ->
          diaInt.dismiss()
          shopRecyclerAdapterInterface.deleteShop(position, shopItems[position])
        }
        .setNegativeButton(
          context.getString(R.string.no_its_ok)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        .create()
      mDialog.show()
      true
    }

    holder.shareButton.setOnClickListener {
      if (shopItems[position].dynamicLink.toString().contains("https://arpanapp.page.link/")) {
        holder.shareButton.isEnabled = true
        val clipboard: ClipboardManager? =
          context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(shopItems[position].name, shopItems[position].dynamicLink)
        clipboard?.setPrimaryClip(clip)
        context.showToast("Copied To Clipboard", FancyToast.SUCCESS)
      } else {
        holder.shareButton.isEnabled = false
        context.showToast("Please Wait....", FancyToast.SUCCESS)
        Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
          longLink = Uri.parse(
            "https://arpanapp.page.link/?link=" +
                    "https://arpan.delivery/shops/${shopItems[position].id}&apn=arpan.delivery"
          )
        }.addOnSuccessListener { (shortLink, flowchartLink) ->
          // You'll need to import com.google.firebase.dynamiclinks.ktx.component1 and
          // com.google.firebase.dynamiclinks.ktx.component2
          // Short link created
          val clipboard: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
          val clip = ClipData.newPlainText(shopItems[position].name, shortLink.toString())
          clipboard?.setPrimaryClip(clip)

          context.showToast("Copied To Clipboard", FancyToast.SUCCESS)
          shopRecyclerAdapterInterface.createShareLink(
            position, shopItems[position].id.toString(),
            shortLink.toString(), holder.shareButton
          )
        }.addOnFailureListener {
          holder.shareButton.isEnabled = true
          // Error
          // ...
          context.showToast("Failed To Generate Link", FancyToast.ERROR)
        }
      }
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return position
  }
}

interface ShopRecyclerAdapterInterface {
  fun onSwitchShopStatusCheckedChanged(
    position: Int,
    shop: Shop,
    buttonView: View,
    isChecked: Boolean
  )

  fun deleteShop(position: Int, shop: Shop)

  fun createShareLink(position: Int, id: String, link: String, shareButton: View)
}