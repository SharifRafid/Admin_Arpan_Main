package admin.arpan.delivery.db.model

data class LocationItem(
    var key : String = "",
    var locationName : String = "",
    var deliveryCharge : Int = 0,
    var order : Int = 0
)