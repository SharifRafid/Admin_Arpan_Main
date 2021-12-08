package admin.arpan.delivery.db.model

data class ShopItem (
    var key : String = "",
    var name : String = "",
    var categories : String = "",
    var image : String = "",
    var cover_image : String = "",
    var da_charge : String = "",
    var deliver_charge : String = "",
    var location : String = "",
    var username : String = "",
    var password : String = "",
    var order : Int = 0,
    var status : String = "",
    val isClient : String = "no",
    val dynamicLink : String = "",
    var shopNotice : String = "",
    var shopNoticeColor : String = "",
    var shopNoticeColorBg : String = "",

    var shopDiscount : Boolean = false,
    var shopCategoryDiscount : Boolean = false,
    var shopCategoryDiscountName : String = "",
    var shopDiscountPercentage : Float = 0f,
    var shopDiscountMinimumPrice : Float = 0f,
)