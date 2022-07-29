package admin.arpan.delivery.db.model

data class CartProductEntity (
    var id : Int = 0,

    var product_item : Boolean = false,
    var custom_order_item : Boolean = false,
    var medicine_item : Boolean = false,
    var parcel_item : Boolean = false,

    var product_item_key : String = "",
    var product_item_name : String = "",
    var product_item_shop_key : String = "",
    var product_item_shop_name : String = "",
    var product_item_category_tag : String = "",
    var product_item_price : Int = 0,
    var product_item_offer_price : Int = 0,
    var product_arpan_profit : Int = 0,
    var product_item_image : String = "",
    var product_item_desc : String = "",
    var product_item_amount : Int = 1,

    //Custom Order
    var custom_order_text : String = "",
    var custom_order_image : String = "",

    //Medicine Order
    var medicine_order_text : String = "",
    var medicine_order_text_2 : String = "",
    var medicine_order_image : String = "",

    //Parcel Order
    var parcel_order_text : String = "",
    var parcel_order_text_2 : String = "",
    var parcel_order_image : String = ""
)