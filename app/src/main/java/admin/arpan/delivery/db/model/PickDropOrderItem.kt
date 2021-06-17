package admin.arpan.delivery.db.model

data class PickDropOrderItem(
    var key : String = "",
    var senderName : String = "",
    var senderPhone : String = "",
    var senderAddress : String = "",
    var senderLocation : String = "",
    var parcelDetails : String = "",
    var recieverName : String = "",
    var recieverPhone : String = "",
    var recieverAddress : String = "",
    var recieverLocation : String = "",
    var paymentType : String = "COD",
    var parcelImage : String = ""
)