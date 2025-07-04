package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ShopItemRecyclerAdapter
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.db.model.ShopItem
import admin.arpan.delivery.utils.Constants
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_shops.*

class ShopsActivity : AppCompatActivity() {

    private var array_size = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shops)

        titleTextView.setOnClickListener {
            onBackPressed()
        }

        loadDataFirestore()
    }

    private fun loadDataFirestore() {
        FirebaseFirestore.getInstance()
            .collection(Constants.FC_SHOPS_MAIN)
            .orderBy(Constants.FIELD_FD_SM_CATEGORY)
            .orderBy(Constants.FIELD_FD_SM_ORDER)
            .get().addOnCompleteListener {
                if(it.isSuccessful){
                    if(it.result!!.documents.isNotEmpty()){
                        val arrayList = ArrayList<ShopItem>()
                        for(document in it.result!!.documents){
                            var shopItem = ShopItem(
                                key = document.id,
                                name = document.getString(Constants.FIELD_FD_SM_NAME).toString(),
                                categories = document.getString(Constants.FIELD_FD_SM_CATEGORY).toString(),
                                image = document.getString(Constants.FIELD_FD_SM_ICON).toString(),
                                cover_image = document.getString(Constants.FIELD_FD_SM_COVER).toString(),
                                da_charge = document.getString(Constants.FIELD_FD_SM_DA_CHARGE).toString(),
                                deliver_charge = document.getString(Constants.FIELD_FD_SM_DELIVERY).toString(),
                                location = document.getString(Constants.FIELD_FD_SM_LOCATION).toString(),
                                username = document.getString(Constants.FIELD_FD_SM_USERNAME).toString(),
                                password = document.getString(Constants.FIELD_FD_SM_PASSWORD).toString(),
                                order = document.getString(Constants.FIELD_FD_SM_ORDER).toString().toInt(),
                                status = document.getString(Constants.FIELD_FD_SM_STATUS).toString()
                            )
                            if(document.contains("shopNotice")){
                                shopItem.shopNotice = document.getString("shopNotice").toString()
                            }
                            if(document.contains("shopNoticeColor")){
                                shopItem.shopNoticeColor = document.getString("shopNoticeColor").toString()
                            }
                            if(document.contains("shopNoticeColorBg")){
                                shopItem.shopNoticeColorBg = document.getString("shopNoticeColorBg").toString()
                            }
                            if(document.contains("shopDiscount")){
                                shopItem.shopDiscount = document.getBoolean("shopDiscount")!!
                            }
                            if(document.contains("shopCategoryDiscount")){
                                shopItem.shopCategoryDiscount = document.getBoolean("shopCategoryDiscount")!!
                            }
                            if(document.contains("shopCategoryDiscountName")){
                                shopItem.shopCategoryDiscountName = document.getString("shopCategoryDiscountName")!!
                            }
                            if(document.contains("shopDiscountPercentage")){
                                shopItem.shopDiscountPercentage = document.getString("shopDiscountPercentage").toString().toFloat()
                            }
                            if(document.contains("shopDiscountMinimumPrice")){
                                shopItem.shopDiscountMinimumPrice = document.getString("shopDiscountMinimumPrice").toString().toFloat()
                            }
                            arrayList.add(shopItem)
                        }
                        array_size = arrayList.size
                        mainRecyclerView.layoutManager = LinearLayoutManager(this)
                        val adapterShops = ShopItemRecyclerAdapter(this, arrayList, "")
                        adapterShops.setHasStableIds(true)
                        mainRecyclerView.adapter = adapterShops
                    }else{
                        Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()

                    }
                }else{
                    it.exception!!.printStackTrace()
                }
            }
    }

    fun addShopClick(view : View){
        val intent = Intent(this, AddShop::class.java)
        intent.putExtra("array_size", (array_size+1).toString())
        startActivity(intent)
    }

    fun addShopCategoryClick(view: View) {
        val intent = Intent(this, ShopCategoryActivity::class.java)
        startActivity(intent)
    }
}