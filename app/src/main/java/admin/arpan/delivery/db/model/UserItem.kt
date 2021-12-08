package admin.arpan.delivery.db.model

data class UserItem(
    var key : String = "",
    var address : String = "",
    var name : String = "",
    var phone : String = "",
    var profile_image : String = "",
    var ordersCountLastMonth : Int = 0
)
