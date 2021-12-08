package admin.arpan.delivery.db.model

data class ProductCategoryItem (
    val key : String,
    var name : String,
    val category_key : String,
    val order : Int
)