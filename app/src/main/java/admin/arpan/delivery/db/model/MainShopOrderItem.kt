package admin.arpan.delivery.db.model

data class MainShopOrderItem(
    var key : String = "",
    var shop_doc_id : String = "",
    var cart_products : ArrayList<CartProductEntity> = ArrayList(),
    var shop_details : ShopItem = ShopItem()
)