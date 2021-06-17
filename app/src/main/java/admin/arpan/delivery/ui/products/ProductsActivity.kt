package admin.arpan.delivery.ui.products

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ProductItemRecyclerAdapter
import admin.arpan.delivery.db.model.ProductCategoryItem
import admin.arpan.delivery.db.model.ProductItem
import admin.arpan.delivery.utils.Constants
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.shashank.sony.fancytoastlib.FancyToast
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.category_item_file.view.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProductsActivity : AppCompatActivity() {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private var shop_key = ""
    private var shop_category_key = ""
    private var shop_category_tag_name = ""
    private lateinit var progressDialog: Dialog
    private val categoryItemsArray = ArrayList<ProductCategoryItem>()
    private val keysList = ArrayList<String>()
    private val namesList = ArrayList<String>()
    private lateinit var categories_item_array_adapter : ArrayAdapter<String>
    private val productsMainArrayList = ArrayList<ProductItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        initVars()
        initLogic()
    }


    private fun initVars() {
        progressDialog = createProgressDialog()
    }

    private fun initLogic() {
        shop_key = intent.getStringExtra("shop_key").toString()
        loadFirestoreShopCategories()
        categories_item_array_adapter = ArrayAdapter<String>(
                this@ProductsActivity,
                R.layout.category_item_file,
                R.id.titleTextView,
                namesList)
    }

    private fun loadFirestoreShopCategories() {
        progressDialog.show()
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                .document(shop_key)
                .get().addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        if(task.result!!.data!=null){
                            namesList.clear()
                            keysList.clear()
                            categoryItemsArray.clear()
                            val map = task.result!!.data as Map<String, Map<String,String>>
                            for(category_field in map.entries){
                                categoryItemsArray.add(
                                        ProductCategoryItem(
                                                key = category_field.key,
                                                name = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_NAME].toString(),
                                                category_key = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_KEY].toString(),
                                                order = category_field.value[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_ORDER].toString().toInt(),
                                        )
                                )
                            }
                            Collections.sort(categoryItemsArray, kotlin.Comparator { o1, o2 ->
                                (o1.order).compareTo(o2.order) })
                            for(item in categoryItemsArray){
                                keysList.add(item.key)
                                namesList.add(item.name)
                            }
                            mainRecyclerView.adapter = categories_item_array_adapter
                            categories_item_array_adapter.notifyDataSetChanged()
                            mainRecyclerView.setOnItemClickListener { parent, view, position, id ->
                                view.isSelected = true
                                view.titleTextView.isSelected = true
                                shop_category_key = keysList[position]
                                shop_category_tag_name = categoryItemsArray[position].category_key
                                loadProductsFromCategory()
                            }
                            mainRecyclerView.setOnItemLongClickListener { parent, view, position, id ->
                                val mDialog = AlertDialog.Builder(this)
                                    .setTitle("Are you sure to delete this category?")
                                    .setMessage("This will delete all the products of the category and all other stuff.....")
                                    .setPositiveButton(
                                        getString(R.string.yes_ok)
                                    ) { diaInt, _ ->
                                        progressDialog.show()
                                        val hashMap = HashMap<String,Any>()
                                        hashMap[keysList[position]] = FieldValue.delete()
                                        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                            .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                            .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                            .document(shop_key)
                                            .update(hashMap).addOnCompleteListener {
                                                if(it.isSuccessful){
                                                    removeProductsDataFromFirestoreDatabaseViaApi(keysList[position])
                                                    keysList.removeAt(position)
                                                    namesList.removeAt(position)
                                                    categories_item_array_adapter.notifyDataSetChanged()
                                                    showToast("SUCCESS", FancyToast.SUCCESS)
                                                }else{
                                                    showToast("FAILURE", FancyToast.ERROR)
                                                }
                                                progressDialog.dismiss()
                                            }

                                    }
                                    .setNegativeButton(
                                        getString(R.string.no_its_ok)
                                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                                    .create()
                                mDialog.show()
                                true
                            }
                            mainRecyclerView.choiceMode = ListView.CHOICE_MODE_SINGLE
                            if(keysList.isNotEmpty()){
                                mainRecyclerView.setItemChecked(0, true)
                                shop_category_key = keysList[0]
                                shop_category_tag_name = categoryItemsArray[0].category_key
                                loadProductsFromCategory()
                            }
                        }else{
                            Log.e("FirestoreCategoryData","NULL")
                        }
                    }else{
                        task.exception!!.printStackTrace()
                    }
                progressDialog.dismiss()
            }
    }

    private fun removeProductsDataFromFirestoreDatabaseViaApi(s: String) {
        val mediaType: MediaType =
            MediaType.parse("application/json; charset=utf-8")
        val data: MutableMap<String, String> =
            java.util.HashMap()
        data["category_key"] = s
        data["shop_key"] = shop_key
        val json = Gson().toJson(data)
        val body: RequestBody =
            RequestBody.create(
                mediaType,
                json
            )
        val request: Request = Request.Builder()
            .url("https://arpan-fcm.herokuapp.com/delete-firestore-products-data-from-a-specific-category-from-the-admin-app")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                e!!.printStackTrace()
            }

            override fun onResponse(response: Response?) {
                Log.e("notifiication response", response!!.message())
            }
        })
    }

    fun addNewCategory(view : View){
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_category,null)
        dialogView.addProductCategoriesButton.setOnClickListener {
            val text = dialogView.edt_shop_name.text.toString()
            if(text.isNotEmpty()){
                dialogView.addProductCategoriesButton.isEnabled = false
                val category_count = (categoryItemsArray.size+1).toString()
                val key = "ctk"+System.currentTimeMillis()
                val mapMain = HashMap<String,String>()
                mapMain[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_NAME] = text
                mapMain[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_KEY] = text
                mapMain[Constants.FIELD_FD_PRODUCTS_MAIN_CATEGORY_ORDER] = category_count
                val subMap = HashMap<String,HashMap<String,String>>()
                subMap[key] = mapMain
                firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                        .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                        .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                        .document(shop_key).get().addOnSuccessListener {
                        if(it.exists()){
                            firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .document(shop_key)
                                .update(subMap as Map<String, Any>)
                                .addOnSuccessListener {
                                    FirebaseDatabase.getInstance().reference
                                        .child(Constants.COUNT_PRODUCT_CATEGORY_MAIN)
                                        .child(shop_key)
                                        .setValue(category_count)
                                        .addOnSuccessListener {
                                            updateCategoriesListWithNewCategory(key, text ,text ,category_count)
                                            dialog.dismiss()
                                            Toast.makeText(this, "Successfully Added New Category", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }else{
                            firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                .document(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .collection(Constants.FD_PRODUCTS_MAIN_CATEGORY)
                                .document(shop_key)
                                .set(subMap)
                                .addOnSuccessListener {
                                    FirebaseDatabase.getInstance().reference
                                        .child(Constants.COUNT_PRODUCT_CATEGORY_MAIN)
                                        .child(shop_key)
                                        .setValue(category_count)
                                        .addOnSuccessListener {
                                            updateCategoriesListWithNewCategory(key, text ,text ,category_count)
                                            dialog.dismiss()
                                            Toast.makeText(this, "Successfully Added New Category", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                    }
            }
        }
        dialog.setContentView(dialogView)
        dialog.show()
    }

    fun addNewProduct(view : View){
        val addNewProductIntent = Intent(this, AddProduct::class.java)
        addNewProductIntent.putExtra("shop_key",shop_key)
        addNewProductIntent.putExtra("product_category_key",shop_category_key)
        addNewProductIntent.putExtra("product_category",shop_category_tag_name)
        addNewProductIntent.putExtra("product_order",(productsMainArrayList.size+1).toString())
        startActivity(addNewProductIntent)
    }

    private fun updateCategoriesListWithNewCategory(key : String, text: String, text1: String, categoryCount: String) {
        categoryItemsArray.add(ProductCategoryItem(key,text,text1,categoryCount.toInt()))
        namesList.add(text)
        keysList.add(text1)
        categories_item_array_adapter.notifyDataSetChanged()
    }

    private fun loadProductsFromCategory() {
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN)
                .document(shop_key).collection(Constants.FD_PRODUCTS_MAIN_SUB_COLLECTION)
            .whereEqualTo("shopCategoryKey", shop_category_tag_name)
                .get().addOnCompleteListener {
                    if(it.isSuccessful){
                        productsMainArrayList.clear()
                        for(document in it.result!!.documents){
                            val obj = document.toObject(ProductItem::class.java)!!
                            obj.key = document.id
                            productsMainArrayList.add(obj)
                        }
                        val adapter = ProductItemRecyclerAdapter(this@ProductsActivity,
                                this@ProductsActivity,
                                productsMainArrayList,shop_key,shop_category_key,shop_key)
                        productsRecyclerView.adapter = adapter
                        val linearLayoutManager = LinearLayoutManager(this@ProductsActivity)
                        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                        productsRecyclerView.layoutManager = linearLayoutManager
                        adapter.notifyDataSetChanged()
                    }else{
                        it.exception!!.printStackTrace()
                    }
                }

    }

}