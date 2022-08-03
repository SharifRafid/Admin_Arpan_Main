package admin.arpan.delivery.models

data class Product(
    var arpanCharge: Int? = null,
    var categories: List<String>? = null,
    var coverPhoto: Any? = null,
    var description: String? = null,
    var icon: Image? = null,
    var id: String? = null,
    var inStock: Boolean? = null,
    var name: String? = null,
    var offerActive: Boolean? = null,
    var offerDetails: String? = null,
    var offerPrice: Int? = null,
    var order: Int? = null,
    var price: Int? = null,
    var shop: String? = null,
    var shortDescription: String? = null
)