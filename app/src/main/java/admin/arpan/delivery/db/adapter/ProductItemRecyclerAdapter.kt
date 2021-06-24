package admin.arpan.delivery.db.adapter

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.ui.products.*
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.product_item_view.view.*
import java.io.IOException

class ProductItemRecyclerAdapter(
    private val context : Context,
    private val activity : Activity,
    private val productItems : ArrayList<ProductItem>,
    private val shopName : String,
    private val categoryKey : String,
    private val shopKey : String
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
            false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.textView.text = productItems[position].name
        holder.price.text = "মুল্যঃ ${productItems[position].price} ৳"
        holder.statusSwitch.isChecked = productItems[position].inStock=="active"
        holder.offerStatusSwitch.isChecked = productItems[position].offerStatus=="active"
        holder.statusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(productItems[position].inStock!="active"){
                    FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
                        .document(shopKey).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(productItems[position].key)
                        .update("inStock", "active")
                    productItems[position].inStock="active"
                }
            }else{
                if(productItems[position].inStock=="active"){
                    FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
                        .document(shopKey).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(productItems[position].key)
                        .update("inStock", "inactive")
                    productItems[position].inStock="inactive"
                }

            }
        }
        holder.offerStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                if(productItems[position].offerStatus!="active"){
                    FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
                        .document(shopKey).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(productItems[position].key)
                        .update("offerStatus", "active")
                    productItems[position].offerStatus="active"
                }
            }else{
                if(productItems[position].offerStatus=="active"){
                    FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
                        .document(shopKey).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(productItems[position].key)
                        .update("offerStatus", "inactive")
                    productItems[position].offerStatus="inactive"
                }

            }
        }
        if(productItems[position].image1.isNotEmpty()){
            val storageReference = FirebaseStorage.getInstance().getReference("shops")
                .child(shopKey)
                .child(productItems[position].image1)

            Glide.with(context)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .override(300,300)
                .placeholder(R.drawable.test_shop_image).into(holder.imageView)
        }
        holder.cardView.setOnClickListener {
            (context as ProductsActivity).currentSelectedProductMainIndex = position
//            val addNewProductIntent = Intent(context, UpdateProductActivity::class.java)
//            addNewProductIntent.putExtra("key",productItems[position].key)
//            addNewProductIntent.putExtra("name",productItems[position].name)
//            addNewProductIntent.putExtra("shopKey",productItems[position].shopKey)
//            addNewProductIntent.putExtra("shopCategoryId",productItems[position].shopCategoryId)
//            addNewProductIntent.putExtra("shopCategoryKey",productItems[position].shopCategoryKey)
//            addNewProductIntent.putExtra("price",productItems[position].price)
//            addNewProductIntent.putExtra("image1",productItems[position].image1)
//            addNewProductIntent.putExtra("image2",productItems[position].image2)
//            addNewProductIntent.putExtra("image3",productItems[position].image3)
//            addNewProductIntent.putExtra("offerPrice",productItems[position].offerPrice)
//            addNewProductIntent.putExtra("offerStatus",productItems[position].offerStatus)
//            addNewProductIntent.putExtra("inStock",productItems[position].inStock)
//            addNewProductIntent.putExtra("description",productItems[position].description)
//            addNewProductIntent.putExtra("shortDescription",productItems[position].shortDescription)
//            addNewProductIntent.putExtra("offerDetails",productItems[position].offerDetails)
//            addNewProductIntent.putExtra("productDetails",productItems[position].productDetails)
//            addNewProductIntent.putExtra("order",productItems[position].order)
//            context.startActivity(addNewProductIntent)
//
            val bundle = Bundle()
            bundle.putString("key",productItems[position].key)
            bundle.putString("name",productItems[position].name)
            bundle.putString("shopKey",productItems[position].shopKey)
            bundle.putString("shopCategoryId",productItems[position].shopCategoryId)
            bundle.putString("shopCategoryKey",productItems[position].shopCategoryKey)
            bundle.putString("price",productItems[position].price)
            bundle.putString("image1",productItems[position].image1)
            bundle.putString("image2",productItems[position].image2)
            bundle.putString("image3",productItems[position].image3)
            bundle.putString("offerPrice",productItems[position].offerPrice)
            bundle.putString("offerStatus",productItems[position].offerStatus)
            bundle.putString("inStock",productItems[position].inStock)
            bundle.putString("description",productItems[position].description)
            bundle.putString("shortDescription",productItems[position].shortDescription)
            bundle.putString("offerDetails",productItems[position].offerDetails)
            bundle.putString("productDetails",productItems[position].productDetails)
            bundle.putString("order",productItems[position].order)
            val updateProductFragment = UpdateProductFragment()
            updateProductFragment.arguments = bundle
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
                    progressDialog.show()
                    FirebaseFirestore.getInstance().collection(Constants.FC_SHOPS_MAIN)
                        .document(shopKey)
                        .collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
                        .document(productItems[position].key)
                        .delete().addOnCompleteListener {
                            if(productItems[position].image1.isEmpty()){
                                productItems.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, productItems.size)
                                context.showToast("Success Delete", FancyToast.SUCCESS)
                                progressDialog.dismiss()
                            }else{
                                FirebaseStorage.getInstance().getReference("shops")
                                    .child(shopKey)
                                    .child(productItems[position].image1)
                                    .delete().addOnCompleteListener {
                                        productItems.removeAt(position)
                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, productItems.size)
                                        context.showToast("Success Delete", FancyToast.SUCCESS)
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


}