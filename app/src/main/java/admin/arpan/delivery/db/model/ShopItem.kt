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
    )