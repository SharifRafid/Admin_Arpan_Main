package admin.arpan.delivery.db.model

data class OrderOldItems(
    var key : String = "",
    var date : String = "",
    var orders : ArrayList<OrderItemMain> = ArrayList()
)