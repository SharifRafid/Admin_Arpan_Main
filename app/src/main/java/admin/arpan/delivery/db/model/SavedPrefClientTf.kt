package admin.arpan.delivery.db.model

data class SavedPrefClientTf(
    var key : String = "",
    var user_name : String = "",
    var user_mobile : String = "",
    var user_address : String = "",
    var user_note : String = "",
    var user_order_details : String = "",
    var delivery_charge : String = "",
    var da_charge : String = "",
    var total_charge : String = "",
)
