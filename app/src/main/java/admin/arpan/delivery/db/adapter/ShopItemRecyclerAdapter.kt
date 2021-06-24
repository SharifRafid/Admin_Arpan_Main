package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopItem
import admin.arpan.delivery.ui.products.ProductsActivity
import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.ui.shops.UpdateShop
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.shop_item_view.view.*
import java.io.IOException

class ShopItemRecyclerAdapter(
    private val context : Context,
    private val shopItems : ArrayList<ShopItem>,
    private val dataLocation : String) : RecyclerView.Adapter<ShopItemRecyclerAdapter.RecyclerViewHolder>() {

    val firebaseFirestore = FirebaseFirestore.getInstance()

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.shopImageItem as ImageView
        val switchShopStatus = itemView.switchShopStatus as SwitchMaterial
        val textView = itemView.titleTextView as TextView
        val cardView = itemView.mainCardView as CardView
        val editButton = itemView.editButton as ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.shop_item_view, parent,
            false)
        return RecyclerViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return shopItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.textView.text = shopItems[position].name
        holder.switchShopStatus.isChecked = shopItems[position].status == "open"

        holder.switchShopStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(shopItems[position].status != "open"){
                    shopItems[position].status = "open"
                    holder.switchShopStatus.isEnabled = false
                    firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                        .document(shopItems[position].key)
                        .update(Constants.FIELD_FD_SM_STATUS,"open")
                        .addOnCompleteListener {
                            holder.switchShopStatus.isEnabled = true
                        }
                }
            }else{
                if(shopItems[position].status != "closed") {
                    shopItems[position].status = "closed"
                    holder.switchShopStatus.isEnabled = false
                    firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                        .document(shopItems[position].key)
                        .update(Constants.FIELD_FD_SM_STATUS,"closed")
                        .addOnCompleteListener {
                            holder.switchShopStatus.isEnabled = true
                        }
                }
            }
        }

        if(shopItems[position].image != ""){
            val storageReference = FirebaseStorage.getInstance()
                    .getReference(Constants.FS_SHOPS_MAIN)
                    .child(shopItems[position].key)
                    .child(shopItems[position].image)

            Glide.with(context)
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .override(300,300)
                    .placeholder(R.drawable.test_shop_image)
                    .into(holder.imageView)
        }

        holder.cardView.setOnClickListener {
            val intent = Intent(context, ProductsActivity::class.java)
            intent.putExtra("shop_key",shopItems[position].key)
            intent.putExtra("shop_name",shopItems[position].name)
            intent.putExtra("shop_location",shopItems[position].location)
            intent.putExtra("cover_image",shopItems[position].cover_image)
            intent.putExtra("deliver_charge",shopItems[position].deliver_charge)
            intent.putExtra("da_charge",shopItems[position].da_charge)
            context.startActivity(intent)
        }
        holder.editButton.setOnClickListener {
            val intent = Intent(context, UpdateShop::class.java)
            intent.putExtra("key",shopItems[position].key)
            intent.putExtra("name",shopItems[position].name)
            intent.putExtra("location",shopItems[position].location)
            intent.putExtra("cover_image",shopItems[position].cover_image)
            intent.putExtra("deliver_charge",shopItems[position].deliver_charge)
            intent.putExtra("da_charge",shopItems[position].da_charge)
            intent.putExtra("image",shopItems[position].image)
            intent.putExtra("order",shopItems[position].order.toString())
            intent.putExtra("categories",shopItems[position].categories)
            context.startActivity(intent)
        }

        holder.cardView.setOnLongClickListener {
            val progressDialog = context.createProgressDialog()
            val mDialog = AlertDialog.Builder(context)
                .setTitle("Are you sure to delete this shop?")
                .setMessage("This will delete all the products of the shop and all other stuff.....")
                .setPositiveButton(
                    context.getString(R.string.yes_ok)
                ) { diaInt, _ ->
                    progressDialog.show()
                    FirebaseFirestore.getInstance()
                        .collection(Constants.FC_SHOPS_MAIN)
                        .document(shopItems[position].key)
                        .delete().addOnCompleteListener {
                            FirebaseFirestore.getInstance()
                                .collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .document(shopItems[position].key)
                                .delete().addOnCompleteListener {
                                    FirebaseDatabase.getInstance().reference.child("product_counts_category_main")
                                        .child(shopItems[position].key).removeValue().addOnCompleteListener {
                                            deleteTheImagesFromFirebaseStorage(shopItems[position].key)
                                            shopItems.removeAt(position)
                                            notifyItemRemoved(position)
                                            notifyItemRangeChanged(position, shopItems.size)
                                            progressDialog.dismiss()
                                        }
                                }
                        }
                    diaInt.dismiss()
                }
                .setNegativeButton(
                    context.getString(R.string.no_its_ok)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
            mDialog.show()
            true
        }
    }

    private fun deleteTheImagesFromFirebaseStorage(key: String) {
        val mediaType: MediaType =
            MediaType.parse("application/json; charset=utf-8")
        val data: MutableMap<String, String> =
            java.util.HashMap()
        data["shop_key"] = key
        val json = Gson().toJson(data)
        val body: RequestBody =
            RequestBody.create(
                mediaType,
                json
            )
        val request: Request = Request.Builder()
            .url("https://arpan-fcm.herokuapp.com/delete-firebase-storage-of-a-specific-shop-all-delete-images")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                e!!.printStackTrace()
            }

            override fun onResponse(response: Response?) {
                Log.e("notifiication response" , response!!.message())
            }

        })
    }
}