package admin.arpan.delivery.models

data class Location(
    var error: Boolean? = null,
    var message: String? = null,
    var daCharge: Int? = null,
    var daChargePickDrop: Int? = null,
    var deliveryCharge: Int? = null,
    var deliveryChargeClient: Int? = null,
    var deliveryChargePickDrop: Int? = null,
    var id: String? = null,
    var locationName: String? = null,
    var order: Int? = null
)