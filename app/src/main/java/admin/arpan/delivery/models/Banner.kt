package admin.arpan.delivery.models

data class Banner(
    var error: Boolean? = null,
    var message: String? = null,
    var icon: Image? = null,
    var id: String? = null,
    var order: Int? = null
)