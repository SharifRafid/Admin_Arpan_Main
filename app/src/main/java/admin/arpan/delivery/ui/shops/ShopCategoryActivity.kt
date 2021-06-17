package admin.arpan.delivery.ui.shops

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.ShopCategoryItem
import admin.arpan.delivery.utils.Constants
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_shop_category.*
import kotlinx.android.synthetic.main.dialog_add_shop_category.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ShopCategoryActivity : AppCompatActivity() {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    val category_keys = ArrayList<String>()
    val category_names = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_category)

        initVars()
        initLogic()
    }

    private fun initLogic() {
        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                .document(Constants.FD_SHOPS_MAIN_CATEGORY)
                .get().addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        category_keys.clear()
                        category_names.clear()
                        val categoryItemsArray = ArrayList<ShopCategoryItem>()
                        val map = task.result!!.data as Map<String, Map<String, String>>
                        for(category_field in map.entries){
                            categoryItemsArray.add(
                                    ShopCategoryItem(
                                            key = category_field.key,
                                            name = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME].toString(),
                                            category_key = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY].toString(),
                                            order = category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_ORDER].toString()
                                                    .toInt(),
                                    )
                            )
                            category_names.add(category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME].toString())
                            category_keys.add(category_field.value[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY].toString())
                        }
                        val adapter = ArrayAdapter(
                                this@ShopCategoryActivity,
                                R.layout.custom_spinner_view,
                                category_names
                        )
                        adapter.setDropDownViewResource(R.layout.custom_spinner_item_view)
                        listView.adapter = adapter
                        listView.setOnItemClickListener { parent, view, position, id ->
                            val dialog = AlertDialog.Builder(this).create()
                            val dialogView = LayoutInflater.from(this)
                                    .inflate(R.layout.dialog_add_shop_category, null)
                            dialogView.edt_shop_name.setText(categoryItemsArray[position].name)
                            dialogView.edt_shop_key.setText(categoryItemsArray[position].category_key)
                            dialogView.edt_shop_order.setText(categoryItemsArray[position].order.toString())
                            dialogView.addProductCategoriesButton.text = "Save Category"
                            dialogView.addProductCategoriesButton.setOnClickListener {
                                dialog.setCancelable(false)
                                dialog.setCanceledOnTouchOutside(false)
                                dialogView.addProductCategoriesButton.isEnabled = false
                                dialogView.addProductCategoriesButton.text = "Saving..."
                                val hashMap = HashMap<String, String>()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME] = dialogView.edt_shop_name.text.toString()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY] = dialogView.edt_shop_key.text.toString()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_ORDER] = dialogView.edt_shop_order.text.toString()
                                val hm2 = HashMap<String, HashMap<String, String>>()
                                hm2[categoryItemsArray[position].key] = hashMap
                                firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                        .document(Constants.FD_SHOPS_MAIN_CATEGORY)
                                        .update(hm2 as Map<String, Any>).addOnCompleteListener {
                                            categoryItemsArray[position] = ShopCategoryItem(
                                                    key = categoryItemsArray[position].key,
                                                    name = dialogView.edt_shop_name.text.toString(),
                                                    category_key = dialogView.edt_shop_key.text.toString(),
                                                    order = dialogView.edt_shop_order.text.toString().toInt(),
                                            )
                                            category_names[position] = dialogView.edt_shop_name.text.toString()
                                            category_keys[position] = dialogView.edt_shop_key.text.toString()
                                            adapter.notifyDataSetChanged()
                                            dialog.dismiss()
                                        }
                            }
                            dialog.setView(dialogView)
                            dialog.show()
                        }
                        listView.setOnItemLongClickListener { parent, view, position, id ->
                            AlertDialog.Builder(this)
                                    .setTitle("Delete Category?")
                                    .setMessage("Are you sure?")
                                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                                        val deleteSong: MutableMap<String, Any> = HashMap()
                                        deleteSong[categoryItemsArray[position].key] = FieldValue.delete()
                                        firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                                .document(Constants.FD_SHOPS_MAIN_CATEGORY)
                                                .update(deleteSong)
                                                .addOnCompleteListener {
                                                    categoryItemsArray.removeAt(position)
                                                    category_names.removeAt(position)
                                                    category_keys.removeAt(position)
                                                    adapter.notifyDataSetChanged()
                                                    dialog.dismiss()
                                                    Toast.makeText(this, "DELETED", Toast.LENGTH_SHORT).show()
                                                }

                                    })
                                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                                        dialog.dismiss()
                                    })
                                    .create().show()
                            true
                        }

                        addCategoriessButton.setOnClickListener {
                            val dialog = AlertDialog.Builder(this).create()
                            val dialogView = LayoutInflater.from(this)
                                    .inflate(R.layout.dialog_add_shop_category, null)
                            dialogView.addProductCategoriesButton.text = "Add Category"
                            dialogView.addProductCategoriesButton.setOnClickListener {
                                dialog.setCancelable(false)
                                dialog.setCanceledOnTouchOutside(false)
                                dialogView.addProductCategoriesButton.isEnabled = false
                                dialogView.addProductCategoriesButton.text = "Adding..."
                                val hashMap = HashMap<String, String>()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_NAME] = dialogView.edt_shop_name.text.toString()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_KEY] = dialogView.edt_shop_key.text.toString()
                                hashMap[Constants.FIELD_FD_SHOPS_MAIN_CATEGORY_ORDER] = dialogView.edt_shop_order.text.toString()
                                val hm2 = HashMap<String, HashMap<String, String>>()
                                val key = "SC"+System.currentTimeMillis()
                                hm2[key] = hashMap
                                firebaseFirestore.collection(Constants.FC_SHOPS_MAIN_CATEGORY)
                                        .document(Constants.FD_SHOPS_MAIN_CATEGORY)
                                        .update(hm2 as Map<String, Any>).addOnCompleteListener {
                                            categoryItemsArray.add(ShopCategoryItem(
                                                    key = key,
                                                    name = dialogView.edt_shop_name.text.toString(),
                                                    category_key = dialogView.edt_shop_key.text.toString(),
                                                    order = dialogView.edt_shop_order.text.toString().toInt(),
                                            ))
                                            category_names.add(dialogView.edt_shop_name.text.toString())
                                            category_keys.add(dialogView.edt_shop_key.text.toString())
                                            adapter.notifyDataSetChanged()
                                            dialog.dismiss()
                                        }
                            }
                            dialog.setView(dialogView)
                            dialog.show()
                        }

                    }else{
                        task.exception!!.printStackTrace()
                    }
                }
    }

    private fun initVars() {

    }

    fun addShopCategoryClick(view: View) {

    }
}