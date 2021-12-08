package admin.arpan.delivery.db.model

data class DaStatusItem(
    var key : String = "",
    var name : String = "",
    var status : String = "",
    var date : String = "",
    var orders : Int = 0,
    var statusTextTitle : String = "",
)
